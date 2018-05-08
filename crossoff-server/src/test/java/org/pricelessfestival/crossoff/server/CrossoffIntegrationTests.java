package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.junit.After;
import org.junit.Before;

import javax.ws.rs.core.Response;

/**
 * Created by ivan on 5/6/18.
 */
@Log4j2
public abstract class CrossoffIntegrationTests extends CrossoffTests {

    static int HTTP_OK = Response.Status.OK.getStatusCode();
    static int HTTP_NO_CONTENT = Response.Status.NO_CONTENT.getStatusCode();
    static int HTTP_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();

    private static int TEST_WEBSERVER_PORT = 8081;
    String rootUrl = "http://localhost:" + TEST_WEBSERVER_PORT + "/";

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
