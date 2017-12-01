package com.betvictor.dbmonitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(DbMonitorProperties.class)
@Slf4j
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    private DbMonitorProperties dbMonitorProperties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("Configuring web socket broker: {}", dbMonitorProperties.getWebSocketPrefix());
        config.enableSimpleBroker(dbMonitorProperties.getWebSocketPrefix());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering endpoint: {}", dbMonitorProperties.getWebSocketEndPoint());
        registry.addEndpoint(dbMonitorProperties.getWebSocketEndPoint()).withSockJS();
    }

}