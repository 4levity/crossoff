package org.pricelessfestival.crossoff.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Created by ivan on 4/26/18.
 */
@Log4j2
@Path("/")
public class TicketsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> getAllTickets(@QueryParam("sort") String orderBy) {
        return Persistence.exec(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> ticketQuery = criteriaBuilder.createQuery(Ticket.class);
            Root<Ticket> root = ticketQuery.from(Ticket.class);
            if (orderBy != null) {
                try {
                    ticketQuery.orderBy(criteriaBuilder.asc(root.get(orderBy)));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("cannot order by unknown column: " + orderBy);
                }
            }
            return session.createQuery(ticketQuery).getResultList();
        });
    }

    @POST
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
                if (!Ticket.validTicketCode(ticket.getCode())) {
                    log.warn("client tried to add ticket with invalid ticket code: " + ticket.getCode());
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
                Ticket newTicket = new Ticket(ticket.getCode(), ticket.getDescription(), ticket.getTicketholder());
                session.saveOrUpdate(newTicket);
            });
            log.info("ADDED {} tickets", tickets.size());
            return tickets;
        });
    }

    @GET
    @Path("{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket getTicketByCode(@PathParam("code") String code) {
        return Persistence.exec(session -> ticket(session, code));
    }

    @PUT
    @Path("{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket updateTicket(@PathParam("code") String code, Ticket updateTicket) {
        if (updateTicket == null) {
            throw new BadRequestException("no body");
        }
        return Persistence.exec(session -> {
            Ticket ticket = ticket(session, code);
            boolean modified = false;
            if (!Strings.isNullOrEmpty(updateTicket.getDescription())
                    && !updateTicket.getDescription().equals(ticket.getDescription())) {
                ticket.setDescription(updateTicket.getDescription()); // update description
                modified = true;
            }
            if (updateTicket.getTicketholder() != null && updateTicket.getTicketholder().isEmpty()
                    && ticket.getTicketholder() != null) {
                ticket.setTicketholder(null); // empty string = unset (set to null)
                modified = true;
            } else if (updateTicket.getTicketholder() != null
                    && (ticket.getTicketholder() == null || !updateTicket.getTicketholder().equals(ticket.getTicketholder()))) {
                ticket.setTicketholder(updateTicket.getTicketholder()); // set or change ticketholder name
                modified = true;
            }
            if (modified) {
                log.info("* UPDATED TICKET DETAILS: {} {} / {}", ticket.getCode(), ticket.getTicketholder(), ticket.getDescription());
                session.saveOrUpdate(ticket);
            }
            return ticket;
        });
    }

    @PATCH // does not require POST body/entity
    @Path("{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket unscanTicket(@PathParam("code") String code) {
        return Persistence.exec(session -> {
            Ticket ticket = ticket(session, code);
            ticket.setScanned(null);
            log.info("* UN-SCANNED TICKET: {} {}", ticket.getCode(), ticket.getDescription());
            session.saveOrUpdate(ticket);
            return ticket;
        });
    }

    @GET
    @Path("example")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> getExampleTicketList() {
        return Lists.newArrayList(
                new Ticket("VWKUMCJEUQ", "Test Event", "Johnny Fakename"),
                new Ticket("9780465026562", "ISBN Gödel, Escher, Bach", null),
                new Ticket("9780399563829", "ISBN Soonish", null)
        );
    }

    @POST // does not require POST body/entity
    @Path("{code}")
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
                String scannedAt = TimeUtil.formatTimestamp(ticket.getScanned(), ZoneId.systemDefault());
                String interval = TimeUtil.formatDuration(Duration.between(ticket.getScanned(), Instant.now()));
                log.warn("* DUPLICATE SCAN: {} (scanned {} ago, {})", ticket.getCode(), interval, scannedAt);
                result = new ScanResult(false, "Already scanned " + interval + " ago, " + scannedAt, ticket);
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

    private Ticket ticket(Session session, String code) {
        Ticket ticket = session.bySimpleNaturalId(Ticket.class).load(code);
        if (ticket == null) {
            throw new NotFoundException();
        }
        return ticket;
    }
}
