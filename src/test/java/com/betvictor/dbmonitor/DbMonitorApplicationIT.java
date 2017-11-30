package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.db.AuditTrailEntityRowMapper;
import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import com.betvictor.dbmonitor.helpers.DataBaseHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
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

    @Autowired
    private TestRestTemplate restTemplate;

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
    public void shouldHealthEndpointRespond() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("OK");
    }

    @Test
    public void shouldVersionEndpointRespond() {
        ResponseEntity<String> response = restTemplate.getForEntity("/version", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("0.0.1-SNAPSHOT");
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
                        assertThat(payload).isInstanceOf(DataBaseEvent.class);
                        DataBaseEvent dbEvent = (DataBaseEvent) payload;
                        assertThat(dbEvent.getTableName()).isEqualTo("SOME_TABLE");
                        assertThat(dbEvent.getRowId()).isEqualTo("Test1");
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
        boolean latchResult = countDownLatch.await(20, TimeUnit.SECONDS);
        //make sure that the latch terminated without errors
        assertThat(latchResult).isEqualTo(true);
        //check that the table is updated
        List<AuditTrailEntity> result = jdbcTemplate.query("SELECT * FROM AUDIT_TRAIL", new AuditTrailEntityRowMapper());
        assertThat(result.get(0).isNotified()).isEqualTo(true);
    }

}
