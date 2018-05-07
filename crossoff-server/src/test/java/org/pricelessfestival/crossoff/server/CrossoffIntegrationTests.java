package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.junit.After;
import org.junit.Before;

/**
 * Created by ivan on 5/6/18.
 */
@Log4j2
public class CrossoffIntegrationTests extends CrossoffTests {

    static int HTTP_OK = 200;
    static int HTTP_BAD_REQUEST = 400;
    String rootUrl = "http://localhost:" + WebServer.PORT + "/";

    @Before
    public void setupDatabase() {
        if (!Persistence.isReady()) {
            Persistence.init("test.hibernate.cfg.xml");
            try {
                new WebServer().start();
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
