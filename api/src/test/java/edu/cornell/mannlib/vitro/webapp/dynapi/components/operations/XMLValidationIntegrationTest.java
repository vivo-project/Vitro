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
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
import org.apache.commons.lang3.StringUtils;
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
public class XMLValidationIntegrationTest extends ServletContextTest {

    private static final String INPUT_PARAM_NAME = "input";
    private static final String XSD_PARAM_NAME = "xsd";
    private static final String TEST_PROCEDURE_URI = "test:xml-validation-procedure";
    private static final String OUTPUT = "result";
    private static final String ERROR_PARAM_NAME = "error";
    private static final String RESOURCES_PATH =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/operations/";
    private static final String TEST_PROCEDURE_FILE_PATH = RESOURCES_PATH + "xml-validation-test-procedure.n3";

    Model storeModel;

    @org.junit.runners.Parameterized.Parameter(0)
    public String inputXml;

    @org.junit.runners.Parameterized.Parameter(1)
    public String xsd;

    @org.junit.runners.Parameterized.Parameter(2)
    public String expectedResult;

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
    public void test() throws Exception {
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
            StringParam inputParam = new StringParam(INPUT_PARAM_NAME);
            Data inputData = new Data(inputParam);
            inputData.setRawString(inputXml);
            inputData.initialization();
            store.addData(INPUT_PARAM_NAME, inputData);
            StringParam xsltParam = new StringParam(XSD_PARAM_NAME);
            Data xsdData = new Data(xsltParam);
            xsdData.setRawString(xsd);
            xsdData.initialization();
            store.addData(XSD_PARAM_NAME, xsdData);
            assertTrue(OperationResult.ok().equals(procedure.run(store)));
            assertTrue(store.contains(OUTPUT));
            Data output = store.getData(OUTPUT);
            boolean expectSuccess = false;
            if (StringUtils.isBlank(expectedResult)) {
                expectSuccess = true;
            }
            assertEquals(Boolean.toString(expectSuccess), output.getSerializedValue());
            Data errorData = store.getData(ERROR_PARAM_NAME);
            String errorString = errorData.getSerializedValue();
            assertEquals(expectedResult, errorString);
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
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<individual>\n" +
                    "    <name>Bob</name>\n" +
                    "</individual>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                    "    <xs:element name=\"individual\">\n" +
                    "        <xs:complexType>\n" +
                    "            <xs:sequence>\n" +
                    "                <xs:element name=\"name\">\n" +
                    "                    <xs:simpleType>\n" +
                    "                        <xs:restriction base=\"xs:string\">\n" +
                    "                            <xs:maxLength value=\"3\" />\n" +
                    "                        </xs:restriction>\n" +
                    "                    </xs:simpleType>\n" +
                    "                </xs:element>" +
                    "            </xs:sequence>\n" +
                    "        </xs:complexType>\n" +
                    "    </xs:element>\n" +
                    "</xs:schema>",
                "" },
                {
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<individual>\n" +
                    "    <name>Alice</name>\n" +
                    "</individual>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                    "    <xs:element name=\"individual\">\n" +
                    "        <xs:complexType>\n" +
                    "            <xs:sequence>\n" +
                    "                <xs:element name=\"name\">\n" +
                    "                    <xs:simpleType>\n" +
                    "                        <xs:restriction base=\"xs:string\">\n" +
                    "                            <xs:maxLength value=\"3\" />\n" +
                    "                        </xs:restriction>\n" +
                    "                    </xs:simpleType>\n" +
                    "                </xs:element>" +
                    "            </xs:sequence>\n" +
                    "        </xs:complexType>\n" +
                    "    </xs:element>\n" +
                    "</xs:schema>",
                "cvc-maxLength-valid: Value 'Alice' with length = '5' is not facet-valid" +
                    " with respect to maxLength '3' for type '#AnonType_nameindividual'." } });
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
