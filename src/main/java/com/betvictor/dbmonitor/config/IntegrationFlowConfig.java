package com.betvictor.dbmonitor.config;

import com.betvictor.dbmonitor.AuditTrailEntityToDbChangeEventTransformer;
import com.betvictor.dbmonitor.db.AuditTrailEntityRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class IntegrationFlowConfig {

    @Autowired
    public DataSource dataSource;

    @Autowired
    private SimpMessagingTemplate webSocket;

    @Bean
    public MessageSource<Object> jdbcMessageSource() {
        JdbcPollingChannelAdapter jdbcPollingChannelAdapter = new JdbcPollingChannelAdapter(this.dataSource, "SELECT * FROM SOME_TABLE_AUDIT_TRAIL WHERE NOTIFIED = FALSE");
        jdbcPollingChannelAdapter.setUpdateSql("UPDATE SOME_TABLE_AUDIT_TRAIL SET NOTIFIED=TRUE WHERE ID IN (:id);");
        jdbcPollingChannelAdapter.setUpdatePerRow(true);
        jdbcPollingChannelAdapter.setRowMapper(new AuditTrailEntityRowMapper());
        return jdbcPollingChannelAdapter;
    }

    @Bean
    public IntegrationFlow dbToWebSocketFlow() {
        return IntegrationFlows.from(this.jdbcMessageSource(), c ->
                c.poller(Pollers
                        .fixedRate(1000)
                        .maxMessagesPerPoll(10)))
                .split()
                .transform(new AuditTrailEntityToDbChangeEventTransformer())
                .handle(message ->
                {
                    webSocket.convertAndSend("/topic/dbInsertNotifications", message.getPayload());
                    log.info("Database event successfully sent to WebSocket destination" );
                })
                .get();
    }

}