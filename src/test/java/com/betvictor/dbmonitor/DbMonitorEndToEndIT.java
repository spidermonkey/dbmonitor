package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.helpers.DataBaseHelper;
import com.betvictor.dbmonitor.helpers.EndToEndTest;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest(classes = {DbMonitorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Category(EndToEndTest.class)
public class DbMonitorEndToEndIT {

    @LocalServerPort
    private int port;

    private WebClient webClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Before
    public void cleanDBAndInitWebSocket() {
        DataBaseHelper.truncateTables(jdbcTemplate);
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setHTMLParserListener(HTMLParserListener.LOG_REPORTER);
    }

    @After
    public void shutDown() {
        webClient.close();
    }

    @Test(timeout = 60000)
    public void shouldRenderDatabaseChanges() throws IOException {
        Page page = webClient.getPage("http://localhost:" + port + "/");
        waitUntilWebSocketIsConnected((HtmlPage) page);
        DataBaseHelper.insertIntoTableUnderMonitor(jdbcTemplate, "test1", "test2", "test3");
        waitUntilRowCountInTable((HtmlPage) page,2 );
        assertThat(((HtmlPage) page).asText()).contains("test1");
    }

    private void waitUntilRowCountInTable(HtmlPage page, int rowCount) {
        await("wait until RowCount")
                .atMost(20, SECONDS)
                .until(() -> ((HtmlTable) page.getElementById("conversation")).getRowCount(), equalTo(rowCount));

    }
    private void waitUntilWebSocketIsConnected(HtmlPage page) {
        await("wait for WebSocket connection")
                .atMost(20, SECONDS)
                .until(() -> page.getElementById("connected").getTextContent(), equalTo("WebSocket connection established!"));
    }


}
