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
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public class ResourcePoolTest extends ServletContextTest {

    protected final static String TEST_RELOAD_RESOURCE_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/resource/testReloadResource1";

    @After
    public void reset() {
        setup();

        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.init(servletContext);
        resourcePool.reload();

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);
        actionPool.reload();

        assertEquals(0, resourcePool.count());
        assertEquals(0, resourcePool.obsoleteCount());

        assertEquals(0, actionPool.count());
        assertEquals(0, actionPool.obsoleteCount());
    }

    @Test
    public void testGetInstance() {
        ResourcePool resourcePool = ResourcePool.getInstance();
        assertNotNull(resourcePool);
        assertEquals(resourcePool, ResourcePool.getInstance());
    }

    @Test
    public void testGetBeforeInit() {
        ResourcePool resourcePool = ResourcePool.getInstance();
        Resource resource = resourcePool.get(TEST_RESOURCE_NAME);
        assertNotNull(resource);
        assertTrue(resource instanceof DefaultResource);
    }

    @Test
    public void testPrintKeysBeforeInit() {
        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.printKeys();
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
        assertEquals(1, resourcePool.count());
        assertEquals(0, resourcePool.obsoleteCount());

        assertResource(TEST_RESOURCE_NAME, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_NAME));
    }

    @Test
    public void testPrintKeys() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        resourcePool.printKeys();
        // nothing to assert
    }

    @Test
    public void testAdd() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = loader.loadInstance(TEST_RELOAD_RESOURCE_URI, Resource.class);

        resourcePool.add(TEST_RELOAD_RESOURCE_URI, resource);

        assertEquals(0, resourcePool.obsoleteCount());

        assertResource(TEST_RELOAD_RESOURCE_NAME, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_NAME));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        Resource resource = loader.loadInstance(TEST_RELOAD_RESOURCE_URI, Resource.class);

        assertEquals(0, resourcePool.obsoleteCount());

        Resource resourceHasClient = resourcePool.get(TEST_RELOAD_RESOURCE_NAME);

        resourcePool.add(TEST_RELOAD_RESOURCE_URI, resource);

        assertEquals(1, resourcePool.obsoleteCount());

        resourceHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = loader.loadInstance(TEST_RELOAD_RESOURCE_URI, Resource.class);

        reset();

        assertTrue(resourcePool.get(TEST_RELOAD_RESOURCE_NAME) instanceof DefaultResource);

        resourcePool.add(TEST_RELOAD_RESOURCE_URI, resource);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        Resource resource = resourcePool.get(TEST_RELOAD_RESOURCE_NAME);

        assertFalse(resource instanceof DefaultResource);

        resource.removeClient();

        reset();

        resourcePool.remove(TEST_RELOAD_RESOURCE_URI, TEST_RELOAD_RESOURCE_NAME);

        assertEquals(0, resourcePool.obsoleteCount());

        assertTrue(resourcePool.get(TEST_RELOAD_RESOURCE_NAME) instanceof DefaultResource);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        Resource resourceHasClient = resourcePool.get(TEST_RELOAD_RESOURCE_NAME);

        assertFalse(resourceHasClient instanceof DefaultResource);

        setup();

        resourcePool.init(servletContext);

        resourcePool.remove(TEST_RELOAD_RESOURCE_URI, TEST_RELOAD_RESOURCE_NAME);

        assertEquals(1, resourcePool.obsoleteCount());

        assertTrue(resourcePool.get(TEST_RELOAD_RESOURCE_NAME) instanceof DefaultResource);

        resourceHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        resourcePool.remove(TEST_RELOAD_RESOURCE_URI, TEST_RELOAD_RESOURCE_NAME);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = resourcePool.get(TEST_RELOAD_RESOURCE_NAME);

        assertTrue(resource instanceof DefaultResource);

        resourcePool.reload(TEST_RELOAD_RESOURCE_URI);

        assertResource(TEST_RELOAD_RESOURCE_NAME, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_NAME));
    }

    @Test
    public void testReload() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_NAME, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_NAME));

        loadReloadModel();

        resourcePool.reload();

        assertEquals(2, resourcePool.count());
        assertEquals(0, resourcePool.obsoleteCount());

        assertResource(TEST_RELOAD_RESOURCE_NAME, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_NAME));
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_NAME, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_NAME));

        loadReloadModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertResource(TEST_RESOURCE_NAME, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_NAME));
        }

        assertResource(TEST_RESOURCE_NAME, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_NAME));

        assertResource(TEST_RELOAD_RESOURCE_NAME, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_NAME));
    }

    @Test
    public void testRealodOfResourceHasClient() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = resourcePool.get(TEST_RESOURCE_NAME);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_RESOURCE_NAME, resource.getKey());
        }

        resource.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ResourcePool resourcePool = initWithDefaultModel();

        resourcePool.reload();

        long initalCount = resourcePool.obsoleteCount();
        Resource resource = resourcePool.get(TEST_RESOURCE_NAME);

        resource.removeClient();

        assertFalse(resource.hasClients());

        Thread t1 = getResourceInThread(resourcePool, TEST_RESOURCE_NAME);

        t1.join();

        assertTrue(resource.hasClients());

        resourcePool.reload();

        assertEquals(initalCount, resourcePool.obsoleteCount());
    }

    private Thread getResourceInThread(ResourcePool resourcePool, String resourceName) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                Resource resource = resourcePool.get(resourceName);
                assertEquals(resourceName, resource.getKey());
                assertTrue(resource.hasClients());
            }
        };
        Thread thread = new Thread(client);
        thread.start();
        return thread;
    }

    private ResourcePool initWithDefaultModel() throws IOException {
        loadDefaultModel();

        ResourcePool resourcePool = ResourcePool.getInstance();
        resourcePool.init(servletContext);

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);

        return resourcePool;
    }

    private void assertResource(String expctedResourceName, String expectedActionName, Resource actualResource) {
        assertNotNull(actualResource);
        assertFalse(format("%s not loaded!", expctedResourceName), actualResource instanceof DefaultResource);
        assertEquals(expctedResourceName, actualResource.getKey());
        assertTrue(actualResource.hasClients());

        String minVer = "0.1.0";

        assertEquals(expectedActionName, actualResource.getRpcOnGet().getName());
        // TODO: figure out why these are all POST
        // assertEquals("GET", resource.getRpcOnGet().getHttpMethod().getName());
        assertEquals(minVer, actualResource.getRpcOnGet().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnPost().getName());
        assertEquals("POST", actualResource.getRpcOnPost().getHttpMethod().getName());
        assertEquals(minVer, actualResource.getRpcOnPost().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnDelete().getName());
        // assertEquals("DELETE", resource.getRpcOnDelete().getHttpMethod().getName());
        assertEquals(minVer, actualResource.getRpcOnDelete().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnPut().getName());
        // assertEquals("PUT", resource.getRpcOnPut().getHttpMethod().getName());
        assertEquals(minVer, actualResource.getRpcOnPut().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnPatch().getName());
        // assertEquals("PATCH", resource.getRpcOnPatch().getHttpMethod().getName());
        assertEquals(minVer, actualResource.getRpcOnPatch().getMinVersion());

        actualResource.removeClient();
    }

}
