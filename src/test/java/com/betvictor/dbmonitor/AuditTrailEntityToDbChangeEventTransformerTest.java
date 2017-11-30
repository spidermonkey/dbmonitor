package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.betvictor.dbmonitor.enitites.DataBaseEvent.DataBaseEventType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AuditTrailEntityToDbChangeEventTransformerTest {

    public AuditTrailEntityToDbChangeEventTransformer transformer = new AuditTrailEntityToDbChangeEventTransformer();

    public AuditTrailEntity.AuditTrailEntityBuilder testAuditTrailBuilder =  AuditTrailEntity
            .builder()
            .id("id")
            .changeType("INSERT")
            .timeStamp(LocalDateTime.now())
            .notified(true)
            .tableName("table");

    @Test
    public void shouldTransformEntity() {
        LocalDateTime now = LocalDateTime.now();
        testAuditTrailBuilder
                .timeStamp(now);
        DataBaseEvent dataBaseEvent = transformer.transform(testAuditTrailBuilder.build());
        assertThat(dataBaseEvent.getRowId()).isEqualTo("id");
        assertThat(dataBaseEvent.getTableName()).isEqualTo("table");
        assertThat(dataBaseEvent.getTimeStamp()).isEqualTo(now);
        assertThat(dataBaseEvent.getDataBaseEventType()).isEqualTo(INSERT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailInCaseOfInvalidChangeType() {
        testAuditTrailBuilder.changeType("BLA");
        transformer.transform(testAuditTrailBuilder.build());
    }



}