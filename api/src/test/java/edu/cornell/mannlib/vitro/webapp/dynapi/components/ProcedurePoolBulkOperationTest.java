package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.PoolBulkOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.PoolOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.ProcedurePoolBulkOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

public class ProcedurePoolBulkOperationTest extends ServletContextTest{

    private final static String TEST_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testProcedure1";
    private ProcedurePool procedurePool;
    
    @Before
    public void preparePool() {
        LoggingControl.offLogs();
        procedurePool = initWithDefaultModel();
    }
    
    @After
    public void reset() {
        setup();
        procedurePool = initWithDefaultModel();
        assertEquals(1, procedurePool.count());
        LoggingControl.restoreLogs();
    }
    
    @Test
    public void componentLoadUnloadTest() throws InitializationException {
        assertEquals(1, procedurePool.count());
        PoolBulkOperation apao = new ProcedurePoolBulkOperation();
        DataStore dataStore = new DataStore();

        apao.setOperationType(PoolOperation.OperationType.UNLOAD.toString());
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(0, procedurePool.count());
        
        apao.setOperationType(PoolOperation.OperationType.LOAD.toString());
         result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, procedurePool.count());
    }
    
    @Test
    public void componentReloadTest() throws InitializationException {
        assertEquals(1, procedurePool.count());
        Procedure action1 = null;
        Procedure action2 = null;
        try {
        action1 = procedurePool.getByUri(TEST_ACTION_URI);
        ProcedurePoolBulkOperation apao = new ProcedurePoolBulkOperation();
        apao.setOperationType(PoolOperation.OperationType.RELOAD.toString());
        DataStore dataStore = new DataStore();
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, procedurePool.count());
        action2 = procedurePool.getByUri(TEST_ACTION_URI);
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

    
    private ProcedurePool initWithDefaultModel() {
        try {
            loadDefaultModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProcedurePool actionPool = ProcedurePool.getInstance();
        actionPool.init(servletContext);
        return actionPool;
    }
}
