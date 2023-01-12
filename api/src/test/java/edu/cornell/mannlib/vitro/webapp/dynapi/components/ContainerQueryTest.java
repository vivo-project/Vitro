package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer.Type;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;

@RunWith(Parameterized.class)
public class ContainerQueryTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String keyValue;

    @org.junit.runners.Parameterized.Parameter(1)
    public Type containerType;
    
    @org.junit.runners.Parameterized.Parameter(2)
    public String value;

    private String outputName;
    
    @Test
    public void test() throws InitializationException {
        ContainerQuery cq = new ContainerQuery();
        String containerName = "container";
        Parameter container = new JsonContainerObjectParam(containerName);
        cq.setContainer(container);
        outputName = "output";
        String keyName = "key";
        Parameter outputParam = new StringParam(outputName);
        cq.addOutputParameter(outputParam);
        Parameter keyParam = new StringParam(keyName);
        cq.addInputParameter(keyParam);

        DataStore store = new DataStore();
        Data containerData = new Data(container);
        JsonContainer containerObject = new JsonContainer(containerType);
        Parameter outputParam1 = new StringParam(outputName);
        Data data = new Data(outputParam1);
        TestView.setObject(data, value);
        if (containerType.equals(Type.ARRAY)) {
            containerObject.addValue(data);            
        } else {
            containerObject.addKeyValue(keyValue, data);
        }

        TestView.setObject(containerData, containerObject);
        store.addData(container.getName(), containerData);

        Data keyData = new Data(keyParam);
        TestView.setObject(keyData, keyValue);
        store.addData(keyParam.getName(), keyData);
        
        assertFalse(store.contains(outputName));
        
        assertEquals(OperationResult.ok(), cq.run(store));
        
        assertTrue(store.contains(outputName));
        Data output = store.getData(outputName);
        assertEquals(value, output.getSerializedValue());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            {"0", Type.ARRAY, "test value"},
            {"0", Type.OBJECT, "0"},
            {"key", Type.OBJECT, "test value"},
            {"key", Type.OBJECT, ""},
        });
    }
}
