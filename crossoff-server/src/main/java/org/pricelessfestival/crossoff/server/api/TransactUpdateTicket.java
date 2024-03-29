package org.pricelessfestival.crossoff.server.api;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import jakarta.ws.rs.BadRequestException;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
@Log4j2
public class TransactUpdateTicket extends CrossoffTransaction<Ticket> {

    String code;
    Ticket updateTicket;

    @Override
    public Ticket apply(Session session) {
        if (updateTicket == null) {
            log.warn("update ticket request had no body");
            throw new BadRequestException("no body");
        }
        Ticket ticket = ticket(session, code);
        boolean modified = false;
        // update description
        if (!Strings.isNullOrEmpty(updateTicket.getDescription())
                && !updateTicket.getDescription().equals(ticket.getDescription())) {
            validDescription(ticket);
            ticket.setDescription(updateTicket.getDescription()); // update description
            modified = true;
        }
        // remove, set or change ticketholder
        if (updateTicket.getTicketholder() != null && updateTicket.getTicketholder().isEmpty()
                && ticket.getTicketholder() != null) {
            ticket.setTicketholder(null); // empty string = unset (set to null)
            modified = true;
        } else if (updateTicket.getTicketholder() != null
                && (ticket.getTicketholder() == null || !updateTicket.getTicketholder().equals(ticket.getTicketholder()))) {
            validTicketholder(updateTicket);
            ticket.setTicketholder(updateTicket.getTicketholder());
            modified = true;
        }
        // change ticket type
        if (updateTicket.getTicketType() != null
                && (ticket.getTicketType() == null || !updateTicket.getTicketType().equals(ticket.getTicketType()))) {
            ticket.setTicketType(updateTicket.getTicketType());
            modified = true;
        }
        // void or un-void
        if (updateTicket.getVoided() != null && (
                (updateTicket.getVoided() && (ticket.getVoided() == null || !ticket.getVoided()))
                        || (!updateTicket.getVoided() && ticket.getVoided() != null && ticket.getVoided()))) {
            ticket.setVoided(updateTicket.getVoided() ? true : null);
            modified = true;
        }
        // remove, set or change nodes
        if (updateTicket.getNotes() != null && updateTicket.getNotes().isEmpty()
                && ticket.getNotes() != null) {
            ticket.setNotes(null); // empty string = unset (set to null)
            modified = true;
        } else if (updateTicket.getNotes() != null
                && (ticket.getNotes() == null || !updateTicket.getNotes().equals(ticket.getNotes()))) {
            validNotes(updateTicket);
            ticket.setNotes(updateTicket.getNotes());
            modified = true;
        }
        // save changes
        if (modified) {
            log.info("* UPDATED TICKET DETAILS: {} {} / {}", ticket.getCode(), ticket.getTicketholder(), ticket.getDescription());
            session.merge(ticket);
        }
        return ticket;
    }
}
