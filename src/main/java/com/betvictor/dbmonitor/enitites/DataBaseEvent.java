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
    public final String tableName;
    @NonNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public final LocalDateTime timeStamp;
    @NonNull
    public final String rowId;
    @NonNull
    public final DataBaseEventType dataBaseEventType;

    public enum DataBaseEventType {
        INSERT, UPDATE,
    }
}
