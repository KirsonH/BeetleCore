package com.beetlecore.engine.infrastructure;

import com.beetlecore.engine.domain.BissInterceptor;
import com.beetlecore.engine.domain.BissMessage;
import com.beetlecore.engine.ports.EntityAtomPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BeetleCoreRouter {

    private final SqlDataAccessor accessor;
    private final EntityAtomPort entityAtomPort;
    private final OidcTokenValidator tokenValidator = new OidcTokenValidator();
    private final List<BissInterceptor> interceptors;

    public BeetleCoreRouter(SqlDataAccessor accessor, EntityAtomPort entityAtomPort, List<BissInterceptor> interceptors) {
        this.accessor = accessor;
        this.entityAtomPort = entityAtomPort;
        this.interceptors = interceptors;
    }

    public void route(String jwtToken, BissMessage message) throws SQLException {
        for (BissInterceptor interceptor : interceptors) {
            try {
                if (!interceptor.preExecute(message)) {
                    throw new SecurityException(BissLocalizationEngine.getLocalizedMessage(message, "security.policy.denied", interceptor.getClass().getSimpleName()));
                }
            } catch (SecurityException se) {
                throw new SecurityException(BissLocalizationEngine.getLocalizedMessage(message, "security.policy.exception", se.getMessage()), se);
            }
        }

        String datasourceKey = accessor.resolveDataSourceKey(message);
        String federatedIdentityId = tokenValidator.getFederatedIdentityId(jwtToken);

        try (Connection connection = accessor.getConnection(message)) {
            BissMessage.Context resolvedContext = resolveContextFromIdentity(connection, federatedIdentityId);
            BissMessage routedMessage = new BissMessage(message.header(), resolvedContext, message.audit(), message.body());

            String table = routedMessage.header().isSandbox() ? "bc_sandbox.simulated_ledger" : "bc_core.atomic_ledger";
            try (PreparedStatement statement = connection.prepareStatement(String.format(accessor.getInsertLedgerSql(), table))) {
                statement.setString(1, routedMessage.header().transactionCode());
                statement.setBoolean(2, routedMessage.header().isSandbox());
                statement.setString(3, routedMessage.header().system());
                statement.setString(4, routedMessage.header().openBankingConsentId());
                statement.setString(5, routedMessage.header().token());
                statement.setString(6, routedMessage.context().tenantId());
                statement.setString(7, routedMessage.context().branchId());
                statement.setString(8, routedMessage.context().bookId());
                statement.setString(9, routedMessage.context().systemLocale());
                statement.setString(10, routedMessage.context().systemTimezone());
                statement.setString(11, routedMessage.context().systemCurrency());
                statement.setObject(12, createJsonb(routedMessage.context().geolocation()));
                statement.setString(13, routedMessage.context().dataJurisdiction());
                statement.setString(14, routedMessage.audit().operatorId());
                statement.setString(15, routedMessage.audit().clientSessionId());
                statement.setObject(16, createJsonb(routedMessage.body()));
                statement.executeUpdate();
            }

            String lineageId = deriveLineageId(routedMessage);
            entityAtomPort.persistEntityAtom(datasourceKey, lineageId, routedMessage);
        }
    }

    private BissMessage.Context resolveContextFromIdentity(Connection connection, String federatedIdentityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(accessor.getSelectIdentitySql())) {
            statement.setString(1, federatedIdentityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SecurityException("No identity security atom found for federated identity id: " + federatedIdentityId);
                }
                return mapContextFromIdentityPayload(resultSet.getString("payload"));
            }
        } catch (Exception error) {
            if (error instanceof SQLException sqlException) {
                throw sqlException;
            }
            throw new SecurityException("Unable to resolve identity context: " + error.getMessage(), error);
        }
    }


    private BissMessage.Context mapContextFromIdentityPayload(String payloadText) throws Exception {
        JsonNode payload = MAPPER.readTree(payloadText);
        JsonNode geolocation = payload.path("geolocation");
        return new BissMessage.Context(
                payload.path("tenantId").asText(payload.path("tenant_id").asText("")),
                payload.path("branchId").asText(payload.path("branch_id").asText("")),
                payload.path("bookId").asText(payload.path("book_id").asText("")),
                payload.path("systemLocale").asText("en-US"),
                payload.path("systemTimezone").asText("UTC"),
                payload.path("systemCurrency").asText("USD"),
                new BissMessage.Geolocation(
                        geolocation.path("latitude").isNumber() ? geolocation.path("latitude").doubleValue() : 0.0,
                        geolocation.path("longitude").isNumber() ? geolocation.path("longitude").doubleValue() : 0.0,
                        geolocation.path("country").asText(""),
                        geolocation.path("region").asText("")
                ),
                payload.path("dataJurisdiction").asText(payload.path("data_jurisdiction").asText(""))
        );
    }


    private static String deriveLineageId(BissMessage message) {
        if (message.header().openBankingConsentId() != null && !message.header().openBankingConsentId().isBlank()) {
            return message.header().openBankingConsentId();
        }
        return String.join(":", message.header().transactionCode(), message.context().tenantId(), message.context().bookId());
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static PGobject createJsonb(Object value) throws SQLException {
        PGobject object = new PGobject();
        object.setType("jsonb");
        try {
            object.setValue(MAPPER.writeValueAsString(value));
        } catch (Exception error) {
            throw new SQLException("Unable to serialize JSONB payload", error);
        }
        return object;
    }
}
