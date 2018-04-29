package org.pricelessfestival.crossoff.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

import javax.persistence.criteria.CriteriaQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivan on 4/26/18.
 */
@Log4j2
@Path("/")
public class RootResource {

    @GET
    public String getRoot() {
        return this.getClass().getCanonicalName();
    }

    @GET
    @Path("tickets")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> getAllTickets() {
        return Persistence.exec(session -> {
            CriteriaQuery<Ticket> ticketQuery = session.getCriteriaBuilder().createQuery(Ticket.class);
            ticketQuery.from(Ticket.class);
            return session.createQuery(ticketQuery).getResultList();
        });
    }

    @GET
    @Path("tickets/example")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> getExampleTicketList() {
        return Lists.newArrayList(
                new Ticket("VWKUMCJEUQ", "Test Event / Johnny Fakename"),
                new Ticket("9780399563829", "Gödel, Escher, Bach")
        );
    }

    @POST
    @Path("tickets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> createTickets(List<Ticket> tickets) {
        return Persistence.exec(session -> {
            if (tickets == null || tickets.size() == 0) {
                log.warn("client tried to add empty ticket list");
                throw new BadRequestException("must provide list of tickets");
            }
            tickets.forEach(ticket -> {
                // ticket code is valid (nonempty)
                if (Strings.isNullOrEmpty(ticket.getCode())) {
                    log.warn("client tried to add ticket with invalid ticket code");
                    throw new BadRequestException("invalid ticket code");
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
                Ticket newTicket = new Ticket(ticket.getCode(), ticket.getDescription());
                session.saveOrUpdate(newTicket);
            });
            log.info("ADDED {} tickets", tickets.size());
            return tickets;
        });
    }

    @POST // does not require POST body/entity
    @Path("tickets/{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ScanResult scanTicket(@PathParam("code") String code) {
        return Persistence.exec(session -> {
            ScanResult result;
            Ticket ticket = session.bySimpleNaturalId(Ticket.class).load(code);
            if (ticket == null) {
                // code is not found in database
                log.warn("* UNKNOWN TICKET: {}", code);
                result = new ScanResult(false, "Invalid ticket code: " + code, null);
            } else if (ticket.getScanned() != null) {
                // ticket was already scanned
                DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.LONG )
                                .withLocale( Locale.US ).withZone( ZoneId.systemDefault() );
                String scannedAt = formatter.format(ticket.getScanned());
                log.warn("* DUPLICATE SCAN: {} (previously scanned at {})", ticket.getCode(), scannedAt);
                result = new ScanResult(false, "Already scanned at " + scannedAt, ticket);
            } else {
                // successfully validated
                ticket.setScanned(Instant.now());
                session.saveOrUpdate(ticket);
                log.info("* SCANNED VALID TICKET: {} {}", ticket.getCode(), ticket.getDescription());
                result = new ScanResult(true, "Valid Ticket " + ticket.getCode(), ticket);
            }
            return result;
        });
    }
}
