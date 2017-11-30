package com.betvictor.dbmonitor.helpers;

import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;

@UtilityClass
public class DataBaseHelper {

    public static void insertIntoTableUnderMonitor(final JdbcTemplate jdbcTemplate, final String id, final String attribute1, final String attribute2) {
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO SOME_TABLE(id, some_attribute_1, some_attribute_2) VALUES (?, ?, ?);");
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, attribute1);
            preparedStatement.setString(3, attribute2);
            return preparedStatement;
        });
    }

    public static void truncateTables(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("TRUNCATE TABLE AUDIT_TRAIL");
        jdbcTemplate.execute("TRUNCATE TABLE SOME_TABLE");
    }
}
