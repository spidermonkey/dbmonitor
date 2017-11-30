package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import com.betvictor.dbmonitor.helpers.DataBaseHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DbMonitorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DbMonitorApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SockJsClient sockJsClient;

    private WebSocketStompClient stompClient;

    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();


    @Before
    public void cleanDBAndInitWebSocket() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        this.sockJsClient = new SockJsClient(transports);
        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        DataBaseHelper.truncateTables(jdbcTemplate);
    }

    @Test
	public void shouldApplicationStartUp() {
		//the application starts up because of the @SpringBootTest annotation
	}

    @Test
    public void shouldTableExistInTheDatabaseAfterStartup() {
        jdbcTemplate.execute("SELECT * FROM SOME_TABLE");
    }

    @Test
    public void shouldAuditTrailTableExistAfterStartup() {
        jdbcTemplate.execute("SELECT * FROM AUDIT_TRAIL");
    }

    @Test
    public void shouldMessageBeSentToWebSocket() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/dbInsertNotifications", new StompSessionHandlerAdapter() {
                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return DataBaseEvent.class;
                    }
                });
            }
        };
        this.stompClient.connect("ws://localhost:{port}/betvictor", this.headers, handler, this.port);
        DataBaseHelper.insertIntoTableUnderMonitor(jdbcTemplate, "Test1", "Test2", "Test3");
        boolean latchResult = countDownLatch.await(10, TimeUnit.SECONDS);
        //make sure that the latch terminated without errors
        assertThat(latchResult).isEqualTo(true);
    }

}
