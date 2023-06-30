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

    protected final static String TEST_PERSON_RESOURCE_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/resource/testPersonResource1";

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

        ResourceKey resouce_v0 = ResourceKey.of("test_resource", "0");
        ResourceKey resource_v1 = ResourceKey.of("test_resource", "1");

        ResourceKey document_v0 = ResourceKey.of("test_document_resource", "0");
        ResourceKey document_v1 = ResourceKey.of("test_document_resource", "1");

        String testDocumentActionName = "test_document";

        ResourceKey person_v0 = ResourceKey.of("test_person_resource", "0");
        ResourceKey person_v1 = ResourceKey.of("test_person_resource", "1");
        ResourceKey person_v1_0 = ResourceKey.of("test_person_resource", "1.0");
        ResourceKey person_v1_0_0 = ResourceKey.of("test_person_resource", "1.0.0");
        ResourceKey person_v1_1 = ResourceKey.of("test_person_resource", "1.1");
        ResourceKey person_v1_1_0 = ResourceKey.of("test_person_resource", "1.1.0");
        ResourceKey person_v1_2 = ResourceKey.of("test_person_resource", "1.2");
        ResourceKey person_v2 = ResourceKey.of("test_person_resource", "2");
        ResourceKey person_v3 = ResourceKey.of("test_person_resource", "3");
        ResourceKey person_v4 = ResourceKey.of("test_person_resource", "4");
        ResourceKey person_v4_2 = ResourceKey.of("test_person_resource", "4.2");
        ResourceKey person_v4_3 = ResourceKey.of("test_person_resource", "4.3");
        ResourceKey person_v4_3_6 = ResourceKey.of("test_person_resource", "4.3.6");
        ResourceKey person_v4_3_7 = ResourceKey.of("test_person_resource", "4.3.7");
        ResourceKey person_v4_3_8 = ResourceKey.of("test_person_resource", "4.3.8");
        ResourceKey person_v4_4 = ResourceKey.of("test_person_resource", "4.4");
        ResourceKey person_v5 = ResourceKey.of("test_person_resource", "5");

        ResourceKey expectedDocument_v1_0_0 = ResourceKey.of("test_document_resource", "1.0.0");

        ResourceKey expectedPerson_v1_0_0 = TEST_PERSON_RESOURCE_KEY; // "test_person_resource", "1.0.0"
        ResourceKey expectedPerson_v1_1_0 = ResourceKey.of("test_person_resource", "1.1.0");
        ResourceKey expectedPerson_v2_0_0 = ResourceKey.of("test_person_resource", "2.0.0");
        ResourceKey expectedPerson_v4_3_7 = ResourceKey.of("test_person_resource", "4.3.7");


        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resource_v1));
        // base test for test_document
        assertTrue(resourcePool.get(document_v0) instanceof DefaultResource);
        assertTrue(resourcePool.get(document_v1) instanceof DefaultResource);
        // demonstrate no person resources in resource pool
        assertTrue(resourcePool.get(person_v0) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v1) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v1_0) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v1_1) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v1_1_0) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v1_2) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v2) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v3) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4_2) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4_3) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4_3_6) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4_3_7) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4_3_8) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v4_4) instanceof DefaultResource);
        assertTrue(resourcePool.get(person_v5) instanceof DefaultResource);

        loadTestModel();
        resourcePool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resource_v1));
        // base test for test_document
        assertTrue(resourcePool.get(document_v0) instanceof DefaultResource);
        assertResource(expectedDocument_v1_0_0, testDocumentActionName, resourcePool.get(document_v1));

        // no person version 0 in pool
        assertTrue(resourcePool.get(person_v0) instanceof DefaultResource);
        // person resource version 1.0.0 has no max version, any major version request greater than 1 should return version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v2));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v3));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v4));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v5));


        loadPersonVersion1_1Model();
        resourcePool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resource_v1));
        // base test for test_document
        assertTrue(resourcePool.get(document_v0) instanceof DefaultResource);
        assertResource(expectedDocument_v1_0_0, testDocumentActionName, resourcePool.get(document_v1));

        // no person version 0 in pool
        assertTrue(resourcePool.get(person_v0) instanceof DefaultResource);
        // still able to get person version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_0));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_0_0));

        // able to get person version 1.1.0 from varying levels of specificity
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_1_0));

        // person version 1 does not have specific minor version 2
        assertTrue(resourcePool.get(person_v1_2) instanceof DefaultResource);

        // person resource version 1.1.0 has no max version, any major version request greater than 1 should return version 1.1.0
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v2));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v3));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v4));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v5));


        loadPersonVersion2Model();
        resourcePool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resource_v1));
        // base test for test_document
        assertTrue(resourcePool.get(document_v0) instanceof DefaultResource);
        assertResource(expectedDocument_v1_0_0, testDocumentActionName, resourcePool.get(document_v1));

         // no person version 0 in pool
         assertTrue(resourcePool.get(person_v0) instanceof DefaultResource);
         // still able to get person version 1.0.0
         assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_0));
         assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_0_0));
 
         // able to get person version 1.1.0 from varying levels of specificity
         assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1));
         assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_1));
         assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_1_0));
 
         // person version 1 does not have specific minor version 2
         assertTrue(resourcePool.get(person_v1_2) instanceof DefaultResource);

         // able to get person version 2.0.0
         assertResource(expectedPerson_v2_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v2));

         // person resource version 2.0.0 has no max version, any major version request greater than 2 should return version 2.0.0
         assertResource(expectedPerson_v2_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v3));
         assertResource(expectedPerson_v2_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v4));
         assertResource(expectedPerson_v2_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v5));


        loadPersonVersion4_3_7Model();
        resourcePool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(resource_v1));
        // base test for test_document
        assertTrue(resourcePool.get(document_v0) instanceof DefaultResource);
        assertResource(expectedDocument_v1_0_0, testDocumentActionName, resourcePool.get(document_v1));


        // no person version 0 in pool
        assertTrue(resourcePool.get(person_v0) instanceof DefaultResource);
        // still able to get person version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_0));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_0_0));

        // able to get person version 1.1.0 from varying levels of specificity
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v1_1_0));

        // person version 1 does not have specific minor version 2
        assertTrue(resourcePool.get(person_v1_2) instanceof DefaultResource);

        // still able to get person version 2.0.0
        assertResource(expectedPerson_v2_0_0, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v2));

        // skipped a version from 2.0.0 to 4.3.7 and 2.0.0 has max of 2.0.0
        assertTrue(resourcePool.get(person_v3) instanceof DefaultResource);

        // no version 4.2 exists
        assertTrue(resourcePool.get(person_v4_2) instanceof DefaultResource);
        // version 4.3.6 does not exist
        assertTrue(resourcePool.get(person_v4_3_6) instanceof DefaultResource);
        // version 4.3.8 does not exist
        assertTrue(resourcePool.get(person_v4_3_8) instanceof DefaultResource);

        // no version 4.4 exists
        assertTrue(resourcePool.get(person_v4_4) instanceof DefaultResource);

        // able to get person version 4.3.7 at varying levels of specificity
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v4));
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v4_3));
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v4_3_7));

        // person resource version 4.3.7 has no max version, any major version request greater than 4 should return version 2.0.0
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_ACTION_NAME, resourcePool.get(person_v5));
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

        loadTestModel();

        Resource resource = loader.loadInstance(TEST_PERSON_RESOURCE_URI, Resource.class);

        resourcePool.add(TEST_PERSON_RESOURCE_URI, resource);

        assertEquals(0, resourcePool.obsoleteCount());

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_ACTION_NAME, resourcePool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

        resourcePool.reload();

        Resource resource = loader.loadInstance(TEST_PERSON_RESOURCE_URI, Resource.class);

        assertEquals(0, resourcePool.obsoleteCount());

        Resource resourceHasClient = resourcePool.get(TEST_PERSON_RESOURCE_KEY);

        resourcePool.add(TEST_PERSON_RESOURCE_URI, resource);

        assertEquals(1, resourcePool.obsoleteCount());

        resourceHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

        Resource resource = loader.loadInstance(TEST_PERSON_RESOURCE_URI, Resource.class);

        reset();

        assertTrue(resourcePool.get(TEST_PERSON_RESOURCE_KEY) instanceof DefaultResource);

        resourcePool.add(TEST_PERSON_RESOURCE_URI, resource);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

        resourcePool.reload();

        Resource resource = resourcePool.get(TEST_PERSON_RESOURCE_KEY);

        assertFalse(resource instanceof DefaultResource);

        resource.removeClient();

        reset();

        resourcePool.remove(TEST_PERSON_RESOURCE_URI, TEST_PERSON_RESOURCE_KEY);

        assertEquals(0, resourcePool.obsoleteCount());

        assertTrue(resourcePool.get(TEST_PERSON_RESOURCE_KEY) instanceof DefaultResource);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

        resourcePool.reload();

        Resource resourceHasClient = resourcePool.get(TEST_PERSON_RESOURCE_KEY);

        assertFalse(resourceHasClient instanceof DefaultResource);

        setup();

        resourcePool.init(servletContext);

        resourcePool.remove(TEST_PERSON_RESOURCE_URI, TEST_PERSON_RESOURCE_KEY);

        assertEquals(1, resourcePool.obsoleteCount());

        assertTrue(resourcePool.get(TEST_PERSON_RESOURCE_KEY) instanceof DefaultResource);

        resourceHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

        resourcePool.reload();

        resourcePool.remove(TEST_PERSON_RESOURCE_URI, TEST_PERSON_RESOURCE_KEY);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

        Resource resource = resourcePool.get(TEST_PERSON_RESOURCE_KEY);

        assertTrue(resource instanceof DefaultResource);

        resourcePool.reload(TEST_PERSON_RESOURCE_URI);

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_ACTION_NAME, resourcePool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testReload() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));

        loadTestModel();

        resourcePool.reload();

        assertEquals(8, resourcePool.count());
        assertEquals(0, resourcePool.obsoleteCount());

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_ACTION_NAME, resourcePool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));

        loadTestModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourcePool.reload());

        while (!reloadFuture.isDone()) {
            assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));
        }

        assertResource(TEST_RESOURCE_KEY, TEST_ACTION_NAME, resourcePool.get(TEST_RESOURCE_KEY));

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_ACTION_NAME, resourcePool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testRealodOfResourceHasClient() throws IOException {
        ResourcePool resourcePool = initWithDefaultModel();

        loadTestModel();

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

    private void loadPersonVersion1_1Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person1_1.n3")
        );
    }

    private void loadPersonVersion2Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person2.n3")
        );
    }

    private void loadPersonVersion4_3_7Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person4_3_7.n3")
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
