-- limpieza previa de tablas afectadas para evitar conflictos de tipos
DROP TABLE IF EXISTS bc_data.entity_atoms CASCADE;
DROP TABLE IF EXISTS bc_data.identity_security_atoms CASCADE;
DROP TABLE IF EXISTS bc_data.financial_entities CASCADE;
DROP TABLE IF EXISTS bc_config.access_control_policies CASCADE;
DROP TABLE IF EXISTS bc_config.product_traits CASCADE;

-- ====================================================================
-- PLANO 1: CONFIGURATION PLANE (Product Forge, OPA Policies & i18n)
-- ====================================================================
CREATE SCHEMA IF NOT EXISTS bc_config;

CREATE TABLE bc_config.product_traits (
    trait_key VARCHAR(100) PRIMARY KEY,
    display_name VARCHAR(150) NOT NULL,            -- Descriptive field
    description TEXT,                              -- Descriptive field
    behavior_rules JSONB NOT NULL,                 -- Declarative product rules & boundaries
    i18n_manifest JSONB NOT NULL,                  -- Dynamic i18n support (labels, localized formats)
    
    -- Forensic Audit Trail
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL,              -- System or human architect ID
    change_reason TEXT NOT NULL
);

CREATE TABLE bc_config.access_control_policies (
    policy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_key VARCHAR(50) NOT NULL,               -- e.g., 'TELLER', 'RISK_ANALYST', 'CUSTOMER'
    allowed_transaction_codes VARCHAR(10)[] NOT NULL, -- BISS Standard allowed codes (e.g., {'02', '10'})
    opa_policy_payload JSONB NOT NULL,           -- Granular rules (e.g., max_transaction_amount)
    
    -- Forensic Audit & Descriptive Fields
    display_name VARCHAR(150) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL
);
CREATE UNIQUE INDEX idx_policy_role ON bc_config.access_control_policies(role_key);

-- ====================================================================
-- PLANO 2: IMMUTABLE CORE LEDGER (The Invariant BISS Production Stream)
-- ====================================================================
CREATE SCHEMA IF NOT EXISTS bc_core;

CREATE TABLE bc_core.atomic_ledger (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Beetle Influx Transversal Dimensions
    tenant_id VARCHAR(50) NOT NULL,                -- Multi-company / Tenant isolation
    branch_id VARCHAR(50) NOT NULL,                -- Regional/Branch isolation
    book_id VARCHAR(50) NOT NULL,                  -- Accounting ledger isolation (Treasury vs Ops)
    
    -- BISS Invariant Contract fields
    transaction_code VARCHAR(10) NOT NULL,         -- Standard action code (e.g., '02' Register)
    biss_payload JSONB NOT NULL,                   -- JSON message body contract
    
    -- Multi-Language & Regional Context (i18n dimension)
    system_locale VARCHAR(10) NOT NULL DEFAULT 'en_US', 
    system_timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    system_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- Forensic Audit Trail (Strictly Append-Only)
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL,              -- Identity Consumer reference
    client_session_id VARCHAR(100),                -- Hardware device session tracking
    blockchain_anchor_hash VARCHAR(66) DEFAULT NULL 
);
CREATE INDEX idx_core_ledger_enterprise_dimensions 
ON bc_core.atomic_ledger (tenant_id, branch_id, book_id, transaction_code);

-- ====================================================================
-- PLANO 3: BEETLE MIRROR SANDBOX PLANE (Zero-Infrastructure Logical Forking)
-- ====================================================================
CREATE SCHEMA IF NOT EXISTS bc_sandbox;

CREATE TABLE bc_sandbox.simulated_ledger (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_real_event_id UUID DEFAULT NULL,        -- Allows Logical Forking referencing production
    tenant_id VARCHAR(50) NOT NULL,
    branch_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    transaction_code VARCHAR(10) NOT NULL,
    biss_payload JSONB NOT NULL,
    
    -- Localization Dimension
    system_locale VARCHAR(10) NOT NULL DEFAULT 'en_US',
    system_timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    
    -- Simulation Metadata & Audit
    simulated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    virtual_timestamp TIMESTAMP WITH TIME ZONE,    -- Time Machine simulation clock
    operator_id VARCHAR(50) NOT NULL
);

-- ====================================================================
-- PLANO 4: OPERATIONAL DATA PLANE (Atomic Composition & OIDC Mapping)
-- ====================================================================
CREATE SCHEMA IF NOT EXISTS bc_data;

CREATE TABLE bc_data.financial_entities (
    entity_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,              -- BIAN Service Domain (e.g., 'CUSTOMER', 'PRODUCT')
    display_name VARCHAR(200),                     -- Descriptive Name
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Identity Security ATOM (OIDC Federation Mapping - No passwords stored locally)
CREATE TABLE bc_data.identity_security_atoms (
    atom_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID REFERENCES bc_data.financial_entities(entity_id) ON DELETE CASCADE,
    tenant_id VARCHAR(50) NOT NULL,                -- Influx Dimension
    branch_id VARCHAR(50) NOT NULL,                -- Influx Dimension
    
    -- IDP Federation (The 'sub' claim from Keycloak / Okta / Auth0 JWT)
    federated_identity_id VARCHAR(255) NOT NULL,   -- e.g., 'keycloak|u_746a-84bd...'
    username VARCHAR(100) NOT NULL,                -- Audit convenience field
    assigned_role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER', -- Financial role for OPA evaluation
    
    -- i18n, Pure JSON Payload & Audit attributes
    payload JSONB NOT NULL,                        -- Dynamic parameters in JSON format
    i18n_preferences JSONB NOT NULL,               -- Language, locale, timezone overrides
    lineage_event_id UUID NOT NULL,                -- Strict forensic pointer to the ledger event
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX idx_federated_identity_tenant 
ON bc_data.identity_security_atoms(federated_identity_id, tenant_id);

-- General Entity ATOMs Data Plane (Pure JSONB)
CREATE TABLE bc_data.entity_atoms (
    atom_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID REFERENCES bc_data.financial_entities(entity_id) ON DELETE CASCADE,
    tenant_id VARCHAR(50) NOT NULL,
    atom_type VARCHAR(50) NOT NULL,                -- e.g., 'STATE_ATOM', 'AI_PROFILE_ATOM'
    
    -- Core Pure JSON Data Plane
    payload JSONB NOT NULL,                        -- Dynamic business attributes JSON
    i18n_overrides JSONB,                          -- Localized strings per jurisdiction
    
    -- Forensic Audit Trail & Lineage
    lineage_event_id UUID NOT NULL,                -- Strict pointer to the core ledger event
    appended_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL
);
CREATE INDEX idx_atoms_jsonb_payload ON bc_data.entity_atoms USING gin (payload);
CREATE INDEX idx_atoms_forensic_lineage ON bc_data.entity_atoms (tenant_id, atom_type, lineage_event_id);