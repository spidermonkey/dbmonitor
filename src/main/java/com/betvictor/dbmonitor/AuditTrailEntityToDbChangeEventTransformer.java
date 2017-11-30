package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.GenericTransformer;

@Slf4j
public class AuditTrailEntityToDbChangeEventTransformer implements GenericTransformer<AuditTrailEntity, DataBaseEvent> {
    @Override
    public DataBaseEvent transform(AuditTrailEntity auditTrailEntity) {
        log.debug("Transforming audit trail entity with id: {}", auditTrailEntity.getId());
        return DataBaseEvent
                .builder()
                .tableName(auditTrailEntity.getTableName())
                .rowId(auditTrailEntity.getId())
                .timeStamp(auditTrailEntity.getTimeStamp())
                .dataBaseEventType(DataBaseEvent.DataBaseEventType.valueOf(auditTrailEntity.getChangeType()))
                .build();
    }
}
