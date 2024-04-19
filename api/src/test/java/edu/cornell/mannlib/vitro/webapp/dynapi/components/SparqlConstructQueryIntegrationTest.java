/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dynapi.AbstractTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
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
public class SparqlConstructQueryIntegrationTest extends AbstractTest {
    private static final String OUTPUT = "output";
    private static final String QUERY = "query";
    private static final String URI = "uri";

    private static final String RESOURCES_PATH =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
    private static final String TEST_PROCEDURE = RESOURCES_PATH + "sparql-construct-test-action.n3";
    private static final String TEST_STORE = RESOURCES_PATH + "sparql-construct-test-store.n3";

    Model storeModel;

    @org.junit.runners.Parameterized.Parameter(0)
    public String uris;

    @org.junit.runners.Parameterized.Parameter(1)
    public String size;

    @Before
    public void beforeEach() {
        LoggingControl.offLogs();
        storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
    }

    @After
    public void reset() {
        LoggingControl.restoreLogs();
    }

    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException {
        loadOntology(ontModel);
        loadModel(ontModel, TEST_PROCEDURE);
        loadModel(storeModel, TEST_STORE);
        Procedure procedure = loader.loadInstance("test:procedure", Procedure.class);
        assertTrue(procedure.isValid());
        Parameters inputParameters = procedure.getInputParams();
        DataStore store = new DataStore();
        OntModelImpl outputModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);

        final Parameters outputParams = procedure.getOutputParams();
        Parameter paramOutput = outputParams.get(OUTPUT);
        assertNotNull(paramOutput);
        Data outputData = new Data(paramOutput);
        TestView.setObject(outputData, outputModel);
        store.addData(OUTPUT, outputData);

        Parameter paramQuery = inputParameters.get(QUERY);
        assertNotNull(paramQuery);
        Data queryData = new Data(paramQuery);
        TestView.setObject(queryData, storeModel);
        store.addData(QUERY, queryData);

        final Parameter paramUri = inputParameters.get(URI);
        assertNotNull(paramUri);
        Data uriData = new Data(paramUri);
        store.addData(URI, uriData);

        for (String uri : uris.split(",")) {
            TestView.setObject(uriData, uri);
            OperationResult opResult = procedure.run(store);
            assertFalse(opResult.hasError());
        }
        assertEquals(Integer.parseInt(size), outputModel.size());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "test:uri0", "0" },
                { "test:uri1", "1" },
                { "test:uri1,test:uri1", "1" },
                { "test:uri1,test:uri2", "2" },

        });
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
