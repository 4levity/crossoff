package org.pricelessfestival.crossoff.server.api;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
@Log4j2
public class TransactDeleteTicket extends CrossoffTransaction<Response> {
    String code;

    @Override
    public Response apply(Session session) {
        Ticket ticket = ticket(session, code);
        if (ticket.getScanned() != null) {
            log.warn("tried to delete ticket {} that was already scanned", ticket.getCode());
            throw new BadRequestException("cannot delete a ticket that has a scan timestamp");
        }
        session.remove(ticket);
        return Response.noContent().build();
    }
}
