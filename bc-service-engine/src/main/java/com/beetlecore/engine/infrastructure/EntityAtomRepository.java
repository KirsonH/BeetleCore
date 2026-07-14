package com.beetlecore.engine.infrastructure;

import com.beetlecore.engine.domain.BissMessage;
import com.beetlecore.engine.ports.EntityAtomPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntityAtomRepository implements EntityAtomPort {

    private final SqlDataAccessor accessor;

    public EntityAtomRepository(SqlDataAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public void persistEntityAtom(String datasourceKey, String lineageId, BissMessage message) throws SQLException {
        try (Connection connection = accessor.getConnection(datasourceKey); PreparedStatement statement = connection.prepareStatement(accessor.getInsertAtomSql())) {
            statement.setString(1, lineageId);
            statement.setString(2, message.context().tenantId());
            statement.setString(3, message.context().branchId());
            statement.setString(4, message.context().bookId());
            statement.setObject(5, createJsonb(message));
            statement.executeUpdate();
        }
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
