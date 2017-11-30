package com.betvictor.dbmonitor.db;

import com.betvictor.dbmonitor.helpers.DataBaseHelper;
import com.betvictor.dbmonitor.DbMonitorApplication;
import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DbMonitorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TableChangeTriggerIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void cleanDb() {
        DataBaseHelper.truncateTables(jdbcTemplate);
    }

    @Test
    public void shouldTriggerExecutedAndMapCorrectValues() throws InterruptedException {
        LocalDateTime testStartTime = LocalDateTime.now();
        DataBaseHelper.insertIntoTableUnderMonitor(jdbcTemplate, "Test1", "Test2", "Test3");
        Thread.sleep(100); //makes sense to wait until the trigger kicks off
        List<AuditTrailEntity> result = jdbcTemplate.query("SELECT * FROM AUDIT_TRAIL", new AuditTrailEntityRowMapper());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangeType()).isEqualTo("INSERT");
        assertThat(result.get(0).getTableName()).isEqualTo("SOME_TABLE");
        assertThat(result.get(0).getId()).isEqualTo("Test1");
        assertThat(result.get(0).getTimeStamp()).isBetween(testStartTime, LocalDateTime.now());
    }

    @Test
    public void shouldTriggerExecutedInCaseOfUpdate() throws InterruptedException {
        DataBaseHelper.insertIntoTableUnderMonitor(jdbcTemplate, "Test1", "Test2", "Test3");
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE SOME_TABLE SET some_attribute_1= ? WHERE ID=?;");
            preparedStatement.setString(1, "updated_value");
            preparedStatement.setString(2, "Test1");
            return preparedStatement;
        });
        Thread.sleep(100);
        List<AuditTrailEntity> result = jdbcTemplate.query("SELECT * FROM AUDIT_TRAIL", new AuditTrailEntityRowMapper());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getChangeType()).isEqualTo("INSERT");
        assertThat(result.get(1).getChangeType()).isEqualTo("UPDATE");
    }



}