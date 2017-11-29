package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.config.WebSocketConfig;
import com.betvictor.dbmonitor.db.AuditTrailEntityRowMapper;
import com.betvictor.dbmonitor.enitites.AuditTrailEntity;
import com.betvictor.dbmonitor.enitites.DataBaseEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

        jdbcTemplate.execute("TRUNCATE TABLE SOME_TABLE_AUDIT_TRAIL");
        jdbcTemplate.execute("TRUNCATE TABLE SOME_TABLE");
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
        jdbcTemplate.execute("SELECT * FROM SOME_TABLE_AUDIT_TRAIL");
    }

    @Test
    public void shouldTriggerExecutedInCaseOfInsert() throws InterruptedException {
        insertIntoTableUnderMonitor("Test1", "Test2", "Test3");
        Thread.sleep(100); //makes sense to wait until the trigger
        List<AuditTrailEntity> result = jdbcTemplate.query("SELECT * FROM SOME_TABLE_AUDIT_TRAIL", new AuditTrailEntityRowMapper());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangeType()).isEqualTo("INSERT");
    }

    @Test
    public void shouldTriggerExecutedInCaseOfUpdate() throws InterruptedException {
        insertIntoTableUnderMonitor("Test1", "Test2", "Test3");
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE SOME_TABLE SET some_attribute_1= ? WHERE ID=?;");
            preparedStatement.setString(1, "updated_value");
            preparedStatement.setString(2, "Test1");
            return preparedStatement;
        });
        Thread.sleep(100); //makes sense to wait until the trigger
        List<AuditTrailEntity> result = jdbcTemplate.query("SELECT * FROM SOME_TABLE_AUDIT_TRAIL", new AuditTrailEntityRowMapper());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getChangeType()).isEqualTo("INSERT");
        assertThat(result.get(1).getChangeType()).isEqualTo("UPDATE");
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

                    @Override
                    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                        super.handleException(session, command, headers, payload, exception);
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        super.handleTransportError(session, exception);
                    }
                });
            }
        };
        this.stompClient.connect("ws://localhost:{port}/betvictor", this.headers, handler, this.port);
        Thread.sleep(1000);
        insertIntoTableUnderMonitor("Test1", "Test2", "Test3");
        countDownLatch.await();
    }


    private void insertIntoTableUnderMonitor(final String id, final String attribute1, final String attribute2) {
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO SOME_TABLE(id, some_attribute_1, some_attribute_2) VALUES (?, ?, ?);");
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, attribute1);
            preparedStatement.setString(3, attribute2);
            return preparedStatement;
        });
    }


}
