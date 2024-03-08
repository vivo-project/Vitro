package edu.cornell.mannlib.vitro.webapp.dynapi.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.RequestType;

@RunWith(MockitoJUnitRunner.class)
public class RequestPathTest {

    private String resourceId = "https://scholars.institution.edu/individual/n1f9d4ddc";
    private String encodedResourceId = Base64.getEncoder().encodeToString(resourceId.getBytes());

    @Mock
    private HttpServletRequest request;

    @Test
    public void testRpc() {
        when(request.getContextPath()).thenReturn("/api/rpc/create");
        when(request.getPathInfo()).thenReturn("/create");

        RequestPath requestPath = RequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertFalse(requestPath.isCustomRestAction());
        assertEquals(RequestType.RPC, requestPath.getType());
        assertEquals("create", requestPath.getActionName());
        assertEquals(null, requestPath.getResourceVersion());
        assertEquals(null, requestPath.getResourceName());
        assertEquals(null, requestPath.getResourceId());


        when(request.getContextPath()).thenReturn("/api/rpc/");
        when(request.getPathInfo()).thenReturn("/");

        assertFalse(RequestPath.from(request).isValid());


        when(request.getContextPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn("/create");

        assertFalse(RequestPath.from(request).isValid());


        when(request.getContextPath()).thenReturn("/api/rpc");
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(RequestPath.from(request).isValid());


        when(request.getContextPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(RequestPath.from(request).isValid());
    }

    @Test
    public void testRestCollection() {
        when(request.getContextPath()).thenReturn("/api/rest/1/persons");
        when(request.getPathInfo()).thenReturn("/1/persons");

        RequestPath requestPath = RequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertFalse(requestPath.isCustomRestAction());
        assertEquals(RequestType.REST, requestPath.getType());
        assertEquals("1", requestPath.getResourceVersion());
        assertEquals("persons", requestPath.getResourceName());
        assertEquals(null, requestPath.getResourceId());
        assertEquals(null, requestPath.getActionName());


        when(request.getContextPath()).thenReturn("/api/rest/");
        when(request.getPathInfo()).thenReturn("/");

        assertFalse(RequestPath.from(request).isValid());


        when(request.getContextPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn("/1/persons");

        assertFalse(RequestPath.from(request).isValid());


        when(request.getContextPath()).thenReturn("/api/rest");
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(RequestPath.from(request).isValid());


        when(request.getContextPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(RequestPath.from(request).isValid());
    }

    @Test
    public void testRestCustomAction() {
        when(request.getContextPath()).thenReturn("/api/rest/1/persons/dedupe");
        when(request.getPathInfo()).thenReturn("/1/persons/dedupe");

        RequestPath requestPath = RequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertTrue(requestPath.isCustomRestAction());
        assertEquals(RequestType.REST, requestPath.getType());
        assertEquals("1", requestPath.getResourceVersion());
        assertEquals("persons", requestPath.getResourceName());
        assertEquals(null, requestPath.getResourceId());
        assertEquals("dedupe", requestPath.getActionName());
    }

    @Test
    public void testRestIndividual() {
        when(request.getContextPath()).thenReturn("/api/rest/1/persons/resource:" + encodedResourceId);
        when(request.getPathInfo()).thenReturn("/1/persons/resource:" + encodedResourceId);

        RequestPath requestPath = RequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertFalse(requestPath.isCustomRestAction());
        assertEquals(RequestType.REST, requestPath.getType());
        assertEquals("1", requestPath.getResourceVersion());
        assertEquals("persons", requestPath.getResourceName());
        assertEquals(resourceId, requestPath.getResourceId());
        assertEquals(null, requestPath.getActionName());
    }

    @Test
    public void testRestIndividualCustomAction() {
        when(request.getContextPath()).thenReturn("/api/rest/1/persons/resource:" + encodedResourceId + "/patch");
        when(request.getPathInfo()).thenReturn("/1/persons/resource:" + encodedResourceId + "/patch");

        RequestPath requestPath = RequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertTrue(requestPath.isCustomRestAction());
        assertEquals(RequestType.REST, requestPath.getType());
        assertEquals("1", requestPath.getResourceVersion());
        assertEquals("persons", requestPath.getResourceName());
        assertEquals(resourceId, requestPath.getResourceId());
        assertEquals("patch", requestPath.getActionName());
    }

    @Test
    public void testNotFound() {
        when(request.getContextPath()).thenReturn("/api/rest/1/persons/resource:" + encodedResourceId + "/patch/foo");
        when(request.getPathInfo()).thenReturn("/1/persons/resource:" + encodedResourceId + "/patch/foo");

        assertFalse(RequestPath.from(request).isValid());

        when(request.getContextPath()).thenReturn("/api/bar/1/persons");
        when(request.getPathInfo()).thenReturn("/1/persons");

        assertFalse(RequestPath.from(request).isValid());

        when(request.getContextPath()).thenReturn("/some/random/path");
        when(request.getPathInfo()).thenReturn("/3/2/1");

        assertFalse(RequestPath.from(request).isValid());
    }

}
