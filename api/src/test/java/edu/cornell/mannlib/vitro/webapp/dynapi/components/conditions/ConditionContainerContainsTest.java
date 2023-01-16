package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.ResourceAPIPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer.Type;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;

@RunWith(Parameterized.class)
public class ConditionContainerContainsTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public List<String> keyValues;

    @org.junit.runners.Parameterized.Parameter(1)
    public Type containerType;
    
    @org.junit.runners.Parameterized.Parameter(2)
    public boolean result;

    @org.junit.runners.Parameterized.Parameter(3)
    public String testedKey;
    
    @Before
    public void init() {
        Logger.getLogger(JsonContainer.class).setLevel(Level.OFF);
    }
    
    @After
    public void after() {
        Logger.getLogger(JsonContainer.class).setLevel(Level.ERROR);
    }
    
    @Test
    public void test() throws InitializationException {
        ConditionContainerContains ccc = new ConditionContainerContains();
        String containerName = "container";
        Parameter container = new JsonContainerObjectParam(containerName);
        ccc.setContainer(container);
        String keyName = "key";
        Parameter keyParam = new StringParam(keyName);
        ccc.addInputParameter(keyParam);

        DataStore store = new DataStore();
        Data containerData = createContainer(container);
        store.addData(container.getName(), containerData);
        
        Data keyData = new Data(keyParam);
        TestView.setObject(keyData, testedKey);
        store.addData(keyParam.getName(), keyData);
        assertEquals(result, ccc.isSatisfied(store));
    }

    private Data createContainer(Parameter container) {
        Data containerData = new Data(container);
        JsonContainer containerObject = new JsonContainer(containerType);
        String expectedOutputParamName = "output";
        Parameter expectedOutputParam = new StringParam(expectedOutputParamName);
        Data data = new Data(expectedOutputParam);
        if (containerType.equals(Type.ARRAY)) {
            for (int i = 0; i <= Integer.parseInt(keyValues.iterator().next()); i++) {
                containerObject.addValue(data);            
            }
        } else {
            for (String key : keyValues) {
                containerObject.addKeyValue(key, data);
            }
        }
        TestView.setObject(containerData, containerObject);
        return containerData;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { Arrays.asList("0"), Type.ARRAY, true, "0" },
            { Arrays.asList("0"), Type.ARRAY, false, "1" },
            { Arrays.asList("2"), Type.ARRAY, true, "2" },
            { Arrays.asList("2"), Type.ARRAY, false, "u10" },

            { Arrays.asList("0"), Type.OBJECT, true, "0"  },
            { Arrays.asList("key"), Type.OBJECT, true, "key"  },
            { Arrays.asList("key with space"), Type.OBJECT, true, "key with space"  },
            { Arrays.asList("key$"), Type.OBJECT, true, "key$" },
            { Arrays.asList("key\""), Type.OBJECT, true, "key\"" },
            { Arrays.asList("key."), Type.OBJECT, true, "key." },
            { Arrays.asList("key\n"), Type.OBJECT, true, "key\n" },
            { Arrays.asList("key\t"), Type.OBJECT, true, "key\t" },
            { Arrays.asList("key'"), Type.OBJECT, true, "key'" },
            { Arrays.asList("key\\"), Type.OBJECT, true, "key\\" },
        });
    }
}
