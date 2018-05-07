package org.pricelessfestival.crossoff.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.junit.Assert.*;
import static org.pricelessfestival.crossoff.server.GlobalObjectMapper.JACKSON;

/**
 * Created by ivan on 4/26/18.
 */
@SuppressWarnings("unchecked")
@Log4j2
public class ApiTests extends CrossoffIntegrationTests {

    @Test
    public void testValidScans() throws IOException {
        // create tickets
        addTickets("A","B","C");

        // scan two of them
        ScanResult scanA = scan("A");
        assertTrue(scanA.isAccepted());
        assertNotNull(scanA.getTicket());
        assertTrue(scanA.getMessage().toLowerCase().startsWith("valid"));
        assertTrue(scanA.getTicket().getDescription().toLowerCase().startsWith("generic"));

        assertTrue(scan("B").isAccepted());

        // now two of the tickets have been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("A").getScanned());
        assertNotNull(tickets.get("B").getScanned());
        assertNull(tickets.get("C").getScanned());
    }

    @Test
    public void testScanUnknownTicket() throws IOException {
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
    public void testScanTwice() throws IOException {
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
    public void testCreateTicketCleanDatabase1() throws IOException {
        // verify no tickets to start
        assertEquals(0, getTickets().size());
        // create ticket
        assertEquals(HTTP_OK, addTickets("TESTCLEANUP"));
        // ticket was created
        assertEquals(1, getTickets().size());
    }

    @Test
    public void testCreateTicketCleanDatabase2() throws IOException {
        // if database is not cleaned between tests, either this test or testCreateTicketCleanDatabase1 will fail
        testCreateTicketCleanDatabase1();
    }

    @Test
    public void testCreateFailsWithNoEntity() throws IOException {
        int status = Request.Post(rootUrl + "tickets/").execute().returnResponse().getStatusLine().getStatusCode();
        assertEquals(HTTP_BAD_REQUEST, status);
    }

    @Test
    public void testCreateFailsWithNoTickets() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets());
    }

    @Test
    public void testCreateFailsWithInvalidTicketCode() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets("TICKET1","","TICKET3"));
    }

    @Test
    public void testCreateFailsWIthDuplicateTicketInSubmission() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets("A1","B1","A1","C1"));
    }

    @Test
    public void testCreateFailsWIthDuplicateTicketInDatabase() throws IOException {
        // create some valid tickets
        assertEquals(HTTP_OK, addTickets("A1","B1","C1"));

        // try to create same ticket again
        assertEquals(HTTP_BAD_REQUEST, addTickets("A1"));
    }

    @Test
    public void testValidExample() throws IOException {
        Map<String, Ticket> exampleTickets = getTicketMap("example", null);
        String[] ticketCodes = new String[exampleTickets.size()];
        assertEquals(HTTP_OK, addTickets(exampleTickets.keySet().toArray(ticketCodes)));
        Map<String, Ticket> tickets = getTickets();
        assertEquals(tickets.keySet(), exampleTickets.keySet());
    }

    @Test
    public void testSortByCode() throws IOException {
        Random random = new Random();
        String[] codes = new String[1000];
        for (int i = 0; i < 1000; i++) {
            codes[i] = Long.toString(random.nextLong());
        }
        addTickets(codes);
        List<Ticket> list = getTicketList(null, null);
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
        assertEquals(200, Request.Put(rootUrl + "tickets/A1")
                .bodyString(JACKSON.writeValueAsString(updateTicket), ContentType.APPLICATION_JSON)
                .execute().returnResponse().getStatusLine().getStatusCode());
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
        assertEquals(200, Request.Put(rootUrl + "tickets/A1")
                .bodyString(JACKSON.writeValueAsString(updateTicket), ContentType.APPLICATION_JSON)
                .execute().returnResponse().getStatusLine().getStatusCode());
        ticket = JACKSON.readValue(Request.Get(rootUrl + "tickets/A1").execute().returnContent().asString(), Ticket.class);
        assertTrue(ticket.getTicketholder().startsWith("Alice"));
    }

    @Test
    public void updateTicketDescriptionFailsWithNoEntity() throws IOException {
        addTickets("A1");
        assertEquals(400, Request.Put(rootUrl + "tickets/A1")
                .execute().returnResponse().getStatusLine().getStatusCode());
    }

    @Test
    public void testUnscan() throws IOException {
        // create tickets and scan
        addTickets("A","B");
        scan("A");
        scan("B");

        // both of the tickets have been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("A").getScanned());
        assertNotNull(tickets.get("B").getScanned());

        // un-scan ticket A
        assertEquals(200, Request.Patch(rootUrl + "tickets/A").execute().returnResponse().getStatusLine().getStatusCode());

        // now only one of the tickets has been scanned
        tickets = getTickets();
        assertNull(tickets.get("A").getScanned());
        assertNotNull(tickets.get("B").getScanned());
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
                .map(code -> new Ticket(code, "GENERIC TICKET " + code, "Ticket Holder")).collect(Collectors.toList());
        String postTickets = JACKSON.writeValueAsString(tickets);
        return Request.Post(rootUrl + "tickets/").bodyString(postTickets, ContentType.APPLICATION_JSON).execute()
                .returnResponse().getStatusLine().getStatusCode();
    }

    private ScanResult scan(String code) throws IOException {
        return JACKSON.readValue(Request.Post(rootUrl + "tickets/" + code).execute().returnContent().asString(), ScanResult.class);
    }
}
