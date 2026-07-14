package com.beetlecore.engine.infrastructure;

import com.beetlecore.engine.domain.BissMessage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.helidon.config.Config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DataSourceRegistry implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(DataSourceRegistry.class.getName());

    private static final String DATASOURCE_ROOT = "beetlecore.datasources";
    private final Map<String, HikariDataSource> registry = new LinkedHashMap<>();

    public DataSourceRegistry() {
        Config datasourceRoot = ApplicationConfig.config().get(DATASOURCE_ROOT);
        if (!datasourceRoot.exists()) {
            throw new IllegalStateException("Missing configuration section: " + DATASOURCE_ROOT);
        }

        Map<String, String> flattened = datasourceRoot.asMap().orElse(Collections.emptyMap());
        Set<String> datasourceKeys = flattened.keySet().stream()
                .map(key -> key.contains(".") ? key.substring(0, key.indexOf('.')) : key)
                .filter(key -> !key.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (datasourceKeys.isEmpty()) {
            throw new IllegalStateException("No datasources defined under " + DATASOURCE_ROOT);
        }

        for (String datasourceKey : datasourceKeys) {
            Config config = datasourceRoot.get(datasourceKey);
            String url = config.get("url").asString().orElseThrow(() -> new IllegalStateException("Missing url for datasource " + datasourceKey));
            String user = config.get("user").asString().orElseThrow(() -> new IllegalStateException("Missing user for datasource " + datasourceKey));
            String pass = config.get("pass").asString().orElseThrow(() -> new IllegalStateException("Missing pass for datasource " + datasourceKey));
            int poolSize = config.get("pool-size").asInt().orElse(8);

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            hikariConfig.setMaximumPoolSize(poolSize);
            hikariConfig.setMinimumIdle(Math.max(1, poolSize / 2));
            hikariConfig.setConnectionTimeout(10000);
            hikariConfig.setPoolName("BeetleCore-" + datasourceKey + "-Pool");

            this.registry.put(datasourceKey, new HikariDataSource(hikariConfig));
        }
    }

    public Connection getConnection(String datasourceKey) throws SQLException {
        return getDataSource(datasourceKey).getConnection();
    }

    public Connection getConnection(BissMessage message) throws SQLException {
        return getConnection(resolveDataSourceKey(message));
    }

    public DataSource getDataSource(String datasourceKey) {
        HikariDataSource dataSource = registry.get(datasourceKey);
        if (dataSource == null) {
            if (registry.containsKey("default")) {
                LOGGER.log(Level.WARNING, "Datasource key '{0}' is not registered. Falling back to default datasource pool.", datasourceKey);
                return registry.get("default");
            }
            throw new IllegalArgumentException("Unknown datasource key and no default datasource configured: " + datasourceKey);
        }
        return dataSource;
    }

    public String resolveDataSourceKey(BissMessage message) {
        if (message.header().token() != null && message.header().token().contains("sidecar-audit")) {
            return "sidecar-audit";
        }
        if ("audit".equalsIgnoreCase(message.header().system())) {
            return "sidecar-audit";
        }
        if (message.context() != null && "audit".equalsIgnoreCase(message.context().dataJurisdiction())) {
            return "sidecar-audit";
        }
        return "default";
    }

    public Set<String> datasourceKeys() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    @Override
    public void close() {
        registry.values().forEach(HikariDataSource::close);
        registry.clear();
    }
}
