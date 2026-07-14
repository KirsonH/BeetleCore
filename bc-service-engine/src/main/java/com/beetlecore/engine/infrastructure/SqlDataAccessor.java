package com.beetlecore.engine.infrastructure;

import com.beetlecore.engine.domain.BissMessage;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class SqlDataAccessor implements AutoCloseable {

    private final DataSourceRegistry registry;
    private final String insertLedgerSql;
    private final String insertAtomSql;
    private final String selectIdentitySql;
    private final String selectPolicySql;

    public SqlDataAccessor() {
        this(new DataSourceRegistry());
    }

    public SqlDataAccessor(DataSourceRegistry registry) {
        this.registry = registry;
        this.insertLedgerSql = ApplicationConfig.getString("beetlecore.sql.insertLedger", "INSERT INTO %s (transaction_code, is_sandbox, system, open_banking_consent_id, token, tenant_id, branch_id, book_id, system_locale, system_timezone, system_currency, geolocation, data_jurisdiction, operator_id, client_session_id, payload, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");
        this.insertAtomSql = ApplicationConfig.getString("beetlecore.sql.insertAtom", "INSERT INTO bc_data.entity_atoms (lineage_id, tenant_id, branch_id, book_id, payload, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
        this.selectIdentitySql = ApplicationConfig.getString("beetlecore.sql.selectIdentity", "SELECT payload FROM bc_data.identity_security_atoms WHERE federated_identity_id = ? ORDER BY created_at DESC LIMIT 1");
        this.selectPolicySql = ApplicationConfig.getString("beetlecore.sql.selectPolicy", "SELECT policy_document FROM bc_config.access_control_policies WHERE enabled = true ORDER BY updated_at DESC LIMIT 1");
    }

    public Connection getConnection() throws SQLException {
        return registry.getConnection("default");
    }

    public Connection getConnection(String datasourceKey) throws SQLException {
        return registry.getConnection(datasourceKey);
    }

    public Connection getConnection(BissMessage message) throws SQLException {
        return registry.getConnection(message);
    }

    public String resolveDataSourceKey(BissMessage message) {
        return registry.resolveDataSourceKey(message);
    }

    public DataSource getDataSource() {
        return registry.getDataSource("default");
    }

    public DataSource getDataSource(String datasourceKey) {
        return registry.getDataSource(datasourceKey);
    }

    public String getInsertLedgerSql() {
        return insertLedgerSql;
    }

    public String getInsertAtomSql() {
        return insertAtomSql;
    }

    public String getSelectIdentitySql() {
        return selectIdentitySql;
    }

    public String getSelectPolicySql() {
        return selectPolicySql;
    }

    public int getServerPort() {
        return ApplicationConfig.getInt("beetlecore.server.port", 8080);
    }

    @Override
    public void close() {
        registry.close();
    }
}
