package org.pricelessfestival.crossoff.server.service;

import org.junit.Test;
import org.pricelessfestival.crossoff.server.CrossoffIntegrationTests;
import org.pricelessfestival.crossoff.server.api.Ticket;

import static org.junit.Assert.*;

/**
 * Created by ivan on 5/12/18.
 */
public class PersistenceTests extends CrossoffIntegrationTests {

    public static class TestException extends RuntimeException {
    }

    @Test
    public void rollbackDuringTransaction() {
        // make a change
        Persistence.exec(session -> {
            Ticket ticket = new Ticket("A", "desc", null, Ticket.TicketType.MOBILE);
            session.saveOrUpdate(ticket);
            return null;
        });
        // make a change but then roll back
        boolean threw = false;
        try {
            Persistence.exec(session -> {
                Ticket ticket = new Ticket("B", "desc", null, Ticket.TicketType.MOBILE);
                session.saveOrUpdate(ticket);
                throw new TestException();
            });
        } catch (TestException e) {
            threw = true;
        }
        assertTrue(threw);
        // 2nd change wasn't made
        Persistence.exec(session -> {
            assertNotNull(session.bySimpleNaturalId(Ticket.class).load("A"));
            assertNull(session.bySimpleNaturalId(Ticket.class).load("B"));
            return null;
        });
    }
}
