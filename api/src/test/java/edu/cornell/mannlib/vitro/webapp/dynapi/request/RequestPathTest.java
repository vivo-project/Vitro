package edu.cornell.mannlib.vitro.webapp.dynapi.request;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.REST_SERVLET_PATH;
import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;

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

import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RequestType;

@RunWith(MockitoJUnitRunner.class)
public class RequestPathTest {

    private String resourceId = "https://scholars.institution.edu/individual/n1f9d4ddc";
    private String encodedResourceId = Base64.getEncoder().encodeToString(resourceId.getBytes());

    @Mock
    private HttpServletRequest request;

    @Test
    public void testRpc() {
        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/create");

        ApiRequestPath requestPath = ApiRequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertFalse(requestPath.isCustomRestAction());
        assertEquals(RequestType.RPC, requestPath.getType());
        assertEquals("create", requestPath.getActionName());
        assertEquals(null, requestPath.getResourceVersion());
        assertEquals(null, requestPath.getResourceName());
        assertEquals(null, requestPath.getResourceId());

        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/");

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn("/create");

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(ApiRequestPath.from(request).isValid());
    }

    @Test
    public void testRestCollection() {
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/1/persons");

        ApiRequestPath requestPath = ApiRequestPath.from(request);

        assertTrue(requestPath.isValid());
        assertFalse(requestPath.isCustomRestAction());
        assertEquals(RequestType.REST, requestPath.getType());
        assertEquals("1", requestPath.getResourceVersion());
        assertEquals("persons", requestPath.getResourceName());
        assertEquals(null, requestPath.getResourceId());
        assertEquals(null, requestPath.getActionName());

        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/");

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn("/1/persons");

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn(null);
        when(request.getPathInfo()).thenReturn(null);

        assertFalse(ApiRequestPath.from(request).isValid());
    }

    @Test
    public void testRestCustomAction() {
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/1/persons/dedupe");

        ApiRequestPath requestPath = ApiRequestPath.from(request);

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
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/1/persons/resource:" + encodedResourceId);

        ApiRequestPath requestPath = ApiRequestPath.from(request);

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
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/1/persons/resource:" + encodedResourceId + "/patch");

        ApiRequestPath requestPath = ApiRequestPath.from(request);

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
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn("/1/persons/resource:" + encodedResourceId + "/patch/foo");

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn("/api/bar");
        when(request.getPathInfo()).thenReturn("/1/persons");

        assertFalse(ApiRequestPath.from(request).isValid());

        when(request.getServletPath()).thenReturn("/some/random/path");
        when(request.getPathInfo()).thenReturn("/3/2/1");

        assertFalse(ApiRequestPath.from(request).isValid());
    }

}
