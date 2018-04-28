package org.pricelessfestival.crossoff.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.junit.Assert.*;
import static org.pricelessfestival.crossoff.server.GlobalObjectMapper.JACKSON;

/**
 * Created by ivan on 4/26/18.
 */
@SuppressWarnings("unchecked")
@Log4j2
public class ApiTests {

    private static int HTTP_OK = 200;
    private static int HTTP_NOT_FOUND = 404;
    private static int HTTP_CONFLICT = 409;
    private static int HTTP_BAD_REQUEST = 400;

    private String rootUrl = "http://localhost:" + WebServer.PORT + "/";

    @Rule
    public TestName name = new TestName();

    @Before
    public void setup() {
        log.info("***** STARTING TEST {} *****", name.getMethodName());
        if (!Persistence.isReady()) {
            Persistence.init("test.hibernate.cfg.xml");
            try {
                new WebServer().start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @After
    public void tearDown() {
        log.debug("DELETING all tickets after test");
        Persistence.exec(session -> session.createQuery("delete from Ticket").executeUpdate());
    }

    @Test
    public void testValidScans() throws IOException {
        // create tickets
        addTickets("A","B","C");

        // scan two of them
        assertEquals(HTTP_OK, scan("A").returnResponse().getStatusLine().getStatusCode());
        assertEquals(HTTP_OK, scan("B").returnResponse().getStatusLine().getStatusCode());

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
        assertEquals(HTTP_OK, scan("1").returnResponse().getStatusLine().getStatusCode());
        assertEquals(HTTP_NOT_FOUND, scan("3").returnResponse().getStatusLine().getStatusCode());

        // one ticket has been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("1").getScanned());
        assertNull(tickets.get("2").getScanned());
    }

    @Test
    public void testScanTwice() throws IOException {
        // create tickets
        addTickets("1","2");

        // duplicate scan
        assertEquals(HTTP_OK, scan("1").returnResponse().getStatusLine().getStatusCode());
        assertEquals(HTTP_CONFLICT, scan("1").returnResponse().getStatusLine().getStatusCode());

        // one ticket has been scanned
        Map<String, Ticket> tickets = getTickets();
        assertNotNull(tickets.get("1").getScanned());
        assertNull(tickets.get("2").getScanned());
    }

    @Test
    public void testRoot() throws IOException {
        assertEquals(HTTP_OK, Request.Get(rootUrl).execute().returnResponse().getStatusLine().getStatusCode());
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
    public void testCreateFailsWIthNoEntity() throws IOException {
        int status = Request.Post(rootUrl + "tickets").execute().returnResponse().getStatusLine().getStatusCode();
        assertEquals(HTTP_BAD_REQUEST, status);
    }

    @Test
    public void testCreateFailsWIthNoTickets() throws IOException {
        assertEquals(HTTP_BAD_REQUEST, addTickets());
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

    private Map<String, Ticket> getTickets() throws IOException {
        String ticketList = Request.Get(rootUrl + "tickets").execute().returnContent().asString();
        List<Ticket> tickets = JACKSON.readValue(ticketList, new TypeReference<List<Ticket>>() { } );
        return tickets.stream().collect(Collectors.toMap(Ticket::getCode, identity()));
    }

    private int addTickets(String... codes) throws IOException {
        List<Ticket> tickets = Lists.newArrayList(codes).stream()
                .map(code -> new Ticket(code, "GENERIC TICKET " + code)).collect(Collectors.toList());
        String postTickets = JACKSON.writeValueAsString(tickets);
        return Request.Post(rootUrl + "tickets").bodyString(postTickets, ContentType.APPLICATION_JSON).execute()
                .returnResponse().getStatusLine().getStatusCode();
    }

    private Response scan(String code) throws IOException {
        return Request.Post(rootUrl + "tickets/" + code).execute();
    }
}
