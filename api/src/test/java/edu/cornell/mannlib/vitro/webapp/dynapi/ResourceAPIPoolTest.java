/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ResourceAPIPoolTest extends ServletContextTest {

    protected final static String TEST_PERSON_RESOURCE_URI =
            "https://vivoweb.org/ontology/vitro-dynamic-api/resourceAPI/testPersonResource1";
    protected final static String TEST_PERSON_PROCEDURE_URI_1_1 =
            "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure1_1";
    protected final static String TEST_PERSON_PROCEDURE_URI_2 =
            "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure2";
    protected final static String TEST_PERSON_PROCEDURE_URI_3 =
            "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure3";
    protected final static String TEST_PERSON_PROCEDURE_URI_4 =
            "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure4";
    protected final static String TEST_PERSON_PROCEDURE_URI_5 =
            "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure5";

    @Before
    public void beforeEach() {
        LoggingControl.offLogs();
        setup();
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        resourceAPIPool.init();
        resourceAPIPool.reload();

        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        procedurePool.reload();

        assertEquals(0, resourceAPIPool.count());
        assertEquals(0, resourceAPIPool.obsoleteCount());

        assertEquals(0, procedurePool.count());
        assertEquals(0, procedurePool.obsoleteCount());
    }

    @After
    public void reset() {
        setup();

        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        resourceAPIPool.init();
        resourceAPIPool.reload();

        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        procedurePool.reload();

        assertEquals(0, resourceAPIPool.count());
        assertEquals(0, resourceAPIPool.obsoleteCount());

        assertEquals(0, procedurePool.count());
        assertEquals(0, procedurePool.obsoleteCount());
        LoggingControl.restoreLogs();
    }

    @Test
    public void testGetInstance() {
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        assertNotNull(resourceAPIPool);
        assertEquals(resourceAPIPool, ResourceAPIPool.getInstance());
    }

    @Test
    public void testGetBeforeInit() {
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        ResourceAPI resourceAPI = resourceAPIPool.get(TEST_RESOURCE_KEY);
        assertTrue(NullResourceAPI.getInstance().equals(resourceAPI));
    }

    @Test
    public void testPrintKeysBeforeInit() {
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        resourceAPIPool.printKeys();
        // nothing to assert
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        resourceAPIPool.reload();
        // not sure what to assert
    }

    @Test
    public void testInit() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();
        assertEquals(1, resourceAPIPool.count());
        assertEquals(0, resourceAPIPool.obsoleteCount());

        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(TEST_RESOURCE_KEY));
    }

    @Test
    public void testVersioning() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        ResourceAPIKey resouce_v0 = ResourceAPIKey.of("test_resource", "0");
        ResourceAPIKey resource_v1 = ResourceAPIKey.of("test_resource", "1");

        ResourceAPIKey document_v0 = ResourceAPIKey.of("test_document_resource", "0");
        ResourceAPIKey document_v1 = ResourceAPIKey.of("test_document_resource", "1");

        String testDocumentProcedureUri =
                "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testDocumentProcedure1";

        ResourceAPIKey person_v0 = ResourceAPIKey.of("test_person_resource", "0");
        ResourceAPIKey person_v1 = ResourceAPIKey.of("test_person_resource", "1");
        ResourceAPIKey person_v1_0 = ResourceAPIKey.of("test_person_resource", "1.0");
        ResourceAPIKey person_v1_0_0 = ResourceAPIKey.of("test_person_resource", "1.0.0");
        ResourceAPIKey person_v1_1 = ResourceAPIKey.of("test_person_resource", "1.1");
        ResourceAPIKey person_v1_1_0 = ResourceAPIKey.of("test_person_resource", "1.1.0");
        ResourceAPIKey person_v1_2 = ResourceAPIKey.of("test_person_resource", "1.2");
        ResourceAPIKey person_v2 = ResourceAPIKey.of("test_person_resource", "2");
        ResourceAPIKey person_v3 = ResourceAPIKey.of("test_person_resource", "3");
        ResourceAPIKey person_v4 = ResourceAPIKey.of("test_person_resource", "4");
        ResourceAPIKey person_v4_2 = ResourceAPIKey.of("test_person_resource", "4.2");
        ResourceAPIKey person_v4_3 = ResourceAPIKey.of("test_person_resource", "4.3");
        ResourceAPIKey person_v4_3_6 = ResourceAPIKey.of("test_person_resource", "4.3.6");
        ResourceAPIKey person_v4_3_7 = ResourceAPIKey.of("test_person_resource", "4.3.7");
        ResourceAPIKey person_v4_3_8 = ResourceAPIKey.of("test_person_resource", "4.3.8");
        ResourceAPIKey person_v4_4 = ResourceAPIKey.of("test_person_resource", "4.4");
        ResourceAPIKey person_v5 = ResourceAPIKey.of("test_person_resource", "5");

        ResourceAPIKey expectedDocument_v1_0_0 = ResourceAPIKey.of("test_document_resource", "1.0.0");

        ResourceAPIKey expectedPerson_v1_0_0 = TEST_PERSON_RESOURCE_KEY; // "test_person_resource", "1.0.0"
        ResourceAPIKey expectedPerson_v1_1_0 = ResourceAPIKey.of("test_person_resource", "1.1.0");
        ResourceAPIKey expectedPerson_v2_0_0 = ResourceAPIKey.of("test_person_resource", "2.0.0");
        ResourceAPIKey expectedPerson_v4_3_7 = ResourceAPIKey.of("test_person_resource", "4.3.7");

        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resource_v1));
        // base test for test_document
        assertTrue(resourceAPIPool.get(document_v0) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(document_v1) instanceof NullResourceAPI);
        // demonstrate no person resources in resource pool
        assertTrue(resourceAPIPool.get(person_v0) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v1) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v1_0) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v1_1) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v1_1_0) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v1_2) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v2) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v3) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4_2) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4_3) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4_3_6) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4_3_7) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4_3_8) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v4_4) instanceof NullResourceAPI);
        assertTrue(resourceAPIPool.get(person_v5) instanceof NullResourceAPI);

        loadTestModel();
        resourceAPIPool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resource_v1));
        // base test for test_document
        assertTrue(resourceAPIPool.get(document_v0) instanceof NullResourceAPI);
        assertResource(expectedDocument_v1_0_0, testDocumentProcedureUri, resourceAPIPool.get(document_v1));

        // no person version 0 in pool
        assertTrue(resourceAPIPool.get(person_v0) instanceof NullResourceAPI);
        // person resource version 1.0.0 has no max version, any major version request greater than 1 should return
        // version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v2));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v3));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v4));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v5));

        loadPersonVersion1_1Model();
        resourceAPIPool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resource_v1));
        // base test for test_document
        assertTrue(resourceAPIPool.get(document_v0) instanceof NullResourceAPI);
        assertResource(expectedDocument_v1_0_0, testDocumentProcedureUri, resourceAPIPool.get(document_v1));

        // no person version 0 in pool
        assertTrue(resourceAPIPool.get(person_v0) instanceof NullResourceAPI);
        // still able to get person version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1_0));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1_0_0));

        // able to get person version 1.1.0 from varying levels of specificity
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1_1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1_1_0));

        // person version 1 does not have specific minor version 2
        assertTrue(resourceAPIPool.get(person_v1_2) instanceof NullResourceAPI);

        // person resource version 1.1.0 has no max version, any major version request greater than 1 should return
        // version 1.1.0
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v2));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v3));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v4));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v5));

        loadPersonVersion2Model();
        resourceAPIPool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resource_v1));
        // base test for test_document
        assertTrue(resourceAPIPool.get(document_v0) instanceof NullResourceAPI);
        assertResource(expectedDocument_v1_0_0, testDocumentProcedureUri, resourceAPIPool.get(document_v1));

        // no person version 0 in pool
        assertTrue(resourceAPIPool.get(person_v0) instanceof NullResourceAPI);
        // still able to get person version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1_0));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1_0_0));

        // able to get person version 1.1.0 from varying levels of specificity
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1_1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1_1_0));

        // person version 1 does not have specific minor version 2
        assertTrue(resourceAPIPool.get(person_v1_2) instanceof NullResourceAPI);

        // able to get person version 2.0.0
        assertResource(expectedPerson_v2_0_0, TEST_PERSON_PROCEDURE_URI_2, resourceAPIPool.get(person_v2));

        // person resource version 2.0.0 has no max version, any major version request greater than 2 should return
        // version 2.0.0
        assertResource(expectedPerson_v2_0_0, TEST_PERSON_PROCEDURE_URI_2, resourceAPIPool.get(person_v3));
        assertResource(expectedPerson_v2_0_0, TEST_PERSON_PROCEDURE_URI_2, resourceAPIPool.get(person_v4));
        assertResource(expectedPerson_v2_0_0, TEST_PERSON_PROCEDURE_URI_2, resourceAPIPool.get(person_v5));

        loadPersonVersion4_3_7Model();
        resourceAPIPool.reload();
        // base test for test_resource
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resouce_v0));
        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(resource_v1));
        // base test for test_document
        assertTrue(resourceAPIPool.get(document_v0) instanceof NullResourceAPI);
        assertResource(expectedDocument_v1_0_0, testDocumentProcedureUri, resourceAPIPool.get(document_v1));

        // no person version 0 in pool
        assertTrue(resourceAPIPool.get(person_v0) instanceof NullResourceAPI);
        // still able to get person version 1.0.0
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1_0));
        assertResource(expectedPerson_v1_0_0, TEST_PERSON_PROCEDURE_URI_1, resourceAPIPool.get(person_v1_0_0));

        // able to get person version 1.1.0 from varying levels of specificity
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1_1));
        assertResource(expectedPerson_v1_1_0, TEST_PERSON_PROCEDURE_URI_1_1, resourceAPIPool.get(person_v1_1_0));

        // person version 1 does not have specific minor version 2
        assertTrue(resourceAPIPool.get(person_v1_2) instanceof NullResourceAPI);

        // still able to get person version 2.0.0
        assertResource(expectedPerson_v2_0_0, TEST_PERSON_PROCEDURE_URI_2, resourceAPIPool.get(person_v2));

        // skipped a version from 2.0.0 to 4.3.7 and 2.0.0 has max of 2.0.0
        assertTrue(resourceAPIPool.get(person_v3) instanceof NullResourceAPI);

        // no version 4.2 exists
        assertTrue(resourceAPIPool.get(person_v4_2) instanceof NullResourceAPI);
        // version 4.3.6 does not exist
        assertTrue(resourceAPIPool.get(person_v4_3_6) instanceof NullResourceAPI);
        // version 4.3.8 does not exist
        assertTrue(resourceAPIPool.get(person_v4_3_8) instanceof NullResourceAPI);

        // no version 4.4 exists
        assertTrue(resourceAPIPool.get(person_v4_4) instanceof NullResourceAPI);

        // able to get person version 4.3.7 at varying levels of specificity
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_PROCEDURE_URI_4, resourceAPIPool.get(person_v4));
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_PROCEDURE_URI_4, resourceAPIPool.get(person_v4_3));
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_PROCEDURE_URI_4, resourceAPIPool.get(person_v4_3_7));

        // person resource version 4.3.7 has no max version, any major version request greater than 4 should return
        // version 4.0.0
        assertResource(expectedPerson_v4_3_7, TEST_PERSON_PROCEDURE_URI_4, resourceAPIPool.get(person_v5));
    }

    @Test
    public void testPrintKeys() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        resourceAPIPool.printKeys();
        // nothing to assert
    }

    @Test
    public void testAdd() throws IOException, ConfigurationBeanLoaderException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        ResourceAPI resourceAPI = loader.loadInstance(TEST_PERSON_RESOURCE_URI, ResourceAPI.class);

        resourceAPI.setUri(TEST_PERSON_RESOURCE_URI);

        resourceAPIPool.add(TEST_PERSON_RESOURCE_URI, resourceAPI);

        assertEquals(0, resourceAPIPool.obsoleteCount());

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_PROCEDURE_URI_1,
                resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        resourceAPIPool.reload();

        ResourceAPI resourceAPI = loader.loadInstance(TEST_PERSON_RESOURCE_URI, ResourceAPI.class);

        assertEquals(0, resourceAPIPool.obsoleteCount());

        ResourceAPI resourceAPIHasClient = resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY);

        resourceAPIPool.add(TEST_PERSON_RESOURCE_URI, resourceAPI);

        assertEquals(1, resourceAPIPool.obsoleteCount());

        resourceAPIHasClient.removeClient();
    }

    @Ignore
    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        ResourceAPI resourceAPI = loader.loadInstance(TEST_PERSON_RESOURCE_URI, ResourceAPI.class);

        reset();

        assertTrue(resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY) instanceof NullResourceAPI);

        resourceAPIPool.add(TEST_PERSON_RESOURCE_URI, resourceAPI);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        resourceAPIPool.reload();

        ResourceAPI resourceAPI = resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY);

        resourceAPI.removeClient();

        assertFalse(resourceAPI instanceof NullResourceAPI);

        reset();

        resourceAPIPool.unload(TEST_PERSON_RESOURCE_URI);

        assertEquals(0, resourceAPIPool.obsoleteCount());

        assertTrue(resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY) instanceof NullResourceAPI);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        resourceAPIPool.reload();

        ResourceAPI resourceAPIHasClient = resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY);

        assertFalse(resourceAPIHasClient instanceof NullResourceAPI);

        setup();

        resourceAPIPool.init();

        resourceAPIPool.unload(TEST_PERSON_RESOURCE_URI);

        assertEquals(1, resourceAPIPool.obsoleteCount());

        assertTrue(resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY) instanceof NullResourceAPI);

        resourceAPIHasClient.removeClient();
    }

    @Test
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        resourceAPIPool.reload();

        resourceAPIPool.unload(TEST_PERSON_RESOURCE_URI);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        ResourceAPI resourceAPI = resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY);

        assertTrue(NullResourceAPI.getInstance().equals(resourceAPI));

        resourceAPI.removeClient();

        resourceAPIPool.load(TEST_PERSON_RESOURCE_URI);

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_PROCEDURE_URI_1,
                resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testReload() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        ResourceAPI actualResourceAPI = resourceAPIPool.get(TEST_RESOURCE_KEY);

        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, actualResourceAPI);

        loadTestModel();

        actualResourceAPI.removeClient();

        resourceAPIPool.reload();

        assertEquals(8, resourceAPIPool.count());
        assertEquals(0, resourceAPIPool.obsoleteCount());

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_PROCEDURE_URI_1,
                resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testUnload() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        ResourceAPI actualResourceAPI = resourceAPIPool.get(TEST_RESOURCE_KEY);

        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, actualResourceAPI);

        actualResourceAPI.removeClient();

        resourceAPIPool.unload();

        assertEquals(0, resourceAPIPool.count());
    }

    @Test
    public void testUnloadUri() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        long resourceCount = resourceAPIPool.count();

        ResourceAPI actualResourceAPI = resourceAPIPool.get(TEST_RESOURCE_KEY);

        String uri = actualResourceAPI.getUri();

        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, actualResourceAPI);

        resourceAPIPool.unload(uri);

        assertEquals(resourceCount - 1, resourceAPIPool.count());
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(TEST_RESOURCE_KEY));

        loadTestModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourceAPIPool.reload());

        while (!reloadFuture.isDone()) {
            assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(TEST_RESOURCE_KEY));
        }

        assertResource(TEST_RESOURCE_KEY, TEST_PROCEDURE_URI, resourceAPIPool.get(TEST_RESOURCE_KEY));

        assertResource(TEST_PERSON_RESOURCE_KEY, TEST_PERSON_PROCEDURE_URI_1,
                resourceAPIPool.get(TEST_PERSON_RESOURCE_KEY));
    }

    @Test
    public void testRealodOfResourceHasClient() throws IOException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        loadTestModel();

        ResourceAPI resourceAPI = resourceAPIPool.get(TEST_RESOURCE_KEY);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> resourceAPIPool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_RESOURCE_KEY, resourceAPI.getKey());
        }

        resourceAPI.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ResourceAPIPool resourceAPIPool = initWithDefaultModel();

        resourceAPIPool.reload();

        long initalCount = resourceAPIPool.obsoleteCount();
        ResourceAPI resourceAPI = resourceAPIPool.get(TEST_RESOURCE_KEY);

        resourceAPI.removeClient();

        assertFalse(resourceAPI.hasClients());

        Thread t1 = getResourceInThread(resourceAPIPool, TEST_RESOURCE_KEY);

        t1.join();

        assertTrue(resourceAPI.hasClients());

        resourceAPIPool.reload();

        assertEquals(initalCount, resourceAPIPool.obsoleteCount());
    }

    private Thread getResourceInThread(ResourceAPIPool resourceAPIPool, ResourceAPIKey resourceAPIKey) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                ResourceAPI resourceAPI = resourceAPIPool.get(resourceAPIKey);
                assertEquals(resourceAPIKey, resourceAPI.getKey());
                assertTrue(resourceAPI.hasClients());
            }
        };
        Thread thread = new Thread(client);
        thread.start();
        return thread;
    }

    private ResourceAPIPool initWithDefaultModel() throws IOException {
        loadDefaultModel();

        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        resourceAPIPool.init();

        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();

        return resourceAPIPool;
    }

    private void assertResource(ResourceAPIKey expctedResourceAPIKey, String expectedActionName,
            ResourceAPI actualResourceAPI) {
        try {
            assertNotNull(actualResourceAPI);
            assertFalse(format("%s not loaded!", expctedResourceAPIKey), actualResourceAPI instanceof NullResourceAPI);
            assertEquals(expctedResourceAPIKey, actualResourceAPI.getKey());
            assertTrue(actualResourceAPI.hasClients());
            assertEquals(expectedActionName, actualResourceAPI.getProcedureUriOnGet());
            assertEquals(expctedResourceAPIKey.getVersion().toString(), actualResourceAPI.getVersionMin());
            assertEquals(expectedActionName, actualResourceAPI.getProcedureUriOnPost());
            assertEquals(expctedResourceAPIKey.getVersion().toString(), actualResourceAPI.getVersionMin());
            assertEquals(expectedActionName, actualResourceAPI.getProcedureUriOnDelete());
            assertEquals(expctedResourceAPIKey.getVersion().toString(), actualResourceAPI.getVersionMin());
            assertEquals(expectedActionName, actualResourceAPI.getProcedureUriOnPut());
            assertEquals(expctedResourceAPIKey.getVersion().toString(), actualResourceAPI.getVersionMin());
            assertEquals(expectedActionName, actualResourceAPI.getProcedureUriOnPatch());
            assertEquals(expctedResourceAPIKey.getVersion().toString(), actualResourceAPI.getVersionMin());
        } finally {
            actualResourceAPI.removeClient();
        }

    }

}
