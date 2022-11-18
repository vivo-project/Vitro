package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ResourceAPIPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;

public class ResourceAPIPoolAtomicOperationTest extends ServletContextTest{

    private static final String STRING_PARAM_NAME = "stringParam";
    private final static String TEST_ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api/resourceAPI/testResource1";
    private ResourceAPIPool resourcePool;
    
    @Before
    public void preparePool() {
       resourcePool = initWithDefaultModel();
    }
    
    @After
    public void reset() {
        setup();
        resourcePool = initWithDefaultModel();
        assertEquals(0, resourcePool.count());
    }
    
    @Test
    public void componentLoadUnloadTest() throws InitializationException {
        ResourceAPIPoolAtomicOperation apao = new ResourceAPIPoolAtomicOperation();
        apao.setOperationType(PoolOperation.Types.LOAD.toString());
        DataStore dataStore = new DataStore();
        addStringParam(dataStore, apao);
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, resourcePool.count());
        
        apao.setOperationType(PoolOperation.Types.UNLOAD.toString());
        result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(0, resourcePool.count());
    }
    
    @Test
    public void componentReloadTest() throws InitializationException {
        assertEquals(0, resourcePool.count());
        ResourceAPIPoolAtomicOperation apao = new ResourceAPIPoolAtomicOperation();
        apao.setOperationType(PoolOperation.Types.RELOAD.toString());
        DataStore dataStore = new DataStore();
        addStringParam(dataStore, apao);
        OperationResult result = apao.run(dataStore);
        assertEquals(OperationResult.ok().toString(),result.toString());
        assertEquals(1, resourcePool.count());
    }

    private void addStringParam(DataStore dataStore, ResourceAPIPoolAtomicOperation apao) {
        Parameter plainStringParam = new StringParam(STRING_PARAM_NAME);
        apao.addInputParameter(plainStringParam);
        Data plainStringData = new Data(plainStringParam);
        TestView.setObject(plainStringData, TEST_ACTION_URI);
        dataStore.addData(STRING_PARAM_NAME, plainStringData);
    }
    
    private ResourceAPIPool initWithDefaultModel() {
        try {
            loadDefaultModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ResourceAPIPool resourcePool = ResourceAPIPool.getInstance();
        resourcePool.init(servletContext);
        resourcePool.unload();
        return resourcePool;
    }
}
