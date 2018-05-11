package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.junit.After;
import org.junit.Before;
import org.pricelessfestival.crossoff.server.service.CrossoffWebServer;
import org.pricelessfestival.crossoff.server.service.Persistence;

import javax.ws.rs.core.Response;

/**
 * Created by ivan on 5/6/18.
 */
@Log4j2
public abstract class CrossoffIntegrationTests extends CrossoffTests {

    public static final int HTTP_OK = Response.Status.OK.getStatusCode();
    public static final int HTTP_NO_CONTENT = Response.Status.NO_CONTENT.getStatusCode();
    public static final int HTTP_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();

    private static final int TEST_WEBSERVER_PORT = 8081;
    public static final String rootUrl = "http://localhost:" + TEST_WEBSERVER_PORT + "/";

    @Before
    public void setupDatabase() {
        if (!Persistence.isReady()) {
            Persistence.init("test.hibernate.cfg.xml", false);
            try {
                new CrossoffWebServer(TEST_WEBSERVER_PORT).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @After
    public void resetDatabase() {
        log.debug("DELETING all tickets after test");
        Persistence.exec(session -> session.createQuery("delete from Ticket").executeUpdate());
    }
}
