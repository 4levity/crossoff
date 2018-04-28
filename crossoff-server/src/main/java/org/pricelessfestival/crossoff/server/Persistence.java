package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

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

    public static void init(String hibernateConfigFile) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure(hibernateConfigFile).build();
        sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        log.info("Persistence ready");
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
        try {
            Transaction transaction = session.getTransaction();
            if (transaction == null || !transaction.isActive()) {
                log.error("finish() called with no active Transaction", new Exception());
            }
            if (completed) {
                session.getTransaction().commit();
            } else {
                session.getTransaction().rollback();
            }
        } finally {
            session.close();
        }
    }
}
