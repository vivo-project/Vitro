package edu.cornell.mannlib.vitro.webapp.dynapi;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcedurePoolTest extends ServletContextTest {

    protected final static String TEST_PERSON_ACTION_URI =
            "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure1";

    @After
    public void reset() {
        setup();

        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init(servletContext);
        procedurePool.reload();

        assertEquals(0, procedurePool.count());
        assertEquals(0, procedurePool.obsoleteCount());
        LoggingControl.restoreLogs();
    }

    @Before
    public void before() {
        LoggingControl.offLogs();
    }

    @Test
    public void testGetInstance() {
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        assertNotNull(procedurePool);
        assertEquals(procedurePool, ProcedurePool.getInstance());
    }

    @Test
    public void testGetBeforeInit() {
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        Procedure procedure = procedurePool.get(TEST_PROCEDURE_URI);
        assertNotNull(procedure);
        assertTrue(procedure instanceof NullProcedure);
    }

    @Test
    public void testPrintKeysBeforeInit() {
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.printKeys();
        // nothing to assert
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.reload();
        // not sure what to assert
    }

    @Test
    public void testInit() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();
        assertEquals(1, procedurePool.count());
        assertEquals(0, procedurePool.obsoleteCount());

        assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));
    }

    @Test
    public void testPrintKeys() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        procedurePool.printKeys();
        // nothing to assert
    }

    @Test
    public void testAdd() throws IOException, ConfigurationBeanLoaderException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        Procedure procedure = loader.loadInstance(TEST_PERSON_ACTION_URI, Procedure.class);

        procedure.setUri(TEST_PERSON_ACTION_URI);

        procedurePool.add(TEST_PERSON_ACTION_URI, procedure);

        assertEquals(0, procedurePool.obsoleteCount());

        assertProcedure(TEST_PERSON_ACTION_URI, procedurePool.get(TEST_PERSON_ACTION_URI));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        procedurePool.reload();

        Procedure procedure = loader.loadInstance(TEST_PERSON_ACTION_URI, Procedure.class);

        assertEquals(0, procedurePool.obsoleteCount());

        Procedure procedureHasClient = procedurePool.get(TEST_PERSON_ACTION_URI);

        procedurePool.add(TEST_PERSON_ACTION_URI, procedure);

        assertEquals(1, procedurePool.obsoleteCount());

        procedureHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        Procedure procedure = loader.loadInstance(TEST_PERSON_ACTION_URI, Procedure.class);

        reset();

        assertTrue(procedurePool.get(TEST_PERSON_PROCEDURE_URI_1) instanceof NullProcedure);

        procedurePool.add(TEST_PERSON_ACTION_URI, procedure);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        procedurePool.reload();

        Procedure procedure = procedurePool.get(TEST_PERSON_ACTION_URI);

        assertFalse(procedure instanceof NullProcedure);

        procedure.removeClient();

        reset();

        procedurePool.unload(TEST_PERSON_ACTION_URI);

        assertEquals(0, procedurePool.obsoleteCount());

        assertTrue(procedurePool.get(TEST_PERSON_ACTION_URI) instanceof NullProcedure);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        procedurePool.reload();

        Procedure procedureHasClient = procedurePool.get(TEST_PERSON_ACTION_URI);

        assertFalse(procedureHasClient instanceof NullProcedure);

        setup();

        procedurePool.init(servletContext);

        procedurePool.unload(TEST_PERSON_ACTION_URI);

        assertEquals(1, procedurePool.obsoleteCount());

        assertTrue(procedurePool.get(TEST_PERSON_ACTION_URI) instanceof NullProcedure);

        procedureHasClient.removeClient();
    }

    @Test
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        procedurePool.reload();

        procedurePool.unload(TEST_PERSON_ACTION_URI);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        Procedure procedure = procedurePool.get(TEST_PERSON_PROCEDURE_URI_1);

        assertTrue(procedure instanceof NullProcedure);

        procedurePool.load(TEST_PERSON_ACTION_URI);

        assertProcedure(TEST_PERSON_ACTION_URI, procedurePool.get(TEST_PERSON_ACTION_URI));
    }

    @Test
    public void testReload() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));

        loadTestModel();

        procedurePool.reload();

        assertEquals(8, procedurePool.count());
        assertEquals(0, procedurePool.obsoleteCount());

        assertProcedure(TEST_PERSON_ACTION_URI, procedurePool.get(TEST_PERSON_ACTION_URI));
    }

    @Test
    public void testUnload() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));

        procedurePool.unload();

        assertEquals(0, procedurePool.count());
    }

    @Test
    public void testUnloadUri() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        long procedureCount = procedurePool.count();

        assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));

        procedurePool.unload("https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testProcedure1");

        assertEquals(procedureCount - 1, procedurePool.count());
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));

        loadTestModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> procedurePool.reload());

        while (!reloadFuture.isDone()) {
            assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));
        }

        assertProcedure(TEST_PROCEDURE_URI, procedurePool.get(TEST_PROCEDURE_URI));

        assertProcedure(TEST_PERSON_ACTION_URI, procedurePool.get(TEST_PERSON_ACTION_URI));
    }

    @Test
    public void testRealodOfActionHasClient() throws IOException {
        ProcedurePool procedurePool = initWithDefaultModel();

        loadTestModel();

        Procedure procedure = procedurePool.get(TEST_PROCEDURE_URI);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> procedurePool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_PROCEDURE_URI, procedure.getKey());
        }

        procedure.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ProcedurePool procedurePool = initWithDefaultModel();

        procedurePool.reload();

        long initalCount = procedurePool.obsoleteCount();
        Procedure procedure = procedurePool.get(TEST_PROCEDURE_URI);

        procedure.removeClient();

        assertFalse(procedure.hasClients());

        Thread t1 = getProcedureInThread(procedurePool, TEST_PROCEDURE_URI);

        t1.join();

        assertTrue(procedure.hasClients());

        procedurePool.reload();

        assertEquals(initalCount, procedurePool.obsoleteCount());
    }

    private Thread getProcedureInThread(ProcedurePool procedurePool, String name) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                Procedure procedure = procedurePool.get(name);
                assertEquals(name, procedure.getKey());
                assertTrue(procedure.hasClients());
            }
        };
        Thread thread = new Thread(client);
        thread.start();
        return thread;
    }

    private ProcedurePool initWithDefaultModel() throws IOException {
        loadDefaultModel();

        ProcedurePool procedurePool = ProcedurePool.getInstance();

        procedurePool.init(servletContext);

        return procedurePool;
    }

    private void assertProcedure(String expectedName, Procedure actualProcedure) {
        assertNotNull(actualProcedure);
        assertFalse(format("%s not loaded!", expectedName), NullProcedure.getInstance().equals(actualProcedure));
        assertTrue(actualProcedure.isValid());
        assertEquals(expectedName, actualProcedure.getKey());
        assertTrue(actualProcedure.hasClients());
        actualProcedure.removeClient();
    }

}
