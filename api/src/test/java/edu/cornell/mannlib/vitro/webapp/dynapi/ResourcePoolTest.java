package edu.cornell.mannlib.vitro.webapp.dynapi;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResource;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Resource;

public class ResourcePoolTest extends ServletContextTest {

    @After
    public void reset() {
        setup();

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);
        actionPool.reload();

        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.init(servletContext);
        resourcePool.reload();
    }

    @Test
    public void testGetInstance() {
        ResourcePool resourcePool = ResourcePool.getInstance();
        assertNotNull(resourcePool);
        assertEquals(resourcePool, ResourcePool.getInstance());
    }

    @Test
    public void testGetByNameBeforeInit() {
        ResourcePool resourcePool = ResourcePool.getInstance();
        Resource resource = resourcePool.getByName(TEST_RESOURCE_NAME);
        assertNotNull(resource);
        assertTrue(resource instanceof DefaultResource);
    }

    @Test
    public void testPrintActionNamesBeforeInit() {
        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.printNames();
        // nothing to assert
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.reload();
        // not sure what to assert
    }

    @Test
    public void testInit() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResourceByName(resourcePool.getByName(TEST_RESOURCE_NAME), TEST_RESOURCE_NAME, TEST_ACTION_NAME);
    }

    @Test
    public void testPrintActionNames() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        resourcePool.printNames();
        // nothing to assert
    }

    @Test
    public void testReload() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResourceByName(resourcePool.getByName(TEST_RESOURCE_NAME), TEST_RESOURCE_NAME, TEST_ACTION_NAME);

        loadReloadModel();

        resourcePool.reload();

        assertResourceByName(resourcePool.getByName(TEST_RELOAD_RESOURCE_NAME), TEST_RELOAD_RESOURCE_NAME, TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResourceByName(resourcePool.getByName(TEST_RESOURCE_NAME), TEST_RESOURCE_NAME, TEST_ACTION_NAME);

        loadReloadModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertResourceByName(resourcePool.getByName(TEST_RESOURCE_NAME), TEST_RESOURCE_NAME, TEST_ACTION_NAME);
        }

        assertResourceByName(resourcePool.getByName(TEST_RESOURCE_NAME), TEST_RESOURCE_NAME, TEST_ACTION_NAME);

        assertResourceByName(resourcePool.getByName(TEST_RELOAD_RESOURCE_NAME), TEST_RELOAD_RESOURCE_NAME, TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testRealodOfActionInUse() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = resourcePool.getByName(TEST_RESOURCE_NAME);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_RESOURCE_NAME, resource.getName());
        }

        resource.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ResourcePool resourcePool = initWithDefaultModel();

        resourcePool.reload();

        long initalCount = resourcePool.obsoleteCount();
        Resource resource = resourcePool.getByName(TEST_RESOURCE_NAME);

        resource.removeClient();

        assertFalse(resource.hasClients());

        Thread t1 = getResourceInThread(resourcePool, TEST_RESOURCE_NAME, TEST_ACTION_NAME);

        t1.join();

        assertTrue(resource.hasClients());

        resourcePool.reload();

        assertEquals(initalCount, resourcePool.obsoleteCount());
    }

    private Thread getResourceInThread(ResourcePool resourcePool, String name, String actionName) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                Resource resource = resourcePool.getByName(name);
                assertResourceByName(resource, name, actionName);
            }
        };
        Thread thread = new Thread(client);
        thread.start();
        return thread;
    }

    private ResourcePool initWithDefaultModel() throws IOException {
        loadDefaultModel();

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);

        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.init(servletContext);

        return resourcePool;
    }

    private void assertResourceByName(Resource resource, String name, String actionName) {
        assertNotNull(resource);
        assertFalse(format("%s not loaded!", name), resource instanceof DefaultResource);
        assertEquals(name, resource.getName());
        assertTrue(resource.hasClients());

        String minVer = "0.1.0";

        assertEquals(actionName, resource.getRpcOnGet().getName());
        // assertEquals("GET", resource.getRpcOnGet().getHttpMethod().getName());
        assertEquals(minVer, resource.getRpcOnGet().getMinVersion());
        assertEquals(actionName, resource.getRpcOnPost().getName());
        assertEquals("POST", resource.getRpcOnPost().getHttpMethod().getName());
        assertEquals(minVer, resource.getRpcOnPost().getMinVersion());
        assertEquals(actionName, resource.getRpcOnDelete().getName());
        // assertEquals("DELETE", resource.getRpcOnDelete().getHttpMethod().getName());
        assertEquals(minVer, resource.getRpcOnDelete().getMinVersion());
        assertEquals(actionName, resource.getRpcOnPut().getName());
        // assertEquals("PUT", resource.getRpcOnPut().getHttpMethod().getName());
        assertEquals(minVer, resource.getRpcOnPut().getMinVersion());
        assertEquals(actionName, resource.getRpcOnPatch().getName());
        // assertEquals("PATCH", resource.getRpcOnPatch().getHttpMethod().getName());
        assertEquals(minVer, resource.getRpcOnPatch().getMinVersion());
    }

}
