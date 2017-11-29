package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import org.springframework.integration.transformer.GenericTransformer;

public class AuditTrailEntityToDbChangeEventTransformer implements GenericTransformer<AuditTrailEntity, DataBaseEvent> {
    @Override
    public DataBaseEvent transform(AuditTrailEntity auditTrailEntity) {
        return DataBaseEvent
                .builder()
                .tableName(auditTrailEntity.getTableName())
                .rowId(auditTrailEntity.getId())
                .timeStamp(auditTrailEntity.getTimeStamp())
                .dataBaseEventType(DataBaseEvent.DataBaseEventType.valueOf(auditTrailEntity.getChangeType()))
                .build();
    }
}
