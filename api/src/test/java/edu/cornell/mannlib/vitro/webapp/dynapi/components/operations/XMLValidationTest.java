/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
import org.apache.tika.utils.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class XMLValidationTest {

    private static final String ERROR_PARAM_NAME = "error";
    private static final String OUTPUT_PARAM_NAME = "result";
    private static final String INPUT_XML_PARAM_NAME = "input";
    private static final String XSD_PARAM_NAME = "xsd";
    private XMLValidation xmlt;
    private Parameter inputParam;
    private Parameter xsdParam;
    private Parameter outputParam;
    private Parameter errorMessageParam;
    private Data inputData;
    private Data xsdData;

    @org.junit.runners.Parameterized.Parameter(0)
    public String inputValue;

    @org.junit.runners.Parameterized.Parameter(1)
    public String xsd;

    @org.junit.runners.Parameterized.Parameter(2)
    public String expectedResult;

    private DataStore ds;

    @Before
    public void init() {
        LoggingControl.offLog(XMLTransformation.class);
        xmlt = new XMLValidation();
        ds = new DataStore();
        inputParam = new StringParam(INPUT_XML_PARAM_NAME);
        outputParam = new BooleanParam(OUTPUT_PARAM_NAME);
        errorMessageParam = new StringParam(ERROR_PARAM_NAME);
        xsdParam = new StringParam(XSD_PARAM_NAME);
        inputData = new Data(inputParam);
        inputData.setRawString(inputValue);
        inputData.initialization();
        xsdData = new Data(xsdParam);
        xsdData.setRawString(xsd);
        xsdData.initialization();
    }

    @After
    public void reset() {
        LoggingControl.restoreLog(XMLTransformation.class);
    }

    @Test
    public void testUnconfiguredOperation() throws Exception {
        xmlt.setInputXmlParam(inputParam);
        assertFalse(xmlt.isValid());
        xmlt.setSchemaParam(xsdParam);
        assertFalse(xmlt.isValid());
        xmlt.setValidationResult(outputParam);
        xmlt.setErrorMessage(errorMessageParam);
        assertTrue(xmlt.isValid());
        ds.addData(INPUT_XML_PARAM_NAME, inputData);
        ds.addData(XSD_PARAM_NAME, xsdData);
        assertEquals(OperationResult.ok(), xmlt.run(ds));
        assertTrue(ds.contains(OUTPUT_PARAM_NAME));
        Data outputData = ds.getData(OUTPUT_PARAM_NAME);
        String outputString = outputData.getSerializedValue();
        boolean expectSuccess = false;
        if (StringUtils.isBlank(expectedResult)) {
            expectSuccess = true;
        }
        assertEquals(Boolean.toString(expectSuccess), outputString);
        Data errorData = ds.getData(ERROR_PARAM_NAME);
        String errorString = errorData.getSerializedValue();
        assertEquals(expectedResult, errorString);
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
                "cvc-maxLength-valid: Value 'Alice' with length = '5' is not facet-valid with respect " +
                        "to maxLength '3' for type '#AnonType_nameindividual'."
                } });
    }
}
