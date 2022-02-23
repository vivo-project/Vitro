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
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultAction;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public class ActionPoolTest extends ServletContextTest {

    protected final static String TEST_RELOAD_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/action/testReloadAction1";

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
    public void testGetByNameBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        Action action = actionPool.getByName(TEST_ACTION_NAME);
        assertNotNull(action);
        assertTrue(action instanceof DefaultAction);
    }

    @Test
    public void testPrintNamesBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.printNames();
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

        assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);
    }

    @Test
    public void testPrintNames() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        actionPool.printNames();
        // nothing to assert
    }

    @Test
    public void testAdd() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        Action action = loader.loadInstance(TEST_RELOAD_ACTION_URI, Action.class);

        actionPool.add(TEST_RELOAD_ACTION_URI, action);

        assertEquals(0, actionPool.obsoleteCount());

        assertActionByName(actionPool.getByName(TEST_RELOAD_ACTION_NAME), TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testAddHasClient() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        actionPool.reload();

        Action action = loader.loadInstance(TEST_RELOAD_ACTION_URI, Action.class);

        assertEquals(0, actionPool.obsoleteCount());

        Action actionHasClient = actionPool.getByName(TEST_RELOAD_ACTION_NAME);

        actionPool.add(TEST_RELOAD_ACTION_URI, action);

        assertEquals(1, actionPool.obsoleteCount());

        actionHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithoutModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        Action action = loader.loadInstance(TEST_RELOAD_ACTION_URI, Action.class);

        reset();

        assertTrue(actionPool.getByName(TEST_RELOAD_ACTION_NAME) instanceof DefaultAction);

        actionPool.add(TEST_RELOAD_ACTION_URI, action);
    }

    @Test
    public void testRemove() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        actionPool.reload();

        Action action = actionPool.getByName(TEST_RELOAD_ACTION_NAME);

        assertFalse(action instanceof DefaultAction);

        action.removeClient();

        reset();

        actionPool.remove(TEST_RELOAD_ACTION_URI, TEST_RELOAD_ACTION_NAME);

        assertEquals(0, actionPool.obsoleteCount());

        assertTrue(actionPool.getByName(TEST_RELOAD_ACTION_NAME) instanceof DefaultAction);
    }

    @Test
    public void testRemoveHasClient() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        actionPool.reload();

        Action actionHasClient = actionPool.getByName(TEST_RELOAD_ACTION_NAME);

        assertFalse(actionHasClient instanceof DefaultAction);

        setup();

        actionPool.init(servletContext);

        actionPool.remove(TEST_RELOAD_ACTION_URI, TEST_RELOAD_ACTION_NAME);

        assertEquals(1, actionPool.obsoleteCount());

        assertTrue(actionPool.getByName(TEST_RELOAD_ACTION_NAME) instanceof DefaultAction);

        actionHasClient.removeClient();
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveWithModelLoaded() throws IOException, ConfigurationBeanLoaderException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        actionPool.reload();

        actionPool.remove(TEST_RELOAD_ACTION_URI, TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testReloadSingle() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        Action action = actionPool.getByName(TEST_RELOAD_ACTION_NAME);

        assertTrue(action instanceof DefaultAction);

        actionPool.reload(TEST_RELOAD_ACTION_URI);

        assertActionByName(actionPool.getByName(TEST_RELOAD_ACTION_NAME), TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testReload() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);

        loadReloadModel();

        actionPool.reload();

        assertEquals(2, actionPool.count());
        assertEquals(0, actionPool.obsoleteCount());

        assertActionByName(actionPool.getByName(TEST_RELOAD_ACTION_NAME), TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testReloadThreadSafety() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);

        loadReloadModel();

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> actionPool.reload());

        while (!reloadFuture.isDone()) {
            assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);
        }

        assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);

        assertActionByName(actionPool.getByName(TEST_RELOAD_ACTION_NAME), TEST_RELOAD_ACTION_NAME);
    }

    @Test
    public void testRealodOfActionHasClient() throws IOException {
        ActionPool actionPool = initWithDefaultModel();

        loadReloadModel();

        Action action = actionPool.getByName(TEST_ACTION_NAME);

        CompletableFuture<Void> reloadFuture = CompletableFuture.runAsync(() -> actionPool.reload());

        while (!reloadFuture.isDone()) {
            assertEquals(TEST_ACTION_NAME, action.getName());
        }

        action.removeClient();
    }

    @Test
    public void testClientsManagement() throws IOException, InterruptedException {
        ActionPool actionPool = initWithDefaultModel();

        actionPool.reload();

        long initalCount = actionPool.obsoleteCount();
        Action action = actionPool.getByName(TEST_ACTION_NAME);

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
                Action action = actionPool.getByName(name);
                assertEquals(name, action.getName());
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

    private void assertActionByName(Action action, String name) {
        assertNotNull(action);
        assertFalse(format("%s not loaded!", name), action instanceof DefaultAction);
        assertTrue(action.isValid());
        assertEquals(name, action.getName());
        assertTrue(action.hasClients());
        action.removeClient();
    }

}
