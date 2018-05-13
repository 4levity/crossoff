package org.pricelessfestival.crossoff.server.api;

import lombok.AllArgsConstructor;
import org.hibernate.Session;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
public class TransactGetTicket extends CrossoffTransaction<Ticket> {
    String code;

    @Override
    public Ticket apply(Session session) {
        return ticket(session, code);
    }
}
