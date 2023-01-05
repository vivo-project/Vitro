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

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullAction;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public class RPCPoolTest extends ServletContextTest {

    protected final static String TEST_PERSON_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/action/testPersonAction1";

    @After
    public void reset() {
        setup();

        RPCPool rpcPool = RPCPool.getInstance();
        rpcPool.init(servletContext);
        rpcPool.reload();

        assertEquals(0, rpcPool.count());
        assertEquals(0, rpcPool.obsoleteCount());
    }

    @Test
    public void testGetInstance() {
        RPCPool rpcPool = RPCPool.getInstance();
        assertNotNull(rpcPool);
        assertEquals(rpcPool, RPCPool.getInstance());
    }

    @Test
    public void testGetBeforeInit() {
        RPCPool rpcPool = RPCPool.getInstance();
        Action action = rpcPool.get(TEST_ACTION_NAME);
        assertNotNull(action);
        assertTrue(action instanceof NullAction);
    }

    @Test
    public void testPrintKeysBeforeInit() {
        RPCPool rpcPool = RPCPool.getInstance();
        rpcPool.printKeys();
        // nothing to assert
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        RPCPool rpcPool = RPCPool.getInstance();
        rpcPool.reload();
        // not sure what to assert
    }

    @Test
    public void testInit() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();
        assertEquals(1, rpcPool.count());
        assertEquals(0, rpcPool.obsoleteCount());

        assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));
    }

    @Test
    public void testPrintKeys() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        rpcPool.printKeys();
        // nothing to assert
    }

    @Test
    public void testAdd() throws IOException, ConfigurationBeanLoaderException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        Action action = loader.loadInstance(TEST_PERSON_ACTION_URI, Action.class);

        rpcPool.add(TEST_PERSON_ACTION_URI, action);

        assertEquals(0, rpcPool.obsoleteCount());

        assertAction(TEST_PERSON_ACTION_NAME, rpcPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        rpcPool.reload();

        Action action = loader.loadInstance(TEST_PERSON_ACTION_URI, Action.class);

        assertEquals(0, rpcPool.obsoleteCount());

        Action actionHasClient = rpcPool.get(TEST_PERSON_ACTION_NAME);

        rpcPool.add(TEST_PERSON_ACTION_URI, action);

        assertEquals(1, rpcPool.obsoleteCount());

        actionHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        Action action = loader.loadInstance(TEST_PERSON_ACTION_URI, Action.class);

        reset();

        assertTrue(rpcPool.get(TEST_PERSON_ACTION_NAME) instanceof NullAction);

        rpcPool.add(TEST_PERSON_ACTION_URI, action);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        rpcPool.reload();

        Action action = rpcPool.get(TEST_PERSON_ACTION_NAME);

        assertFalse(action instanceof NullAction);

        action.removeClient();

        reset();

        rpcPool.unload(TEST_PERSON_ACTION_URI);

        assertEquals(0, rpcPool.obsoleteCount());

        assertTrue(rpcPool.get(TEST_PERSON_ACTION_NAME) instanceof NullAction);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        rpcPool.reload();

        Action actionHasClient = rpcPool.get(TEST_PERSON_ACTION_NAME);

        assertFalse(actionHasClient instanceof NullAction);

        setup();

        rpcPool.init(servletContext);

        rpcPool.unload(TEST_PERSON_ACTION_URI);

        assertEquals(1, rpcPool.obsoleteCount());

        assertTrue(rpcPool.get(TEST_PERSON_ACTION_NAME) instanceof NullAction);

        actionHasClient.removeClient();
    }

    @Test
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        rpcPool.reload();

        rpcPool.unload(TEST_PERSON_ACTION_URI);
    }

    @Test
    public void testReloadSingle() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        Action action = rpcPool.get(TEST_PERSON_ACTION_NAME);

        assertTrue(action instanceof NullAction);

        rpcPool.load(TEST_PERSON_ACTION_URI);

        assertAction(TEST_PERSON_ACTION_NAME, rpcPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testReload() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));

        loadTestModel();

        rpcPool.reload();

        assertEquals(8, rpcPool.count());
        assertEquals(0, rpcPool.obsoleteCount());

        assertAction(TEST_PERSON_ACTION_NAME, rpcPool.get(TEST_PERSON_ACTION_NAME));
    }
    
    @Test
    public void testUnload() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));

        rpcPool.unload();

        assertEquals(0, rpcPool.count());
    }
    
    @Test
    public void testUnloadUri() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        long actionCount = rpcPool.count();
        
        assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));

        rpcPool.unload("https://vivoweb.org/ontology/vitro-dynamic-api/action/testAction1");

        assertEquals(actionCount - 1, rpcPool.count());
    }


    @Test
    public void testReloadThreadSafety() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));

        loadTestModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> rpcPool.reload());

        while (!reloadFuture.isDone()) {
            assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));
        }

        assertAction(TEST_ACTION_NAME, rpcPool.get(TEST_ACTION_NAME));

        assertAction(TEST_PERSON_ACTION_NAME, rpcPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testRealodOfActionHasClient() throws IOException {
        RPCPool rpcPool = initWithDefaultModel();

        loadTestModel();

        Action action = rpcPool.get(TEST_ACTION_NAME);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> rpcPool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_ACTION_NAME, action.getKey());
        }

        action.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        RPCPool rpcPool = initWithDefaultModel();

        rpcPool.reload();

        long initalCount = rpcPool.obsoleteCount();
        Action action = rpcPool.get(TEST_ACTION_NAME);

        action.removeClient();

        assertFalse(action.hasClients());

        Thread t1 = getActionInThread(rpcPool, TEST_ACTION_NAME);

        t1.join();

        assertTrue(action.hasClients());

        rpcPool.reload();

        assertEquals(initalCount, rpcPool.obsoleteCount());
    }

    private Thread getActionInThread(RPCPool rpcPool, String name) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                Action action = rpcPool.get(name);
                assertEquals(name, action.getKey());
                assertTrue(action.hasClients());
            }
        };
        Thread thread = new Thread(client);
        thread.start();
        return thread;
    }

    private RPCPool initWithDefaultModel() throws IOException {
        loadDefaultModel();

        RPCPool rpcPool = RPCPool.getInstance();

        rpcPool.init(servletContext);

        return rpcPool;
    }

    private void assertAction(String expectedName, Action actualAction) {
        assertNotNull(actualAction);
        assertFalse(format("%s not loaded!", expectedName), actualAction instanceof NullAction);
        assertTrue(actualAction.isValid());
        assertEquals(expectedName, actualAction.getKey());
        assertTrue(actualAction.hasClients());
        actualAction.removeClient();
    }

}
