package com.betvictor.dbmonitor.db;

import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditTrailEntityRowMapper implements RowMapper<AuditTrailEntity> {
    @Override
    public AuditTrailEntity mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return AuditTrailEntity
                .builder()
                .id(resultSet.getString("ID"))
                .tableName(resultSet.getString("TABLE_NAME"))
                .changeType(resultSet.getString("CHANGE_TYPE"))
                .notified(resultSet.getBoolean("NOTIFIED"))
                .timeStamp(resultSet.getTimestamp("TIMESTAMP").toLocalDateTime())
                .build();
    }
}
