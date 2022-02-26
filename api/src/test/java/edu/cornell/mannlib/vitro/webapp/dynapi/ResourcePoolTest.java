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
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceKey;
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
        assertTrue(resourcePool.get(TEST_RESOURCE_KEY) instanceof DefaultResource);
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

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));
    }

    @Test
    public void testVersioning() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        ResourceKey v0 = ResourceKey.ceiling("test_resource", "0");
        ResourceKey v1 = ResourceKey.ceiling("test_resource", "1");

        ResourceKey rv0 = ResourceKey.ceiling("test_reload_resource", "0");
        ResourceKey rv1 = ResourceKey.ceiling("test_reload_resource", "1");
        ResourceKey rv2 = ResourceKey.ceiling("test_reload_resource", "2");
        ResourceKey rv3 = ResourceKey.ceiling("test_reload_resource", "3");
        ResourceKey rv4 = ResourceKey.ceiling("test_reload_resource", "4");
        ResourceKey rv5 = ResourceKey.ceiling("test_reload_resource", "5");

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(v1));

        assertTrue(resourcePool.get(rv0) instanceof DefaultResource);
        assertTrue(resourcePool.get(rv1) instanceof DefaultResource);
        assertTrue(resourcePool.get(rv2) instanceof DefaultResource);

        loadReloadModel();
        resourcePool.reload();

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(v1));

        assertTrue(resourcePool.get(rv0) instanceof DefaultResource);
        assertResource(TEST_RELOAD_RESOURCE_KEY, TEST_RELOAD_ACTION_NAME, resourcePool.get(rv1));
        assertResource(TEST_RELOAD_RESOURCE_KEY, TEST_RELOAD_ACTION_NAME, resourcePool.get(rv2));


        loadVersionedModel();
        resourcePool.reload();

        ResourceKey erv1 = ResourceKey.from("test_reload_resource", "1.1.0");
        ResourceKey erv2 = ResourceKey.from("test_reload_resource", "2.0.0");
        ResourceKey erv4 = ResourceKey.from("test_reload_resource", "4.3.7");

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(v1));

        assertTrue(resourcePool.get(rv0) instanceof DefaultResource);
        assertResource(erv1, TEST_RELOAD_ACTION_NAME, resourcePool.get(rv1));
        assertResource(erv2, TEST_RELOAD_ACTION_NAME, resourcePool.get(rv2));

        // NOTE: skipped a version from 2 to 4 and 2 has max of 2
        assertTrue(resourcePool.get(rv3) instanceof DefaultResource);

        assertResource(erv4, TEST_RELOAD_ACTION_NAME, resourcePool.get(rv4));
        assertResource(erv4, TEST_RELOAD_ACTION_NAME, resourcePool.get(rv5));
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

        assertResource(TEST_RELOAD_RESOURCE_KEY, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_KEY));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        Resource resource = loader.loadInstance(TEST_RELOAD_RESOURCE_URI, Resource.class);

        assertEquals(0, resourcePool.obsoleteCount());

        Resource resourceHasClient = resourcePool.get(TEST_RELOAD_RESOURCE_KEY);

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

        assertTrue(resourcePool.get(TEST_RELOAD_RESOURCE_KEY) instanceof DefaultResource);

        resourcePool.add(TEST_RELOAD_RESOURCE_URI, resource);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        Resource resource = resourcePool.get(TEST_RELOAD_RESOURCE_KEY);

        assertFalse(resource instanceof DefaultResource);

        resource.removeClient();

        reset();

        resourcePool.remove(TEST_RELOAD_RESOURCE_URI, TEST_RELOAD_RESOURCE_KEY);

        assertEquals(0, resourcePool.obsoleteCount());

        assertTrue(resourcePool.get(TEST_RELOAD_RESOURCE_KEY) instanceof DefaultResource);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        Resource resourceHasClient = resourcePool.get(TEST_RELOAD_RESOURCE_KEY);

        assertFalse(resourceHasClient instanceof DefaultResource);

        setup();

        resourcePool.init(servletContext);

        resourcePool.remove(TEST_RELOAD_RESOURCE_URI, TEST_RELOAD_RESOURCE_KEY);

        assertEquals(1, resourcePool.obsoleteCount());

        assertTrue(resourcePool.get(TEST_RELOAD_RESOURCE_KEY) instanceof DefaultResource);

        resourceHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        resourcePool.reload();

        resourcePool.remove(TEST_RELOAD_RESOURCE_URI, TEST_RELOAD_RESOURCE_KEY);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = resourcePool.get(TEST_RELOAD_RESOURCE_KEY);

        assertTrue(resource instanceof DefaultResource);

        resourcePool.reload(TEST_RELOAD_RESOURCE_URI);

        assertResource(TEST_RELOAD_RESOURCE_KEY, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_KEY));
    }

    @Test
    public void testReload() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));

        loadReloadModel();

        resourcePool.reload();

        assertEquals(2, resourcePool.count());
        assertEquals(0, resourcePool.obsoleteCount());

        assertResource(TEST_RELOAD_RESOURCE_KEY, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_KEY));
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));

        loadReloadModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));
        }

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));

        assertResource(TEST_RELOAD_RESOURCE_KEY, TEST_RELOAD_ACTION_NAME, resourcePool.get(TEST_RELOAD_RESOURCE_KEY));
    }

    @Test
    public void testRealodOfResourceHasClient() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadReloadModel();

        Resource resource = resourcePool.get(TEST_RESOURCE_KEY);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_RESOURCE_KEY, resource.getKey());
        }

        resource.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ResourcePool resourcePool = initWithDefaultModel();

        resourcePool.reload();

        long initalCount = resourcePool.obsoleteCount();
        Resource resource = resourcePool.get(TEST_RESOURCE_KEY);

        resource.removeClient();

        assertFalse(resource.hasClients());

        Thread t1 = getResourceInThread(resourcePool, TEST_RESOURCE_KEY);

        t1.join();

        assertTrue(resource.hasClients());

        resourcePool.reload();

        assertEquals(initalCount, resourcePool.obsoleteCount());
    }

    private Thread getResourceInThread(ResourcePool resourcePool, ResourceKey resourceKey) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                Resource resource = resourcePool.get(resourceKey);
                assertEquals(resourceKey, resource.getKey());
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

    private void loadVersionedModel() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-versioning.n3")
        );
    }

    private void assertResource(ResourceKey expctedResourceKey, String expectedActionName, Resource actualResource) {
        assertNotNull(actualResource);
        assertFalse(format("%s not loaded!", expctedResourceKey), actualResource instanceof DefaultResource);
        assertEquals(expctedResourceKey, actualResource.getKey());
        assertTrue(actualResource.hasClients());

        assertEquals(expectedActionName, actualResource.getRpcOnGet().getName());
        assertEquals("GET", actualResource.getRpcOnGet().getHttpMethod().getName());
        assertEquals(expctedResourceKey.getVersion().toString(), actualResource.getRpcOnGet().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnPost().getName());
        assertEquals("POST", actualResource.getRpcOnPost().getHttpMethod().getName());
        assertEquals(expctedResourceKey.getVersion().toString(), actualResource.getRpcOnPost().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnDelete().getName());
        assertEquals("DELETE", actualResource.getRpcOnDelete().getHttpMethod().getName());
        assertEquals(expctedResourceKey.getVersion().toString(), actualResource.getRpcOnDelete().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnPut().getName());
        assertEquals("PUT", actualResource.getRpcOnPut().getHttpMethod().getName());
        assertEquals(expctedResourceKey.getVersion().toString(), actualResource.getRpcOnPut().getMinVersion());
        assertEquals(expectedActionName, actualResource.getRpcOnPatch().getName());
        assertEquals("PATCH", actualResource.getRpcOnPatch().getHttpMethod().getName());
        assertEquals(expctedResourceKey.getVersion().toString(), actualResource.getRpcOnPatch().getMinVersion());

        actualResource.removeClient();
    }

}
