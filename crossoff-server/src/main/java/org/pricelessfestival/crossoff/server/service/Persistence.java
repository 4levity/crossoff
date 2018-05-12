package org.pricelessfestival.crossoff.server.service;

import lombok.extern.log4j.Log4j2;
import org.h2.tools.Server;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * Created by ivan on 4/26/18.
 */
@Log4j2
public class Persistence {

    private static SessionFactory sessionFactory;

    public static boolean isReady() {
        return sessionFactory != null;
    }

    public static void init(String hibernateConfigFile, boolean startH2WebServer) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure(hibernateConfigFile).build();
        sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        log.info("Persistence ready, JDBC URL = {}", getJdbcUrl());
        if (startH2WebServer) {
            try {
                Server.createWebServer().start();
                log.info("H2 database web interface at http://localhost:8082/ (local access only)");
            } catch (SQLException e) {
                log.warn("failed to start H2 embedded webserver at http://localhost:8082/", e);
            }
        }
    }

    public static <T> T exec(Function<Session, T> operation) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        T result = null;
        boolean completed = false;
        try {
            result = operation.apply(session);
            completed = true;
        } finally {
            finish(completed, session);
        }
        return result;
    }

    private static void finish(boolean completed, Session session) {
        boolean rollback = !completed;
        try {
            Transaction transaction = session.getTransaction();
            if (transaction == null || !transaction.isActive()) {
                log.error("finish() called with no active Transaction", new Exception());
            }
            if (rollback) {
                log.warn("errors occurred during transaction, rolling back");
            } else {
                try {
                    session.getTransaction().commit();
                } catch (RuntimeException e) {
                    log.error("commit failed, switching to rollback", e);
                    rollback = true;
                }
            }
        } finally {
            if (rollback) {
                try {
                    session.getTransaction().rollback();
                } catch (RuntimeException e) {
                    log.error("rollback failed", e);
                }
            }
            session.close();
        }
    }

    private static String getJdbcUrl() {
        return exec(session -> {
            String[] url = new String[1];
            session.doWork(connection -> url[0] = connection.getMetaData().getURL());
            return url[0];
        });
    }
}
