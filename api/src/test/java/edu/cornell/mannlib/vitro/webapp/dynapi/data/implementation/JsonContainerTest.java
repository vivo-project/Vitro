package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static org.junit.Assert.assertEquals;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerArrayParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.LangStringLiteralParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringPlainLiteralParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.URIResourceParam;

public class JsonContainerTest {
    
    private String varName = "varName";

    @Test
    public void testObjectAddStringValue() {
        String objectValue = "string value";
        String expectedValue = "\"string value\"";
        testObjectAddKeyValue(new StringParam(varName), objectValue, expectedValue);
    }
    
    @Test
    public void testObjectAddStringPlainLiteralValue() {
        Literal objectValue = ResourceFactory.createPlainLiteral("string value");
        String expectedValue = "{\"type\":\"literal\",\"value\":\"string value\",\"datatype\":\"http://www.w3.org/2001/XMLSchema#string\"}";
        testObjectAddKeyValue(new StringPlainLiteralParam(varName), objectValue, expectedValue);
    }
    
    @Test
    public void testObjectAddLangStringLiteralValue() {
        Literal objectValue = LangStringLiteralParam.deserialize("\"value\"@en-US");
        String expectedValue = "{\"type\":\"literal\",\"value\":\"value\",\"datatype\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString\",\"xml:lang\":\"en-US\"}";
        testObjectAddKeyValue(new LangStringLiteralParam(varName), objectValue, expectedValue);
    }
    
    @Test
    public void testObjectAddUriResourceValue() {
        Resource objectValue = new ResourceImpl("test:resource");
        String expectedValue = "{\"type\":\"uri\",\"value\":\"test:resource\"}";
        testObjectAddKeyValue(new URIResourceParam(varName), objectValue, expectedValue);
    }
    
    @Test
    public void testObjectAddBooleanValue() {
        Boolean objectValue = true;
        String expectedValue = "true";
        testObjectAddKeyValue(new BooleanParam(varName), objectValue, expectedValue);
    }
    
    @Test
    public void testObjectAddJsonContainerValue() {
        JsonContainer objectValue = new JsonContainer("{\"test\":true}");
        String expectedValue = "{\"test\":true}";
        testObjectAddKeyValue(new JsonContainerObjectParam(varName), objectValue, expectedValue);
    }
    
    @Test
    public void testObjectAddJsonArrayValue() {
        JsonContainer objectValue = new JsonContainer(JsonContainer.Type.EmptyArray);
        Data data1 = new Data(new StringParam("var1"));
        TestView.setObject(data1, "string 1");
        objectValue.addValue(data1);
        Data data2 = new Data(new StringParam("var2"));
        TestView.setObject(data2, "string 2");
        objectValue.addValue(data2);
        String expectedValue = "[\"string 1\",\"string 2\"]";
        testObjectAddKeyValue(new JsonContainerArrayParam(varName), objectValue, expectedValue);
    }

    private void testObjectAddKeyValue(Parameter param, Object value, String expectedValue) {
        JsonContainer container = new JsonContainer(JsonContainer.Type.EmptyObject);
        Data data = new Data(param);
        TestView.setObject(data, value);
        container.addKeyValue(varName, data);
        String expectedJson = expectedJson(varName, expectedValue);
        String actualJson = JsonContainer.serialize(container);
        assertEquals(expectedJson, actualJson);
    }
    private String expectedJson(String name, String value) {
        return "{\"" + name + "\":" + value + "}";
    }
}
