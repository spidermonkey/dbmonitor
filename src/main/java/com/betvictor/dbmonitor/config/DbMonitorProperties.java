package com.betvictor.dbmonitor.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("betvictor.dbmonitor")
@Data
@Getter
public class DbMonitorProperties {
    private String version;
    private long pollRate;
    private long maxMessagesPerPoll;
    private String webSocketEndPoint;
    private String webSocketPrefix;
    private String dbEventTopic;
}
