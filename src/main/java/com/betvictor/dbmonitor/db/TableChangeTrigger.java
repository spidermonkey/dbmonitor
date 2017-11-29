package com.betvictor.dbmonitor.db;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class TableChangeTrigger extends TriggerAdapter {

    public static final String TRIGGER_INSERT_STATEMENT = "INSERT INTO SOME_TABLE_AUDIT_TRAIL(table_name, id, timestamp, change_type, notified) VALUES (?, ?, CURRENT_TIMESTAMP(), ?, ?);";

    @Override
    public void fire(Connection connection, ResultSet resultSet, ResultSet resultSet1) throws SQLException {
        log.info("Database trigger [{}] fired on table [{}]", this.triggerName, this.tableName);
        PreparedStatement preparedStatement = connection.prepareStatement(TRIGGER_INSERT_STATEMENT);
        preparedStatement.setString(1, this.tableName);
        preparedStatement.setString(2, resultSet1.getString("ID"));
        preparedStatement.setString(3, this.type == 1 ? "INSERT" : "UPDATE");
        preparedStatement.setBoolean(4, false);
        preparedStatement.executeUpdate();
    }
}