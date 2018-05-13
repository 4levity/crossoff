package org.pricelessfestival.crossoff.server.api;

import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.pricelessfestival.crossoff.server.service.Persistence;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        return Persistence.exec(new TransactGetAllTickets(orderBy));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> createTickets(List<Ticket> tickets) {
        return Persistence.exec(new TransactCreateTickets(tickets));
    }

    @GET
    @Path("{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket getTicketByCode(@PathParam("code") String code) {
        return Persistence.exec(new TransactGetTicket(code));
    }

    @DELETE
    @Path("{code}")
    public Response deleteTicketByCode(@PathParam("code") String code) {
        return Persistence.exec(new TransactDeleteTicket(code));
    }

    @PUT
    @Path("{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket updateTicket(@PathParam("code") String code, Ticket updateTicket) {
        return Persistence.exec(new TransactUpdateTicket(code, updateTicket));
    }

    @PATCH // does not require POST body/entity
    @Path("{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Ticket unscanTicket(@PathParam("code") String code) {
        return Persistence.exec(new TransactUnscanTicket(code));
    }

    @GET
    @Path("example")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ticket> getExampleTicketList() {
        return Lists.newArrayList(
                new Ticket("VWKUMCJEUQ", "Test Event", "Johnny Fakename", Ticket.TicketType.PHYSICAL_MAILED),
                new Ticket("9780465026562", "ISBN Gödel, Escher, Bach", null, Ticket.TicketType.UNSPECIFIED),
                new Ticket("9780399563829", "ISBN Soonish", null, Ticket.TicketType.PRINT_AT_HOME)
        );
    }

    @POST // does not require POST body/entity
    @Path("{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ScanResult scanTicket(@PathParam("code") String code, @QueryParam("manual") @DefaultValue("false") Boolean manualScan) {
        return Persistence.exec(new TransactScanTicket(code, manualScan));
    }

}
