package com.betvictor.dbmonitor.enitites;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
@Data
public class AuditTrailEntity {
    @NonNull
    private final String tableName;
    @NonNull
    private final String id;
    @NonNull
    private final LocalDateTime timeStamp;
    @NonNull
    private final String changeType;
    @NonNull
    private final boolean notified;
}
