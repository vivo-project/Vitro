package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.RPCPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

public class RPCPoolBulkOperationTest extends ServletContextTest{

    private final static String TEST_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/action/testAction1";
    private RPCPool rpcPool;
    
    @Before
    public void preparePool() {
       rpcPool = initWithDefaultModel();
    }
    
    @After
    public void reset() {
        setup();
        rpcPool = initWithDefaultModel();
        assertEquals(1, rpcPool.count());
    }
    
    @Test
    public void componentLoadUnloadTest() throws InitializationException {
        assertEquals(1, rpcPool.count());
        PoolBulkOperation apao = new RPCPoolBulkOperation();
        DataStore dataStore = new DataStore();

        apao.setOperationType(PoolOperation.OperationType.UNLOAD.toString());
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(0, rpcPool.count());
        
        apao.setOperationType(PoolOperation.OperationType.LOAD.toString());
         result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, rpcPool.count());
    }
    
    @Test
    public void componentReloadTest() throws InitializationException {
        assertEquals(1, rpcPool.count());
        Action action1 = null;
        Action action2 = null;
        try {
        action1 = rpcPool.getByUri(TEST_ACTION_URI);
        RPCPoolBulkOperation apao = new RPCPoolBulkOperation();
        apao.setOperationType(PoolOperation.OperationType.RELOAD.toString());
        DataStore dataStore = new DataStore();
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, rpcPool.count());
        action2 = rpcPool.getByUri(TEST_ACTION_URI);
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

    
    private RPCPool initWithDefaultModel() {
        try {
            loadDefaultModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RPCPool rpcPool = RPCPool.getInstance();
        rpcPool.init(servletContext);
        return rpcPool;
    }
}
