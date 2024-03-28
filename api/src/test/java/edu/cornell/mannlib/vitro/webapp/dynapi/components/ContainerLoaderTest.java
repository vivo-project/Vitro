/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.ContainerLoader;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ContainerLoaderTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String key;

    @org.junit.runners.Parameterized.Parameter(1)
    public JsonContainer.Type containerType;

    @Test
    public void testLoadIntoArrayContainer() throws InitializationException {
        String inputName = "input";
        Parameter input = new StringParam(inputName);
        String containerName = "container";
        Parameter container = new JsonContainerObjectParam(containerName);
        ContainerLoader cl = new ContainerLoader();
        cl.addInputParameter(input);
        cl.setContainer(container);
        if (!key.isEmpty()) {
            cl.setKey(key + "1");
        }

        DataStore store = new DataStore();
        Data inputData = new Data(input);
        TestView.setObject(inputData, "value1");
        store.addData(input.getName(), inputData);

        Data containerData = new Data(container);
        JsonContainer containerObject = JsonFactory.getJson(containerType);
        TestView.setObject(containerData, containerObject);
        store.addData(container.getName(), containerData);

        assertEquals(OperationResult.ok(), cl.run(store));

        inputData = new Data(input);
        TestView.setObject(inputData, "value2");
        store.addData(input.getName(), inputData);
        if (!key.isEmpty()) {
            cl.setKey(key + "2");
        }
        assertEquals(OperationResult.ok(), cl.run(store));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "", JsonContainer.Type.ARRAY },
                { "", JsonContainer.Type.OBJECT },
                { "key", JsonContainer.Type.ARRAY },
                { "key", JsonContainer.Type.OBJECT }, });
    }
}
