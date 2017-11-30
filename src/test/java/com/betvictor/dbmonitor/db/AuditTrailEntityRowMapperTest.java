package com.betvictor.dbmonitor.db;

import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditTrailEntityRowMapperTest {

    private AuditTrailEntityRowMapper auditTrailEntityRowMapper = new AuditTrailEntityRowMapper();

    @Mock
    private ResultSet resultSet;

    @Test
    public void shouldMapFieldsCorrectly() throws SQLException {
        Timestamp someTimeStamp = new Timestamp(1000);
        when(resultSet.getString("ID")).thenReturn("testId");
        when(resultSet.getString("TABLE_NAME")).thenReturn("testTableName");
        when(resultSet.getString("CHANGE_TYPE")).thenReturn("testChangeType");
        when(resultSet.getBoolean("NOTIFIED")).thenReturn(true);
        when(resultSet.getTimestamp("TIMESTAMP")).thenReturn(someTimeStamp);
        AuditTrailEntity result = auditTrailEntityRowMapper.mapRow(resultSet, 0);
        assertThat(result.getId()).isEqualTo("testId");
        assertThat(result.getTableName()).isEqualTo("testTableName");
        assertThat(result.getChangeType()).isEqualTo("testChangeType");
        assertThat(result.getTimeStamp()).isEqualTo(someTimeStamp.toLocalDateTime());
        assertThat(result.isNotified()).isEqualTo(true);
    }

}