/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import com.fasterxml.jackson.databind.JsonNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ModelWriterTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.N3TemplateTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ByteArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFasterxmlNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
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

    public static ConversionConfiguration createConfig(String className, String methodArgs, String method,
            boolean isStatic) throws ClassNotFoundException {
        ConversionConfiguration desConfig = new ConversionConfiguration();
        desConfig.setClassName(className);
        desConfig.setMethodArguments(methodArgs);
        desConfig.setMethodName(method);
        desConfig.setStaticMethod(isStatic);
        return desConfig;
    }

    public static Parameter createModelParameter(String name) throws Exception {
        Parameter uriParam = new Parameter();
        ParameterType paramType = new ParameterType();
        ImplementationType implType = new ImplementationType();
        paramType.addInterface(Model.class.getCanonicalName());
        paramType.setImplementationType(implType);

        ConversionConfiguration serConfig = createConfig(ModelWriterTest.MODEL_CONVERSION_CLASS, "input", "serializeN3",
                true);
        serConfig.setInputInterface(Model.class.getCanonicalName());

        implType.setSerializationConfig(serConfig);
        ConversionConfiguration desConfig = createConfig(ModelWriterTest.MODEL_CONVERSION_CLASS, "input",
                "deserializeN3", true);
        implType.setDeserializationConfig(desConfig);
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
        ImplementationType uri1ImplType = new ImplementationType();

        ConversionConfiguration serialization = new ConversionConfiguration();

        serialization.setClassName(String.class.getCanonicalName());
        serialization.setMethodArguments("");
        serialization.setMethodName("toString");
        serialization.setStaticMethod(false);
        uri1ImplType.setDeserializationConfig(serialization);

        ConversionConfiguration deserialization = new ConversionConfiguration();
        deserialization.setClassName(String.class.getCanonicalName());
        deserialization.setMethodArguments("");
        deserialization.setMethodName("toString");
        deserialization.setStaticMethod(false);
        uri1ImplType.setSerializationConfig(deserialization);
        uriParamType.addInterface(String.class.getCanonicalName());

        RDFType rdfType = new RDFType();
        rdfType.setName("anyURI");
        uriParamType.setRdfType(rdfType);

        uriParamType.setImplementationType(uri1ImplType);
        uri1Param.setType(uriParamType);
        uriParamType.setSerializationType(N3TemplateTest.anyURI);
        uri1Param.setName(name);
        return uri1Param;
    }

    public static Parameter createStringLiteralParameter(String name) throws Exception {
        Parameter uri1Param = new Parameter();
        ParameterType uri1ParamType = new ParameterType();
        ImplementationType impltype = new ImplementationType();

        ConversionConfiguration config = new ConversionConfiguration();

        config.setClassName(String.class.getCanonicalName());
        config.setMethodArguments("");
        config.setMethodName("toString");
        config.setStaticMethod(false);
        impltype.setDeserializationConfig(config);
        impltype.setSerializationConfig(config);
        uri1ParamType.addInterface(String.class.getCanonicalName());

        RDFType rdfType = new RDFType();
        rdfType.setName("string");
        uri1ParamType.setRdfType(rdfType);

        uri1ParamType.setImplementationType(impltype);
        uri1Param.setType(uri1ParamType);

        uri1ParamType.setSerializationType(N3TemplateTest.stringType);
        uri1Param.setName(name);
        return uri1Param;
    }

    public static Parameter createBooleanParameter(String name) throws Exception {
        Parameter uriParam = new Parameter();
        ParameterType uriParamType = new ParameterType();
        ImplementationType uriImplType = new ImplementationType();

        ConversionConfiguration config = new ConversionConfiguration();

        config.setClassName(String.class.getCanonicalName());
        config.setMethodArguments("");
        config.setMethodName("toString");
        config.setStaticMethod(false);
        uriImplType.setDeserializationConfig(config);
        uriImplType.setSerializationConfig(config);
        uriParamType.addInterface(String.class.getCanonicalName());

        RDFType rdfType = new RDFType();
        rdfType.setName("boolean");
        uriParamType.setRdfType(rdfType);

        uriParamType.setImplementationType(uriImplType);
        uriParam.setType(uriParamType);
        uriParamType.setSerializationType(N3TemplateTest.anyURI);
        uriParam.setName(name);
        return uriParam;
    }

    public static Parameter createJsonParameter(String allVar) throws Exception {
        Parameter jsonParam = new Parameter();
        ParameterType jsonParamType = new ParameterType();
        ImplementationType jsonImplType = new ImplementationType();

        jsonImplType.setDeserializationConfig(getJsonDeserializationConfig());
        jsonImplType.setSerializationConfig(getJsonSerializationConfig());
        jsonParamType.addInterface(JsonNode.class.getCanonicalName());
        jsonImplType.setDefaultValue("{ }");
        jsonParamType.setImplementationType(jsonImplType);
        jsonParam.setType(jsonParamType);
        jsonParamType.setSerializationType(N3TemplateTest.stringType);
        jsonParam.setName(allVar);
        return jsonParam;
    }

    private static ConversionConfiguration getJsonDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClassName(JsonFasterxmlNode.class.getCanonicalName());
        config.setMethodArguments("input");
        config.setMethodName("deserialize");
        config.setStaticMethod(true);
        return config;
    }

    private static ConversionConfiguration getJsonSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClassName(JsonFasterxmlNode.class.getCanonicalName());
        config.setInputInterface(JsonNode.class.getCanonicalName());
        config.setMethodArguments("input");
        config.setMethodName("serialize");
        config.setStaticMethod(true);
        return config;
    }

    public static Parameter createByteArrayParameter(String name) throws Exception {
        Parameter param = new Parameter();
        ParameterType paramType = new ParameterType();
        ImplementationType implType = new ImplementationType();

        implType.setDeserializationConfig(getByteArrayDeserializationConfig());
        implType.setSerializationConfig(getByteArraySerializationConfig());
        paramType.addInterface(ByteArray.class.getCanonicalName());
        implType.setDefaultValue("");
        paramType.setImplementationType(implType);
        param.setType(paramType);
        paramType.setSerializationType(N3TemplateTest.stringType);
        param.setName(name);
        return param;
    }

    private static ConversionConfiguration getByteArrayDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClassName(ByteArray.class.getCanonicalName());
        config.setMethodArguments("input");
        config.setMethodName("deserialize");
        config.setStaticMethod(true);
        return config;
    }

    private static ConversionConfiguration getByteArraySerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClassName(ByteArray.class.getCanonicalName());
        config.setMethodArguments("");
        config.setMethodName("serialize");
        config.setStaticMethod(false);
        return config;
    }

}
