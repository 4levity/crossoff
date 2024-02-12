package org.pricelessfestival.crossoff.server.api;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import jakarta.ws.rs.BadRequestException;
import java.util.List;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
@Log4j2
public class TransactCreateTickets extends CrossoffTransaction<List<Ticket>> {
    List<Ticket> tickets;

    @Override
    public List<Ticket> apply(Session session) {
        if (tickets == null || tickets.size() == 0) {
            log.warn("client tried to add empty ticket list");
            throw new BadRequestException("must provide list of tickets");
        }
        tickets.forEach(ticket -> {
            // ticket code is valid (nonempty)
            if (!Ticket.validTicketCode(ticket.getCode())) {
                log.warn("client tried to add ticket with invalid ticket code");
                throw new BadRequestException("invalid ticket code");
            }
            if (Strings.isNullOrEmpty(ticket.getDescription())) {
                log.warn("client tried to add ticket with empty description");
                throw new BadRequestException("empty description");
            }
            validDescription(ticket);
            validTicketholder(ticket); // null is valid
            // ticket type is valid
            if (ticket.getTicketType() == null) {
                log.warn("client tried to add ticket without specifying ticket type");
                throw new BadRequestException("must specify ticketType value");
            }
            // check for duplicates within submission
            tickets.forEach(ticketMatch -> {
                if (ticketMatch != ticket && ticketMatch.getCode().equals(ticket.getCode())) {
                    log.warn("client tried to add ticket list containing duplicates within itself");
                    throw new BadRequestException("submission contains duplicate ticket code " + ticket.getCode());
                }
            });
            // check for duplicates in database
            Ticket duplicateTicket = session.bySimpleNaturalId(Ticket.class).load(ticket.getCode());
            if (duplicateTicket != null) {
                log.warn("client tried to add ticket list containing tickets already in database");
                throw new BadRequestException("preexisting duplicate ticket code " + ticket.getCode());
            }
        });
        // save tickets
        tickets.forEach(ticket -> {
            Ticket newTicket = new Ticket(
                    ticket.getCode(),
                    ticket.getDescription(),
                    ticket.getTicketholder(),
                    ticket.getTicketType(),
                    ticket.getNotes());
            session.saveOrUpdate(newTicket);
        });
        log.info("ADDED {} tickets", tickets.size());
        return tickets;
    }
}
