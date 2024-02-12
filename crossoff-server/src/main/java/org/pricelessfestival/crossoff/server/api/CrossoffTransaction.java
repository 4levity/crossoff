package org.pricelessfestival.crossoff.server.api;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.pricelessfestival.crossoff.server.service.Transaction;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Created by ivan on 5/12/18.
 */
@Log4j2
public abstract class CrossoffTransaction<R> implements Transaction<R> {

    Ticket ticket(Session session, String code) {
        Ticket ticket = session.bySimpleNaturalId(Ticket.class).load(code);
        if (ticket == null) {
            throw new NotFoundException();
        }
        return ticket;
    }

    void validDescription(Ticket ticket) {
        if (!Ticket.validDescription(ticket.getDescription())) {
            log.warn("invalid or missing value for description");
            throw new BadRequestException("invalid or missing value for description");
        }
    }

    void validTicketholder(Ticket ticket) {
        if (!Ticket.validTicketholder(ticket.getTicketholder())) {
            log.warn("invalid value for ticketholder");
            throw new BadRequestException("invalid value for ticketholder");
        }
    }

    void validNotes(Ticket ticket) {
        if (!Ticket.validNotes(ticket.getNotes())) {
            log.warn("invalid value for notes");
            throw new BadRequestException("invalid value for notes");
        }
    }
}
