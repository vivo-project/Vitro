/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.IntegerParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class LoopOperationIntegrationTest extends ServletContextTest {

    private static final String OUTPUT_CONTAINER = "output_container";
    private static final String RESOURCES_PATH =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
    private static final String TEST_ACTION = RESOURCES_PATH + "loop-operation-test-action.n3";

    Model storeModel;

    @org.junit.runners.Parameterized.Parameter(0)
    public List<Integer> inputValues;

    @org.junit.runners.Parameterized.Parameter(1)
    public String expectedValues;

    @Before
    public void beforeEach() {
        LoggingControl.offLogs();
        storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
    }

    @After
    public void reset() {
        setup();
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        procedurePool.reload();
        assertEquals(0, procedurePool.count());
        LoggingControl.restoreLogs();
    }

    private ProcedurePool initWithDefaultModel() throws IOException {
        loadOntology(ontModel);
        ProcedurePool rpcPool = ProcedurePool.getInstance();
        rpcPool.init();
        return rpcPool;
    }

    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException,
            InitializationException {
        loadModel(ontModel, TEST_ACTION);
        ProcedurePool procedurePool = initWithDefaultModel();
        Procedure action = null;
        DataStore store = null;
        try {
            action = procedurePool.getByUri("test:loop_action");
            assertFalse(action instanceof NullProcedure);
            assertTrue(action.isValid());
            store = new DataStore();
            UserAccount user = new UserAccount();
            user.setRootUser(true);
            store.setUser(user);
            addInputContainer(store);

            Endpoint.collectDependencies(action, store, procedurePool);
            assertTrue(OperationResult.ok().equals(action.run(store)));
            assertTrue(store.contains(OUTPUT_CONTAINER));
            Data output = store.getData(OUTPUT_CONTAINER);
            assertEquals(expectedValues, output.getSerializedValue());
        } finally {
            if (action != null) {
                action.removeClient();
            }
            if (store != null) {
                store.removeDependencies();
            }
        }
    }

    private void addInputContainer(DataStore store) {
        String containerParamName = "input_container";
        Parameter container = new JsonContainerObjectParam(containerParamName);
        Data containerData = createContainer(container);
        store.addData(containerParamName, containerData);
    }

    private Data createContainer(Parameter containerParam) {
        Data containerData = new Data(containerParam);
        JsonArray container = new JsonArray();
        String expectedOutputParamName = "item";
        Parameter expectedOutputParam = new IntegerParam(expectedOutputParamName);
        for (Integer value : inputValues) {
            addValue(container, expectedOutputParam, value);
        }
        TestView.setObject(containerData, container);
        return containerData;
    }

    private void addValue(JsonArray container, Parameter expectedOutputParam, Integer value) {
        Data data = new Data(expectedOutputParam);
        TestView.setObject(data, value);
        container.addValue(data);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { Arrays.asList(20, 30, 40, 50, 60), "[\"30\",\"40\",\"50\",\"60\",\"70\"]" },
                { Arrays.asList(-100, -90, -80, -70, -60), "[\"-90\",\"-80\",\"-70\",\"-60\",\"-50\"]" }, });
    }

    protected void loadModel(Model model, String... files) throws IOException {
        for (String file : files) {
            String rdf = readFile(file);
            model.read(new StringReader(rdf), null, "n3");
        }
    }

    public void loadOntology(OntModel ontModel) throws IOException {
        loadModel(ontModel, IMPLEMENTATION_FILE_PATH);
        loadModel(ontModel, ONTOLOGY_FILE_PATH);
        loadModel(ontModel, getFileList(ABOX_PREFIX));
    }
}
