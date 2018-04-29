package org.pricelessfestival.crossoff.server;

/**
 * Created by ivan on 4/26/18.
 */
public class Main {

    public static void main(String... args) throws Exception {
        Persistence.init("hibernate.cfg.xml");
        new WebServer().start();
    }
}
