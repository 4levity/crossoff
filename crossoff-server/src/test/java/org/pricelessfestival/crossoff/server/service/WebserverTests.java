package org.pricelessfestival.crossoff.server.service;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;
import org.pricelessfestival.crossoff.server.CrossoffIntegrationTests;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ivan on 5/6/18.
 */
public class WebserverTests extends CrossoffIntegrationTests {

    @Test
    public void testRoot() throws IOException {
        Response response = Request.Get(rootUrl).execute();
        assertTrue(response.returnContent().asString().contains("a href=admin.html"));
    }

    @Test
    public void testAdminHtml() throws IOException {
        assertEquals(HTTP_OK, Request.Get(rootUrl + "/admin.html").execute().returnResponse().getStatusLine().getStatusCode());
    }
}
