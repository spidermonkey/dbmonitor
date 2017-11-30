package com.betvictor.dbmonitor.config;

import com.betvictor.dbmonitor.AuditTrailEntityToDbChangeEventTransformer;
import com.betvictor.dbmonitor.db.AuditTrailEntityRowMapper;
import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.sql.DataSource;

@Configuration
@EnableIntegration
@Slf4j
@EnableConfigurationProperties(DbMonitorProperties.class)
public class IntegrationFlowConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SimpMessagingTemplate broker;

    @Autowired
    private DbMonitorProperties dbMonitorProperties;

    @Autowired
    private AuditTrailEntityRowMapper auditTrailEntityRowMapper;

    private static final String POLLING_QUERY = "SELECT * FROM AUDIT_TRAIL WHERE NOTIFIED = FALSE ORDER BY TIMESTAMP";

    public static final String UPDATE_QUERY = "UPDATE AUDIT_TRAIL SET NOTIFIED=TRUE WHERE EVENT_ID IN (:eventId)";


    @Bean
    public AuditTrailEntityRowMapper auditTrailEntityRowMapper() {
        return new AuditTrailEntityRowMapper();
    }

    @Bean
    public MessageSource<Object> jdbcMessageSource() {
        JdbcPollingChannelAdapter jdbcPollingChannelAdapter = new JdbcPollingChannelAdapter(this.dataSource, POLLING_QUERY);
        jdbcPollingChannelAdapter.setUpdateSql(UPDATE_QUERY);
        jdbcPollingChannelAdapter.setUpdatePerRow(false);
        jdbcPollingChannelAdapter.setRowMapper(auditTrailEntityRowMapper);
        return jdbcPollingChannelAdapter;
    }

    @Bean
    public AuditTrailEntityToDbChangeEventTransformer auditTrailEntityToDbChangeEventTransformer() {
        return new AuditTrailEntityToDbChangeEventTransformer();
    }

    @Bean
    public IntegrationFlow dbToWebSocketFlow(AuditTrailEntityToDbChangeEventTransformer auditTrailEntityToDbChangeEventTransformer) {
        return IntegrationFlows.from(this.jdbcMessageSource(), c ->
                c.poller(Pollers
                        .fixedRate(dbMonitorProperties.getPollRate())
                        .transactional() //this is important: making database transaction fail if websocket is closed
                        .maxMessagesPerPoll(dbMonitorProperties.getMaxMessagesPerPoll())))
                .split()
                .transform(auditTrailEntityToDbChangeEventTransformer)
                .handle(message ->
                {
                    broker.convertAndSend(dbMonitorProperties.getWebSocketPrefix() + dbMonitorProperties.getDbEventTopic(), message.getPayload());
                    log.info("Database event with id {} successfully sent to WebSocket destination", ((DataBaseEvent) message.getPayload()).getRowId());
                })
                .get();
    }

}