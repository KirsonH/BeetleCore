-- 1. CONFIGURATION PLANE (Product Forge & Regulatory Policies)
CREATE SCHEMA IF NOT EXISTS bc_config;

CREATE TABLE IF NOT EXISTS bc_config.product_traits (
    trait_key VARCHAR(100) PRIMARY KEY,
    display_name VARCHAR(150) NOT NULL,            -- Descriptive Field
    description TEXT,                              -- Descriptive Field
    behavior_rules JSONB NOT NULL,                 -- Declarative OPA definitions
    i18n_manifest JSONB NOT NULL,                  -- Dynamic i18n support (labels, localized constraints)
    
    -- Forensic Audit Trail
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL,              -- System or human architect ID
    change_reason TEXT NOT NULL
);

-- 2. IMMUTABLE CORE LEDGER (The Invariant BISS Stream)
CREATE SCHEMA IF NOT EXISTS bc_core;

CREATE TABLE IF NOT EXISTS bc_core.atomic_ledger (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Beetle Influx Transversal Dimensions
    tenant_id VARCHAR(50) NOT NULL,                -- Multi-company / Tenant isolation
    branch_id VARCHAR(50) NOT NULL,                -- Regional/Sucursal isolation
    book_id VARCHAR(50) NOT NULL,                  -- Accounting ledger isolation (Treasury vs Ops)
    
    -- BISS Invariant Contract fields
    transaction_code VARCHAR(10) NOT NULL,         -- Standard action code (e.g., '02')
    biss_payload JSONB NOT NULL,                   -- JSON message body
    
    -- Multi-Language & Regional Context (i18n/l10n dimension)
    system_locale VARCHAR(10) NOT NULL DEFAULT 'en_US', 
    system_timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    system_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- Forensic Audit Trail (Strictly Append-Only)
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL,              -- Identity Consumer reference
    client_session_id VARCHAR(100),                -- Hardware device session tracking
    blockchain_anchor_hash VARCHAR(66) DEFAULT NULL -- Future EVM cryptographic anchor
);

CREATE INDEX IF NOT EXISTS idx_core_ledger_enterprise_dimensions 
ON bc_core.atomic_ledger (tenant_id, branch_id, book_id, transaction_code);

-- 3. BEETLE MIRROR SANDBOX PLANE (Zero-Infrastructure Logical Forking)
CREATE SCHEMA IF NOT EXISTS bc_sandbox;

CREATE TABLE IF NOT EXISTS bc_sandbox.simulated_ledger (
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

-- 4. OPERATIONAL DATA PLANE (Atomic Composition Schema)
CREATE SCHEMA IF NOT EXISTS bc_data;

CREATE TABLE IF NOT EXISTS bc_data.financial_entities (
    entity_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,              -- BIAN Service Domain (e.g., 'CUSTOMER')
    display_name VARCHAR(200),                     -- Descriptive Name
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bc_data.entity_atoms (
    atom_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID REFERENCES bc_data.financial_entities(entity_id) ON DELETE CASCADE,
    tenant_id VARCHAR(50) NOT NULL,
    atom_type VARCHAR(50) NOT NULL,                -- e.g., 'STATE_ATOM', 'AI_PROFILE_ATOM'
    
    -- Core Pure JSON Data Plane
    payload JSONB NOT NULL,                        -- Dynamic JSON data with native fields
    i18n_overrides JSONB,                          -- Localized strings per tenant/jurisdiction
    
    -- Forensic Audit Trail & Lineage
    lineage_event_id UUID NOT NULL,                -- Strict pointer to the ledger event that modified it
    appended_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operator_id VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_atoms_jsonb_payload ON bc_data.entity_atoms USING gin (payload);
CREATE INDEX IF NOT EXISTS idx_atoms_forensic_lineage ON bc_data.entity_atoms (tenant_id, atom_type, lineage_event_id);