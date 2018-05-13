package org.pricelessfestival.crossoff.server.api;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
@Log4j2
public class TransactUnscanTicket extends CrossoffTransaction<Ticket> {
    String code;

    @Override
    public Ticket apply(Session session) {
        Ticket ticket = ticket(session, code);
        ticket.setScanned(null);
        log.info("* UN-SCANNED TICKET: {} {}", ticket.getCode(), ticket.getDescription());
        session.saveOrUpdate(ticket);
        return ticket;
    }
}
