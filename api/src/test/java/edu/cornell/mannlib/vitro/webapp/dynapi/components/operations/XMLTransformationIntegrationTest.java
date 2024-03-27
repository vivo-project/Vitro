/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
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
public class XMLTransformationIntegrationTest extends ServletContextTest {

    private static final String TEST_PROCEDURE_URI = "test:xml-transformation-procedure";
    private static final String OUTPUT = "output";
    private static final String RESOURCES_PATH =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/operations/";
    private static final String TEST_PROCEDURE_FILE_PATH = RESOURCES_PATH + "xml-transformation-test-procedure.n3";

    Model storeModel;

    @org.junit.runners.Parameterized.Parameter(0)
    public String inputXml;

    @org.junit.runners.Parameterized.Parameter(1)
    public String xslt;

    @org.junit.runners.Parameterized.Parameter(2)
    public String expectedOutputXml;

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
        loadModel(ontModel, TEST_PROCEDURE_FILE_PATH);
        ProcedurePool procedurePool = initWithDefaultModel();
        Procedure procedure = null;
        DataStore store = null;
        try {
            procedure = procedurePool.getByUri(TEST_PROCEDURE_URI);
            assertFalse(procedure instanceof NullProcedure);
            assertTrue(procedure.isValid());
            store = new DataStore();
            UserAccount user = new UserAccount();
            user.setRootUser(true);
            store.setUser(user);
            StringParam inputParam = new StringParam("input");
            Data inputData = new Data(inputParam);
            inputData.setRawString(inputXml);
            inputData.initialization();
            store.addData("input", inputData);
            StringParam xsltParam = new StringParam("xslt");
            Data xsltData = new Data(xsltParam);
            xsltData.setRawString(xslt);
            xsltData.initialization();
            store.addData("xslt", xsltData);
            assertTrue(OperationResult.ok().equals(procedure.run(store)));
            assertTrue(store.contains(OUTPUT));
            Data output = store.getData(OUTPUT);
            assertEquals(expectedOutputXml, output.getSerializedValue());
        } finally {
            if (procedure != null) {
                procedure.removeClient();
            }
            if (store != null) {
                store.removeDependencies();
            }
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                {
                "<?xml version=\"1.0\" encoding=\"utf-8\"?><test><name>dynapi</name></test>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<xml xsl:version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
                    " <xsl:value-of select=\"concat(test/name/text(),' works')\"/>" +
                    "</xml>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml>dynapi works</xml>" }, });
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
