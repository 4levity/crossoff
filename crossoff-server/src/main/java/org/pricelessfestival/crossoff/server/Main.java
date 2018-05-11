package org.pricelessfestival.crossoff.server;

import org.pricelessfestival.crossoff.server.service.CrossoffWebServer;
import org.pricelessfestival.crossoff.server.service.Persistence;

/**
 * Created by ivan on 4/26/18.
 */
public class Main {

    private static final int WEBSERVER_PORT = 8080;

    public static void main(String... args) throws Exception {
        Persistence.init("hibernate.cfg.xml", true);
        new CrossoffWebServer(WEBSERVER_PORT).start();
    }
}
