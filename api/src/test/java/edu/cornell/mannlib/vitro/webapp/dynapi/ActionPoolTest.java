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

public class ActionPoolTest extends ServletContextTest {

    protected final static String TEST_PERSON_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/action/testPersonAction1";

    @After
    public void reset() {
        setup();

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);
        actionPool.reload();

        assertEquals(0, actionPool.count());
        assertEquals(0, actionPool.obsoleteCount());
    }

    @Test
    public void testGetInstance() {
        ActionPool actionPool = ActionPool.getInstance();
        assertNotNull(actionPool);
        assertEquals(actionPool, ActionPool.getInstance());
    }

    @Test
    public void testGetBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        Action action = actionPool.get(TEST_ACTION_NAME);
        assertNotNull(action);
        assertTrue(action instanceof NullAction);
    }

    @Test
    public void testPrintKeysBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.printKeys();
        // nothing to assert
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.reload();
        // not sure what to assert
    }

    @Test
    public void testInit() throws IOException {
        ActionPool actionPool = initWithDefaultModel();
        assertEquals(1, actionPool.count());
        assertEquals(0, actionPool.obsoleteCount());

        assertAction(TEST_ACTION_NAME, actionPool.get(TEST_ACTION_NAME));
    }

    @Test
    public void testPrintKeys() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        actionPool.printKeys();
        // nothing to assert
    }

    @Test
    public void testAdd() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        Action action = loader.loadInstance(TEST_PERSON_ACTION_URI, Action.class);

        actionPool.add(TEST_PERSON_ACTION_URI, action);

        assertEquals(0, actionPool.obsoleteCount());

        assertAction(TEST_PERSON_ACTION_NAME, actionPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        actionPool.reload();

        Action action = loader.loadInstance(TEST_PERSON_ACTION_URI, Action.class);

        assertEquals(0, actionPool.obsoleteCount());

        Action actionHasClient = actionPool.get(TEST_PERSON_ACTION_NAME);

        actionPool.add(TEST_PERSON_ACTION_URI, action);

        assertEquals(1, actionPool.obsoleteCount());

        actionHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        Action action = loader.loadInstance(TEST_PERSON_ACTION_URI, Action.class);

        reset();

        assertTrue(actionPool.get(TEST_PERSON_ACTION_NAME) instanceof NullAction);

        actionPool.add(TEST_PERSON_ACTION_URI, action);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        actionPool.reload();

        Action action = actionPool.get(TEST_PERSON_ACTION_NAME);

        assertFalse(action instanceof NullAction);

        action.removeClient();

        reset();

        actionPool.unload(TEST_PERSON_ACTION_URI);

        assertEquals(0, actionPool.obsoleteCount());

        assertTrue(actionPool.get(TEST_PERSON_ACTION_NAME) instanceof NullAction);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        actionPool.reload();

        Action actionHasClient = actionPool.get(TEST_PERSON_ACTION_NAME);

        assertFalse(actionHasClient instanceof NullAction);

        setup();

        actionPool.init(servletContext);

        actionPool.unload(TEST_PERSON_ACTION_URI);

        assertEquals(1, actionPool.obsoleteCount());

        assertTrue(actionPool.get(TEST_PERSON_ACTION_NAME) instanceof NullAction);

        actionHasClient.removeClient();
    }

    @Test
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        actionPool.reload();

        actionPool.unload(TEST_PERSON_ACTION_URI);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        Action action = actionPool.get(TEST_PERSON_ACTION_NAME);

        assertTrue(action instanceof NullAction);

        actionPool.load(TEST_PERSON_ACTION_URI);

        assertAction(TEST_PERSON_ACTION_NAME, actionPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testReload() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        assertAction(TEST_ACTION_NAME, actionPool.get(TEST_ACTION_NAME));

        loadTestModel();

        actionPool.reload();

        assertEquals(8, actionPool.count());
        assertEquals(0, actionPool.obsoleteCount());

        assertAction(TEST_PERSON_ACTION_NAME, actionPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        assertAction(TEST_ACTION_NAME, actionPool.get(TEST_ACTION_NAME));

        loadTestModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> actionPool.reload());

        while (!reloadFuture.isDone()) {
            assertAction(TEST_ACTION_NAME, actionPool.get(TEST_ACTION_NAME));
        }

        assertAction(TEST_ACTION_NAME, actionPool.get(TEST_ACTION_NAME));

        assertAction(TEST_PERSON_ACTION_NAME, actionPool.get(TEST_PERSON_ACTION_NAME));
    }

    @Test
    public void testRealodOfActionHasClient() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        loadTestModel();

        Action action = actionPool.get(TEST_ACTION_NAME);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> actionPool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_ACTION_NAME, action.getKey());
        }

        action.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ActionPool actionPool = initWithDefaultModel();

        actionPool.reload();

        long initalCount = actionPool.obsoleteCount();
        Action action = actionPool.get(TEST_ACTION_NAME);

        action.removeClient();

        assertFalse(action.hasClients());

        Thread t1 = getActionInThread(actionPool, TEST_ACTION_NAME);

        t1.join();

        assertTrue(action.hasClients());

        actionPool.reload();

        assertEquals(initalCount, actionPool.obsoleteCount());
    }

    private Thread getActionInThread(ActionPool actionPool, String name) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                Action action = actionPool.get(name);
                assertEquals(name, action.getKey());
                assertTrue(action.hasClients());
            }
        };
        Thread thread = new Thread(client);
        thread.start();
        return thread;
    }

    private ActionPool initWithDefaultModel() throws IOException {
        loadDefaultModel();

        ActionPool actionPool = ActionPool.getInstance();

        actionPool.init(servletContext);

        return actionPool;
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
