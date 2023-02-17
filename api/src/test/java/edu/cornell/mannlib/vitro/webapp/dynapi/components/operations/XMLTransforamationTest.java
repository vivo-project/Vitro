package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;

@RunWith(Parameterized.class)
public class XMLTransforamationTest {

    private static final String OUTPUT_XML = "output xml";
    private static final String INPUT_XML = "input xml";
    private static final String XSLT = "xslt";
    private XMLTransformation xmlt;
    private Parameter inputParam;
    private Parameter xlstParam;
    private Parameter outputParam;
    private Data inputData;
    private Data xsltData;

    @org.junit.runners.Parameterized.Parameter(0)
    public String inputValue;

    @org.junit.runners.Parameterized.Parameter(1)
    public String xsltValue;
    
    @org.junit.runners.Parameterized.Parameter(2)
    public String expectedOutput;
    
    private DataStore ds;
    
    @Before
    public void init() {
        LoggingControl.offLog(XMLTransformation.class);
        xmlt = new XMLTransformation();
        ds = new DataStore();
        inputParam = new StringParam(INPUT_XML); 
        outputParam = new StringParam(OUTPUT_XML);
        xlstParam = new StringParam(XSLT); 
        inputData = new Data(inputParam);
        inputData.setRawString(inputValue);
        inputData.initialization();
        xsltData = new Data(xlstParam);
        xsltData.setRawString(xsltValue);
        xsltData.initialization();

    }
    @After
    public void reset() {
        LoggingControl.restoreLog(XMLTransformation.class);
    }
    
    @Test
    public void testUnconfiguredOperation() throws Exception {
        xmlt.setInputXmlParam(inputParam);
        assertFalse(xmlt.isValid());
        xmlt.setXsltParam(xlstParam);
        assertFalse(xmlt.isValid());
        xmlt.setOutputXml(outputParam);
        assertTrue(xmlt.isValid());
        ds.addData(INPUT_XML, inputData);
        ds.addData(XSLT, xsltData);
        assertEquals(OperationResult.ok(), xmlt.run(ds));
        assertTrue(ds.contains(OUTPUT_XML));
        Data outputData = ds.getData(OUTPUT_XML);
        String outputString = outputData.getSerializedValue();
        assertEquals(expectedOutput,outputString);
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { "<?xml version=\"1.0\" encoding=\"utf-8\"?><test><name>dynapi</name></test>", 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<xml xsl:version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + " <xsl:value-of select=\"concat(test/name/text(),' works')\"/>"
            + "</xml>",
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml>dynapi works</xml>"
            },
        });
    }
}
