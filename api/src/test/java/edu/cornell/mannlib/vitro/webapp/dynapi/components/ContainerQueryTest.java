/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.ContainerQuery;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JacksonJsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonObject;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ContainerQueryTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String keyValue;

    @org.junit.runners.Parameterized.Parameter(1)
    public Class<? extends JacksonJsonContainer> containerClass;

    @org.junit.runners.Parameterized.Parameter(2)
    public String value;

    private String outputName;

    @Test
    public void test() throws InitializationException {
        ContainerQuery cq = new ContainerQuery();
        String containerName = "container";
        Parameter containerParam = new JsonContainerObjectParam(containerName);
        cq.setContainer(containerParam);
        outputName = "output";
        String keyName = "key";
        Parameter outputParam = new StringParam(outputName);
        cq.addOutputParameter(outputParam);
        Parameter keyParam = new StringParam(keyName);
        cq.addInputParameter(keyParam);

        DataStore store = new DataStore();
        Data containerData = new Data(containerParam);
        JsonContainer container = JsonFactory.getJson(containerClass);
        Parameter outputParam1 = new StringParam(outputName);
        Data data = new Data(outputParam1);
        TestView.setObject(data, value);
        if (containerClass.equals(JsonArray.class)) {
            container.addValue(data);
        } else {
            container.addKeyValue(keyValue, data);
        }

        TestView.setObject(containerData, container);
        store.addData(containerParam.getName(), containerData);

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
                { "0", JsonArray.class, "test value" },
                { "0", JsonObject.class, "0" },
                { "key", JsonObject.class, "test value" },
                { "key", JsonObject.class, "" },
                { "key$", JsonObject.class, "test value" },
                { "key\\ value", JsonObject.class, "test value" },
                { "key'", JsonObject.class, "test value" },
                { "key.", JsonObject.class, "test value" },
                { "key\n", JsonObject.class, "test value" },
                { "key\t", JsonObject.class, "test value" },

        });
    }
}
