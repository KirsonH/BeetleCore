package com.beetlecore.engine.ports;

import com.beetlecore.engine.domain.BissMessage;

import java.sql.SQLException;

public interface EntityAtomPort {
    void persistEntityAtom(String datasourceKey, String lineageId, BissMessage message) throws SQLException;
}
