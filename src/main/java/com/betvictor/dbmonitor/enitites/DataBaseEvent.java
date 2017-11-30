package com.betvictor.dbmonitor.enitites;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
@Data
public class DataBaseEvent {
    @NonNull
    private final String tableName;
    @NonNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime timeStamp;
    @NonNull
    private final String rowId;
    @NonNull
    private final DataBaseEventType dataBaseEventType;

    public enum DataBaseEventType {
        INSERT, UPDATE,
    }
}
