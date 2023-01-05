package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

public class ActionPoolBulkOperationTest extends ServletContextTest{

    private final static String TEST_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/action/testAction1";
    private ActionPool actionPool;
    
    @Before
    public void preparePool() {
       actionPool = initWithDefaultModel();
    }
    
    @After
    public void reset() {
        setup();
        actionPool = initWithDefaultModel();
        assertEquals(1, actionPool.count());
    }
    
    @Test
    public void componentLoadUnloadTest() throws InitializationException {
        assertEquals(1, actionPool.count());
        PoolBulkOperation apao = new ActionPoolBulkOperation();
        DataStore dataStore = new DataStore();

        apao.setOperationType(PoolOperation.OperationType.UNLOAD.toString());
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(0, actionPool.count());
        
        apao.setOperationType(PoolOperation.OperationType.LOAD.toString());
         result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, actionPool.count());
    }
    
    @Test
    public void componentReloadTest() throws InitializationException {
        assertEquals(1, actionPool.count());
        Action action1 = null;
        Action action2 = null;
        try {
        action1 = actionPool.getByUri(TEST_ACTION_URI);
        ActionPoolBulkOperation apao = new ActionPoolBulkOperation();
        apao.setOperationType(PoolOperation.OperationType.RELOAD.toString());
        DataStore dataStore = new DataStore();
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, actionPool.count());
        action2 = actionPool.getByUri(TEST_ACTION_URI);
        assertNotEquals(action1, action2);
        } finally {
            if (action1 != null) {
                action1.removeClient();    
            }
            if (action2 != null) {
                action2.removeClient();    
            }
        }
    }

    
    private ActionPool initWithDefaultModel() {
        try {
            loadDefaultModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);
        return actionPool;
    }
}
