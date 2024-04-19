/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import com.fasterxml.jackson.databind.JsonNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.N3TemplateTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ByteArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiInMemoryOntModel;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFasterxmlNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

public class ParameterUtils {

    public static ConversionConfiguration createConfig(Class<?> clazz, String methodArgs, String method,
            boolean isStatic) throws ClassNotFoundException {
        ConversionConfiguration desConfig = new ConversionConfiguration();
        desConfig.setClass(clazz);
        desConfig.setMethodArguments(methodArgs);
        desConfig.setMethodName(method);
        desConfig.setStaticMethod(isStatic);
        return desConfig;
    }

    public static Parameter createModelParameter(String name) throws Exception {
        Parameter uriParam = new Parameter();
        ParameterType paramType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();
        paramType.addInterface(Model.class);
        paramType.addFormat(defaultFormat);

        ConversionConfiguration serConfig = createConfig(DynapiInMemoryOntModel.class, "input", "serializeN3",
                true);
        serConfig.setInputInterface(Model.class);

        defaultFormat.setSerializationConfig(serConfig);
        ConversionConfiguration desConfig = createConfig(DynapiInMemoryOntModel.class, "input",
                "deserializeN3", true);
        defaultFormat.setDeserializationConfig(desConfig);
        uriParam.setType(paramType);
        uriParam.setName(name);
        return uriParam;
    }

    public static Statement addStatement(OntModelImpl additionModel, String s, String p, String o) {
        final Resource resource = ResourceFactory.createResource(s);
        final Property property = ResourceFactory.createProperty(p);
        Literal literal;
        if (o.contains("@")) {
            String[] parts = o.split("@");
            literal = ResourceFactory.createLangLiteral(parts[0], parts[1]);
        } else {
            literal = ResourceFactory.createPlainLiteral(o);
        }
        Statement stmt = ResourceFactory.createStatement(resource, property, literal);
        additionModel.add(stmt);
        return stmt;
    }

    public static Parameter createUriParameter(String name) throws Exception {
        Parameter uri1Param = new Parameter();
        ParameterType uriParamType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();

        ConversionConfiguration serialization = new ConversionConfiguration();

        serialization.setClass(String.class);
        serialization.setMethodArguments("");
        serialization.setMethodName("toString");
        serialization.setStaticMethod(false);
        defaultFormat.setDeserializationConfig(serialization);

        ConversionConfiguration deserialization = new ConversionConfiguration();
        deserialization.setClass(String.class);
        deserialization.setMethodArguments("");
        deserialization.setMethodName("toString");
        deserialization.setStaticMethod(false);
        defaultFormat.setSerializationConfig(deserialization);
        uriParamType.addInterface(String.class);

        RDFType rdfType = new RDFType();
        rdfType.setName("anyURI");
        uriParamType.setRdfType(rdfType);

        uriParamType.addFormat(defaultFormat);
        uri1Param.setType(uriParamType);
        uriParamType.setSerializationType(N3TemplateTest.anyURI);
        uri1Param.setName(name);
        return uri1Param;
    }

    public static Parameter createStringLiteralParameter(String name) throws Exception {
        Parameter uri1Param = new Parameter();
        ParameterType uri1ParamType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();

        ConversionConfiguration config = new ConversionConfiguration();

        config.setClass(String.class);
        config.setMethodArguments("");
        config.setMethodName("toString");
        config.setStaticMethod(false);
        defaultFormat.setDeserializationConfig(config);
        defaultFormat.setSerializationConfig(config);
        uri1ParamType.addInterface(String.class);

        RDFType rdfType = new RDFType();
        rdfType.setName("string");
        uri1ParamType.setRdfType(rdfType);

        uri1ParamType.addFormat(defaultFormat);
        uri1Param.setType(uri1ParamType);

        uri1ParamType.setSerializationType(N3TemplateTest.stringType);
        uri1Param.setName(name);
        return uri1Param;
    }

    public static Parameter createBooleanParameter(String name) throws Exception {
        Parameter uriParam = new Parameter();
        ParameterType uriParamType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();

        ConversionConfiguration config = new ConversionConfiguration();

        config.setClass(String.class);
        config.setMethodArguments("");
        config.setMethodName("toString");
        config.setStaticMethod(false);
        defaultFormat.setDeserializationConfig(config);
        defaultFormat.setSerializationConfig(config);
        uriParamType.addInterface(String.class);

        RDFType rdfType = new RDFType();
        rdfType.setName("boolean");
        uriParamType.setRdfType(rdfType);

        uriParamType.addFormat(defaultFormat);
        uriParam.setType(uriParamType);
        uriParamType.setSerializationType(N3TemplateTest.anyURI);
        uriParam.setName(name);
        return uriParam;
    }

    public static Parameter createJsonParameter(String allVar) throws Exception {
        Parameter jsonParam = new Parameter();
        ParameterType jsonParamType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();

        defaultFormat.setDeserializationConfig(getJsonDeserializationConfig());
        defaultFormat.setSerializationConfig(getJsonSerializationConfig());
        jsonParamType.addInterface(JsonNode.class);
        defaultFormat.setDefaultValue("{ }");
        jsonParamType.addFormat(defaultFormat);
        jsonParam.setType(jsonParamType);
        jsonParamType.setSerializationType(N3TemplateTest.stringType);
        jsonParam.setName(allVar);
        return jsonParam;
    }

    private static ConversionConfiguration getJsonDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClass(JsonFasterxmlNode.class);
        config.setMethodArguments("input");
        config.setMethodName("deserialize");
        config.setStaticMethod(true);
        return config;
    }

    private static ConversionConfiguration getJsonSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClass(JsonFasterxmlNode.class);
        config.setInputInterface(JsonNode.class);
        config.setMethodArguments("input");
        config.setMethodName("serialize");
        config.setStaticMethod(true);
        return config;
    }

    public static Parameter createByteArrayParameter(String name) throws Exception {
        Parameter param = new Parameter();
        ParameterType paramType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();

        defaultFormat.setDeserializationConfig(getByteArrayDeserializationConfig());
        defaultFormat.setSerializationConfig(getByteArraySerializationConfig());
        paramType.addInterface(ByteArray.class);
        defaultFormat.setDefaultValue("");
        paramType.addFormat(defaultFormat);
        param.setType(paramType);
        paramType.setSerializationType(N3TemplateTest.stringType);
        param.setName(name);
        return param;
    }

    private static ConversionConfiguration getByteArrayDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClass(ByteArray.class);
        config.setMethodArguments("input");
        config.setMethodName("deserialize");
        config.setStaticMethod(true);
        return config;
    }

    private static ConversionConfiguration getByteArraySerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClass(ByteArray.class);
        config.setMethodArguments("");
        config.setMethodName("serialize");
        config.setStaticMethod(false);
        return config;
    }

}
