package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.ResourceAPIPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.PoolOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.ResourceAPIPoolBulkOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourceAPIPoolBulkOperationTest extends ServletContextTest {

    private final static String TEST_RESOURCE_URI =
            "https://vivoweb.org/ontology/vitro-dynamic-api/resourceAPI/testResource1";
    private ResourceAPIPool resourcePool;

    @Before
    public void preparePool() {
        LoggingControl.offLogs();
        resourcePool = initWithDefaultModel();
    }

    @After
    public void reset() {
        setup();
        resourcePool = initWithDefaultModel();
        assertEquals(1, resourcePool.count());
        LoggingControl.restoreLogs();
    }

    @Test
    public void componentLoadUnloadTest() throws InitializationException {
        assertEquals(1, resourcePool.count());
        ResourceAPIPoolBulkOperation apao = new ResourceAPIPoolBulkOperation();
        DataStore dataStore = new DataStore();

        apao.setOperationType(PoolOperation.OperationType.UNLOAD.toString());
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(), result.toString());
        assertEquals(0, resourcePool.count());

        apao.setOperationType(PoolOperation.OperationType.LOAD.toString());
        result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(), result.toString());
        assertEquals(1, resourcePool.count());
    }

    @Test
    public void componentReloadTest() throws InitializationException {
        assertEquals(1, resourcePool.count());
        ResourceAPI resource1 = null;
        ResourceAPI resource2 = null;
        try {
            resource1 = resourcePool.getByUri(TEST_RESOURCE_URI);
            ResourceAPIPoolBulkOperation apao = new ResourceAPIPoolBulkOperation();
            apao.setOperationType(PoolOperation.OperationType.RELOAD.toString());
            DataStore dataStore = new DataStore();
            OperationResult result = apao.run(dataStore);
            assertEquals(OperationResult.ok().toString(), result.toString());
            assertEquals(1, resourcePool.count());
            resource2 = resourcePool.getByUri(TEST_RESOURCE_URI);
            assertNotEquals(resource1, resource2);
        } finally {
            if (resource1 != null) {
                resource1.removeClient();
            }
            if (resource2 != null) {
                resource2.removeClient();
            }
        }
    }

    private ResourceAPIPool initWithDefaultModel() {
        try {
            loadDefaultModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ResourceAPIPool resourcePool = ResourceAPIPool.getInstance();
        resourcePool.init(servletContext);
        return resourcePool;
    }
}
