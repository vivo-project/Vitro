package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static org.junit.Assert.assertEquals;

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
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Test;

public class JsonContainerTest {

    private String varName = "varName";

    @Test
    public void testObjectAddStringKeyValue() {
        String objectValue = "string value";
        String expectedValue = "\"string value\"";
        testObjectAddKeyValue(new StringParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddStringValue() {
        String objectValue = "string value";
        String expectedValue = "\"string value\"";
        testObjectAddValues(new StringParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddStringPlainLiteralKeyValue() {
        Literal objectValue = ResourceFactory.createPlainLiteral("string value");
        String expectedValue =
                "{\"type\":\"literal\",\"value\":\"string value\"," +
                "\"datatype\":\"http://www.w3.org/2001/XMLSchema#string\"}";
        testObjectAddKeyValue(new StringPlainLiteralParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddLangStringLiteralKeyValue() {
        Literal objectValue = LangStringLiteralParam.deserialize("\"value\"@en-US");
        String expectedValue =
                "{\"type\":\"literal\",\"value\":\"value\",\"datatype\":" +
                "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString\",\"xml:lang\":\"en-US\"}";
        testObjectAddKeyValue(new LangStringLiteralParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddUriResourceKeyValue() {
        Resource objectValue = new ResourceImpl("test:resource");
        String expectedValue = "{\"type\":\"uri\",\"value\":\"test:resource\"}";
        testObjectAddKeyValue(new URIResourceParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddBooleanKeyValue() {
        Boolean objectValue = true;
        String expectedValue = "true";
        testObjectAddKeyValue(new BooleanParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddJsonContainerKeyValue() {
        JsonContainer objectValue = new JsonContainer("{\"test\":true}");
        String expectedValue = "{\"test\":true}";
        testObjectAddKeyValue(new JsonContainerObjectParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testObjectAddJsonArrayKeyValue() {
        JsonContainer objectValue = new JsonContainer(JsonContainer.Type.ARRAY);
        Data data1 = new Data(new StringParam("var1"));
        TestView.setObject(data1, "string 1");
        objectValue.addValue(data1);
        Data data2 = new Data(new StringParam("var2"));
        TestView.setObject(data2, "string 2");
        objectValue.addValue(data2);
        String expectedValue = "[\"string 1\",\"string 2\"]";
        testObjectAddKeyValue(new JsonContainerArrayParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddStringKeyValue() {
        String objectValue = "string value";
        String expectedValue = "\"string value\"";
        testArrayAddKeyValue(new StringParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddStringValue() {
        String objectValue = "string value";
        String expectedValue = "\"string value\"";
        testArrayAddValues(new StringParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddStringPlainLiteralKeyValue() {
        Literal objectValue = ResourceFactory.createPlainLiteral("string value");
        String expectedValue =
                "{\"type\":\"literal\",\"value\":\"string value\"," +
                "\"datatype\":\"http://www.w3.org/2001/XMLSchema#string\"}";
        testArrayAddKeyValue(new StringPlainLiteralParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddLangStringLiteralKeyValue() {
        Literal objectValue = LangStringLiteralParam.deserialize("\"value\"@en-US");
        String expectedValue =
                "{\"type\":\"literal\",\"value\":\"value\",\"datatype\":" +
                "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString\",\"xml:lang\":\"en-US\"}";
        testArrayAddKeyValue(new LangStringLiteralParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddUriResourceKeyValue() {
        Resource objectValue = new ResourceImpl("test:resource");
        String expectedValue = "{\"type\":\"uri\",\"value\":\"test:resource\"}";
        testArrayAddKeyValue(new URIResourceParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddBooleanKeyValue() {
        Boolean objectValue = true;
        String expectedValue = "true";
        testArrayAddKeyValue(new BooleanParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddJsonContainerKeyValue() {
        JsonContainer objectValue = new JsonContainer("{\"test\":true}");
        String expectedValue = "{\"test\":true}";
        testArrayAddKeyValue(new JsonContainerObjectParam(varName), objectValue, expectedValue);
    }

    @Test
    public void testArrayAddJsonArrayKeyValue() {
        JsonContainer objectValue = new JsonContainer(JsonContainer.Type.ARRAY);
        Data data1 = new Data(new StringParam("var1"));
        TestView.setObject(data1, "string 1");
        objectValue.addValue(data1);
        Data data2 = new Data(new StringParam("var2"));
        TestView.setObject(data2, "string 2");
        objectValue.addValue(data2);
        String expectedValue = "[\"string 1\",\"string 2\"]";
        testArrayAddKeyValue(new JsonContainerArrayParam(varName), objectValue, expectedValue);
    }

    private void testObjectAddValues(Parameter param, Object value, String expectedValue) {
        JsonContainer container = new JsonContainer(JsonContainer.Type.OBJECT);
        int n = 3;
        for (int i = 0; i < n; i++) {
            Data data = new Data(param);
            TestView.setObject(data, value);
            container.addValue(data);
        }
        String expectedJson = expectedJsonObjectValue(expectedValue, n);
        // System.out.println(expectedJson);
        String actualJson = JsonContainer.serialize(container);
        assertEquals(expectedJson, actualJson);
    }

    private void testObjectAddKeyValue(Parameter param, Object value, String expectedValue) {
        JsonContainer container = new JsonContainer(JsonContainer.Type.OBJECT);
        Data data = new Data(param);
        TestView.setObject(data, value);
        container.addKeyValue(varName, data);
        String expectedJson = expectedJsonObjectKeyValue(varName, expectedValue);
        // System.out.println(expectedJson);
        String actualJson = JsonContainer.serialize(container);
        assertEquals(expectedJson, actualJson);
    }

    private void testArrayAddKeyValue(Parameter param, Object value, String expectedValue) {
        JsonContainer container = new JsonContainer("[]");
        Data data = new Data(param);
        TestView.setObject(data, value);
        container.addKeyValue(varName, data);
        String expectedJson = expectedJsonArrayKeyValue(varName, expectedValue);
        // System.out.println(expectedJson);
        String actualJson = JsonContainer.serialize(container);
        assertEquals(expectedJson, actualJson);
    }

    private void testArrayAddValues(Parameter param, Object value, String expectedValue) {
        JsonContainer container = new JsonContainer("[]");
        int n = 3;
        for (int i = 0; i < n; i++) {
            Data data = new Data(param);
            TestView.setObject(data, value);
            container.addValue(data);
        }
        String expectedJson = expectedJsonArrayValue(expectedValue, n);
        // System.out.println(expectedJson);
        String actualJson = JsonContainer.serialize(container);
        assertEquals(expectedJson, actualJson);
    }

    private String expectedJsonObjectValue(String value, int n) {
        String values = value;
        for (int i = 1; i < n; i++) {
            values += "," + value;
        }
        return "{" + "\"values\"" + ":" + "[" + values + "]" + "}";
    }

    private String expectedJsonArrayValue(String value, int n) {
        String values = value;
        for (int i = 1; i < n; i++) {
            values += "," + value;
        }
        return "[" + values + "]";
    }

    private String expectedJsonArrayKeyValue(String name, String value) {
        return "[{\"" + name + "\":" + value + "}]";
    }

    private String expectedJsonObjectKeyValue(String name, String value) {
        return "{\"" + name + "\":" + value + "}";
    }
}
