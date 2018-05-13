package org.pricelessfestival.crossoff.server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.pricelessfestival.crossoff.server.CrossoffIntegrationTests;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.junit.Assert.*;
import static org.pricelessfestival.crossoff.server.service.GlobalObjectMapper.JACKSON;

/**
 * Created by ivan on 4/26/18.
 */
@SuppressWarnings("unchecked")
@Log4j2
public class ApiTests extends CrossoffIntegrationTests {

    @Test
    public void validScans() throws IOException {
        // create tickets
        addTickets("A","B","C");

        // scan two of them
        ScanResult scanA = scan("A");
        assertTrue(scanA.isAccepted());
        assertNotNull(scanA.getTicket());
        assertTrue(scanA.getMessage().toLowerCase().startsWith("valid"));
        assertTrue(scanA.getTicket().getDescription().toLowerCase().startsWith("generic"));

        // manual scan
        assertTrue(scan("B", true).isAccepted());

        // now two of the tickets have been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("A").getScanned());
        assertNull(tickets.get("A").getManualScan()); // null if regular scan
        assertNotNull(tickets.get("B").getScanned());
        assertTrue(tickets.get("B").getManualScan()); // true if manual scan
        assertNull(tickets.get("C").getScanned());
    }

    @Test
    public void scanUnknownTicket() throws IOException {
        // create tickets
        addTickets("1","2");

        // valid and invalid
        ScanResult scan1 = scan("1");
        assertTrue(scan1.isAccepted());
        assertNotNull(scan1.getTicket());
        assertTrue(scan1.getMessage().toLowerCase().startsWith("valid"));
        ScanResult scan2 = scan("3");
        assertFalse(scan2.isAccepted());
        assertNull(scan2.getTicket());
        assertTrue(scan2.getMessage().toLowerCase().startsWith("invalid ticket"));

        // one ticket has been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("1").getScanned());
        assertNull(tickets.get("2").getScanned());
    }

    @Test
    public void scanTwice() throws IOException {
        // create tickets
        addTickets("1","2");

        // duplicate scan immediately after
        ScanResult scan1 = scan("1");
        assertTrue(scan1.isAccepted());
        assertNotNull(scan1.getTicket());
        assertTrue(scan1.getMessage().toLowerCase().startsWith("valid"));
        scan1 = scan("1");
        assertFalse(scan1.isAccepted());
        assertNotNull(scan1.getTicket());
        assertTrue(scan1.getMessage().toLowerCase().startsWith("already scanned"));
        assertTrue(scan1.getMessage().toLowerCase().contains("0 seconds ago"));

        // and again 1 second later
        try {
            Thread.sleep(999L);
        } catch (InterruptedException e) {
        }
        scan1 = scan("1");
        assertFalse(scan1.isAccepted());
        assertNotNull(scan1.getTicket());
        assertTrue(scan1.getMessage().toLowerCase().startsWith("already scanned"));
        assertTrue(scan1.getMessage().toLowerCase().contains("1 second ago"));

        // one ticket has been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("1").getScanned());
        assertNull(tickets.get("2").getScanned());
    }

    @Test
    public void createTicketCleanDatabase1() throws IOException {
        // verify no tickets to start
        assertEquals(0, getTickets().size());
        // create ticket
        assertEquals(HTTP_OK, addTickets("TESTCLEANUP"));
        // ticket was created
        assertEquals(1, getTickets().size());
    }

    @Test
    public void createTicketCleanDatabase2() throws IOException {
        // if database is not cleaned between tests, either this test or testCreateTicketCleanDatabase1 will fail
        createTicketCleanDatabase1();
    }

    @Test
    public void createFailsWithNoEntity() throws IOException {
        int status = Request.Post(rootUrl + "tickets/").execute().returnResponse().getStatusLine().getStatusCode();
        assertEquals(HTTP_BAD_REQUEST, status);
    }

    @Test
    public void createFailsWithNoTickets() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets());
    }

    @Test
    public void createFailsWithInvalidTicketCode() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets("TICKET1","","TICKET3"));
    }

    @Test
    public void createFailsWIthDuplicateTicketInSubmission() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets("A1","B1","A1","C1"));
    }

    @Test
    public void createFailsWIthDuplicateTicketInDatabase() throws IOException {
        // create some valid tickets
        assertEquals(HTTP_OK, addTickets("A1","B1","C1"));

        // try to create same ticket again
        assertEquals(HTTP_BAD_REQUEST, addTickets("A1"));
    }

    @Test
    public void createFailsWithBlankDescription() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTicket(new Ticket("A", null, "ticketholder", Ticket.TicketType.MOBILE)));
    }

    @Test
    public void createSucceedsWithBlankTicketholder() throws IOException {
        assertEquals(HTTP_OK, addTicket(new Ticket("A", "desc", null, Ticket.TicketType.MOBILE)));
    }

    @Test
    public void createFailsWithBlankTicketType() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTicket(new Ticket("A", "desc", "ticketholder", null)));
    }

    @Test
    public void validExample() throws IOException {
        Map<String, Ticket> exampleTickets = getTicketMap("example", null);
        String[] ticketCodes = new String[exampleTickets.size()];
        assertEquals(HTTP_OK, addTickets(exampleTickets.keySet().toArray(ticketCodes)));
        Map<String, Ticket> tickets = getTickets();
        assertEquals(tickets.keySet(), exampleTickets.keySet());
    }

    @Test
    public void sortByCode() throws IOException {
        Random random = new Random();
        String[] codes = new String[1000];
        for (int i = 0; i < 1000; i++) {
            codes[i] = Long.toString(random.nextLong());
        }
        addTickets(codes);
        List<Ticket> list = getTicketList(null, null);
        assertEquals(1000, list.size());
        ascendingCode(list, false); // unsorted will not be in order
        list = getTicketList(null, "code");
        ascendingCode(list, true); // sorted will be in order
    }

    @Test
    public void getTicketByCode() throws IOException {
        addTickets("A1");
        Ticket ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertEquals("A1", ticket.getCode());
    }

    @Test
    public void updateTicketDescription() throws IOException {
        addTickets("A1");
        Ticket ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getDescription().startsWith("GENERIC"));
        Ticket updateTicket = new Ticket();
        updateTicket.setDescription("new description");
        assertEquals(HTTP_OK, updateTicket("A1", updateTicket));
        ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getDescription().startsWith("new descr"));
    }

    @Test
    public void updateTicketholder() throws IOException {
        addTickets("A1");
        Ticket ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getTicketholder().startsWith("Ticket"));
        Ticket updateTicket = new Ticket();
        updateTicket.setTicketholder("Alice Cooper");
        assertEquals(HTTP_OK, updateTicket("A1", updateTicket));
        ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getTicketholder().startsWith("Alice"));

        // unset
        updateTicket.setTicketholder("");
        assertEquals(HTTP_OK, updateTicket("A1", updateTicket));
        ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertNull(ticket.getTicketholder());
    }

    @Test
    public void updateTicketType() throws IOException {
        addTickets("A1");
        Ticket ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getTicketType().equals(Ticket.TicketType.UNSPECIFIED));
        Ticket updateTicket = new Ticket();
        updateTicket.setTicketType(Ticket.TicketType.PRINT_AT_HOME);
        assertEquals(HTTP_OK, updateTicket("A1", updateTicket));
        ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getTicketType().equals(Ticket.TicketType.PRINT_AT_HOME));
    }

    @Test
    public void updateTicketDescriptionFailsWithNoEntity() throws IOException {
        addTickets("A1");
        assertEquals(HTTP_BAD_REQUEST, Request.Put(rootUrl + "tickets/A1")
                .execute().returnResponse().getStatusLine().getStatusCode());
    }

    @Test
    public void updateTicketNotes() throws IOException {
        addTicket(new Ticket("A", "GA", "Alice", Ticket.TicketType.MOBILE));
        String notes = "CHECK ID";
        addTicket(new Ticket("B", "GA", "Bob", Ticket.TicketType.MOBILE).setNotes(notes));
        Map<String, Ticket> tickets = getTickets();
        assertEquals(notes, tickets.get("B").getNotes());

        // change a note
        notes = "WATCH LIST";
        Ticket updateTicket = new Ticket().setNotes(notes);
        assertEquals(HTTP_OK, updateTicket("B", updateTicket));
        assertEquals(notes, getTickets().get("B").getNotes());

        // add a note
        assertNull(tickets.get("A").getNotes());
        notes = "GIFT BAG #1";
        updateTicket.setNotes(notes);
        assertEquals(HTTP_OK, updateTicket("A", updateTicket));
        tickets = getTickets();
        assertEquals(notes, tickets.get("A").getNotes());
        assertNotNull(tickets.get("B").getNotes());

        // remove note
        updateTicket.setNotes(""); // empty string to unset
        assertEquals(HTTP_OK, updateTicket("B", updateTicket));
        assertNull(getTickets().get("B").getNotes());
    }

    @Test
    public void voidTicket() throws IOException {
        addTickets("A");
        Ticket ticket = getTickets().get("A");
        assertNull(ticket.getVoided());
        // don't do anything to it (unvoid it)
        Ticket updateTicket = new Ticket();
        updateTicket.setVoided(false);
        assertEquals(HTTP_OK, updateTicket("A", updateTicket));
        ticket = getTickets().get("A");
        assertNull(ticket.getVoided());
        // void it
        updateTicket.setVoided(true);
        assertEquals(HTTP_OK, updateTicket("A", updateTicket));
        ticket = getTickets().get("A");
        assertTrue(ticket.getVoided());

        // try to scan it
        ScanResult result = scan("A");
        assertFalse(result.isAccepted());
        assertTrue(result.getMessage().toLowerCase().contains("void"));

        // unvoid it
        updateTicket.setVoided(false);
        assertEquals(HTTP_OK, updateTicket("A", updateTicket));
        ticket = getTickets().get("A");
        assertNull(ticket.getVoided());

        // scan it
        result = scan("A");
        assertTrue(result.isAccepted());
        assertFalse(result.getMessage().toLowerCase().contains("void"));
    }

    @Test
    public void deleteTicket() throws IOException {
        addTickets("A","B");
        scan("A");
        // cannot delete already-scanned A
        assertEquals(HTTP_BAD_REQUEST,
                Request.Delete(rootUrl + "tickets/A").execute().returnResponse().getStatusLine().getStatusCode());
        // successfully delete not-yet-scanned B
        assertEquals(HTTP_NO_CONTENT,
                Request.Delete(rootUrl + "tickets/B").execute().returnResponse().getStatusLine().getStatusCode());
        Map<String, Ticket> tickets = getTickets();
        assertEquals(1, tickets.size());
        assertNotNull(tickets.get("A"));
    }

    @Test
    public void unscan() throws IOException {
        // create tickets and scan
        addTickets("A","B");
        scan("A");
        scan("B");

        // both of the tickets have been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("A").getScanned());
        assertNotNull(tickets.get("B").getScanned());

        // un-scan ticket A
        assertEquals(HTTP_OK, Request.Patch(rootUrl + "tickets/A").execute().returnResponse().getStatusLine().getStatusCode());

        // now only one of the tickets has been scanned
        tickets = getTickets();
        assertNull(tickets.get("A").getScanned());
        assertNotNull(tickets.get("B").getScanned());
    }

    @Test
    public void longTicketCode() throws IOException {
        String str64 = Strings.repeat("A",64);
        String str65 = Strings.repeat("A",65);
        String str256 = Strings.repeat("A",256);

        // ticket code max 64 chars
        assertEquals(HTTP_BAD_REQUEST, addTickets(Strings.repeat("X", 1024 * 1024)));
        assertEquals(HTTP_BAD_REQUEST, addTickets(str256));
        assertEquals(HTTP_BAD_REQUEST, addTickets(str65));
        assertEquals(HTTP_OK, addTickets(str64));
        assertTrue(scan(str64).isAccepted());
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get(str64).getScanned());

        assertFalse(scan(str256).isAccepted()); // long ticket code is normal unaccepted scan
        assertFalse(scan(str65).isAccepted());
    }

    @Test
    public void invalidTicketCode() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets("NO SPACES"));
        assertEquals(HTTP_BAD_REQUEST, addTickets("nolowercase"));
        assertEquals(HTTP_BAD_REQUEST, addTickets("LIMITEDSPECIAL!"));
        assertEquals(HTTP_BAD_REQUEST, addTickets(""));
        assertEquals(HTTP_BAD_REQUEST, addTickets("[this one, definitely bad]"));
    }

    @Test
    public void invalidTicketholder() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTicket(new Ticket("A", "GA", "", Ticket.TicketType.MOBILE)));
        assertEquals(HTTP_BAD_REQUEST, addTicket(new Ticket("B", "GA", Strings.repeat("X", 256), Ticket.TicketType.MOBILE)));
        assertEquals(HTTP_OK, addTicket(new Ticket("A", "GA", Strings.repeat("X", 255), Ticket.TicketType.MOBILE)));

        // update
        Ticket updateTicket = new Ticket().setTicketholder(Strings.repeat("X",256));
        assertEquals(HTTP_BAD_REQUEST, updateTicket("A", updateTicket));
        updateTicket.setTicketholder("Joe");
        assertEquals(HTTP_OK, updateTicket("A", updateTicket));
    }

    @Test
    public void invalidSortColumn() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, Request.Get(rootUrl + "/tickets/?sort=fhgwgads")
                .execute().returnResponse().getStatusLine().getStatusCode());
    }

    private void ascendingCode(List<Ticket> tickets, boolean isAscending) {
        boolean ascending = true;
        String lastCode = "";
        for (int i = 0; i < tickets.size(); i++) {
            String thisCode = tickets.get(i).getCode();
            if (thisCode.compareTo(lastCode) < 0) {
                ascending = false;
            }
            lastCode = thisCode;
        }
        assertEquals(isAscending, ascending);
    }

    private Map<String, Ticket> getTickets() throws IOException {
        return getTicketMap(null, null);
    }

    private Map<String, Ticket> getTicketMap(String listSpec, String orderBy) throws IOException {
        List<Ticket> ticketList = getTicketList(listSpec, orderBy);
        return ticketList.stream().collect(Collectors.toMap(Ticket::getCode, identity()));
    }

    private List<Ticket> getTicketList(String listSpec, String orderBy) throws IOException {
        String url = rootUrl + "tickets/";
        if (listSpec != null) {
            url += listSpec;
        }
        if (orderBy != null) {
            url += "?sort=" + orderBy;
        }
        String ticketList = Request.Get(url).execute().returnContent().asString();
        return JACKSON.readValue(ticketList, new TypeReference<List<Ticket>>() { } );
    }

    private int addTickets(String... codes) throws IOException {
        List<Ticket> tickets = Lists.newArrayList(codes).stream()
                .map(code -> new Ticket(code, "GENERIC TICKET " + code, "Ticket Holder", Ticket.TicketType.UNSPECIFIED))
                .collect(Collectors.toList());
        return addTickets(tickets);
    }

    private int addTickets(List<Ticket> tickets) throws IOException {
        String postTickets = JACKSON.writeValueAsString(tickets);
        return Request.Post(rootUrl + "tickets/").bodyString(postTickets, ContentType.APPLICATION_JSON).execute()
                .returnResponse().getStatusLine().getStatusCode();
    }

    private int addTicket(Ticket ticket) throws IOException {
        String postTickets = JACKSON.writeValueAsString(ImmutableList.of(ticket));
        return Request.Post(rootUrl + "tickets/").bodyString(postTickets, ContentType.APPLICATION_JSON).execute()
                .returnResponse().getStatusLine().getStatusCode();
    }

    private ScanResult scan(String code, boolean manualScan) throws IOException {
        return JACKSON.readValue(Request.Post(rootUrl + "tickets/" + code + (manualScan ? "?manual=true" : "")).execute().returnContent().asString(), ScanResult.class);
    }

    private ScanResult scan(String code) throws IOException {
        return scan(code, false);
    }

    private int updateTicket(String code, Ticket updateTicket) throws IOException {
        return Request.Put(rootUrl + "tickets/" + code)
                .bodyString(JACKSON.writeValueAsString(updateTicket), ContentType.APPLICATION_JSON)
                .execute().returnResponse().getStatusLine().getStatusCode();
    }
}
