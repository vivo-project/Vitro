package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.ModelWriterTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.N3TemplateTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ByteArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFasterxmlNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;

public class ParameterUtils {

	public static ImplementationConfig createConfig(String className, String methodArgs, String method,
			boolean isStatic) throws ClassNotFoundException {
		ImplementationConfig desConfig = new ImplementationConfig();
		desConfig.setClassName(className);
		desConfig.setMethodArguments(methodArgs);
		desConfig.setMethodName(method);
		desConfig.setStaticMethod(isStatic);
		return desConfig;
	}

	public static Parameter createModelParameter(String name) throws Exception {
		Parameter uri1Param = new Parameter();
		ParameterType paramType = new ParameterType();
		ImplementationType implType = new ImplementationType();
		implType.setClassName(Model.class.getCanonicalName());
		paramType.setImplementationType(implType);

		ImplementationConfig serConfig = createConfig(ModelWriterTest.MODEL_CONVERSION_CLASS, "input", "serialize",
				true);
		implType.setSerializationConfig(serConfig);
		ImplementationConfig desConfig = createConfig(ModelWriterTest.MODEL_CONVERSION_CLASS, "input", "deserialize",
				true);
		implType.setDeserializationConfig(desConfig);
		uri1Param.setType(paramType);
		uri1Param.setName(name);
		return uri1Param;
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
		ParameterType uri1ParamType = new ParameterType();
		ImplementationType uri1ImplType = new ImplementationType();

		ImplementationConfig config = new ImplementationConfig();

		config.setClassName(String.class.getCanonicalName());
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		uri1ImplType.setDeserializationConfig(config);
		uri1ImplType.setSerializationConfig(config);
		uri1ImplType.setClassName(String.class.getCanonicalName());

		RDFType rdfType = new RDFType();
		rdfType.setName("anyURI");
		uri1ParamType.setRdfType(rdfType);

		uri1ParamType.setImplementationType(uri1ImplType);
		uri1Param.setType(uri1ParamType);
		uri1ParamType.setSerializationType(N3TemplateTest.anyURI);
		uri1Param.setName(name);
		return uri1Param;
	}

	public static Parameter createStringLiteralParameter(String name) throws Exception {
		Parameter uri1Param = new Parameter();
		ParameterType uri1ParamType = new ParameterType();
		ImplementationType impltype = new ImplementationType();

		ImplementationConfig config = new ImplementationConfig();

		config.setClassName(String.class.getCanonicalName());
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		impltype.setDeserializationConfig(config);
		impltype.setSerializationConfig(config);
		impltype.setClassName(String.class.getCanonicalName());

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
		Parameter uri1Param = new Parameter();
		ParameterType uri1ParamType = new ParameterType();
		ImplementationType uri1ImplType = new ImplementationType();

		ImplementationConfig config = new ImplementationConfig();

		config.setClassName(String.class.getCanonicalName());
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		uri1ImplType.setDeserializationConfig(config);
		uri1ImplType.setSerializationConfig(config);
		uri1ImplType.setClassName(String.class.getCanonicalName());

		RDFType rdfType = new RDFType();
		rdfType.setName("boolean");
		uri1ParamType.setRdfType(rdfType);

		uri1ParamType.setImplementationType(uri1ImplType);
		uri1Param.setType(uri1ParamType);
		uri1ParamType.setSerializationType(N3TemplateTest.anyURI);
		uri1Param.setName(name);
		return uri1Param;
	}

	public static Parameter createJsonParameter(String allVar) throws Exception {
		Parameter jsonParam = new Parameter();
		ParameterType jsonParamType = new ParameterType();
		ImplementationType jsonImplType = new ImplementationType();

		jsonImplType.setDeserializationConfig(getJsonDeserializationConfig());
		jsonImplType.setSerializationConfig(getJsonSerializationConfig());
		jsonImplType.setClassName(JsonNode.class.getCanonicalName());
		jsonImplType.setDefaultValue("{ }");
		jsonParamType.setImplementationType(jsonImplType);
		jsonParam.setType(jsonParamType);
		jsonParamType.setSerializationType(N3TemplateTest.stringType);
		jsonParam.setName(allVar);
		return jsonParam;
	}

	private static ImplementationConfig getJsonDeserializationConfig() throws ClassNotFoundException {
		ImplementationConfig config = new ImplementationConfig();
		config.setClassName(JsonFasterxmlNode.class.getCanonicalName());
		config.setMethodArguments("input");
		config.setMethodName("deserialize");
		config.setStaticMethod(true);
		return config;
	}

	private static ImplementationConfig getJsonSerializationConfig() throws ClassNotFoundException {
		ImplementationConfig config = new ImplementationConfig();
		config.setClassName(JsonFasterxmlNode.class.getCanonicalName());
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
		implType.setClassName(ByteArray.class.getCanonicalName());
		implType.setDefaultValue("");
		paramType.setImplementationType(implType);
		param.setType(paramType);
		paramType.setSerializationType(N3TemplateTest.stringType);
		param.setName(name);
		return param;
	}

	private static ImplementationConfig getByteArrayDeserializationConfig() throws ClassNotFoundException {
		ImplementationConfig config = new ImplementationConfig();
		config.setClassName(ByteArray.class.getCanonicalName());
		config.setMethodArguments("input");
		config.setMethodName("deserialize");
		config.setStaticMethod(true);
		return config;
	}

	private static ImplementationConfig getByteArraySerializationConfig() throws ClassNotFoundException {
		ImplementationConfig config = new ImplementationConfig();
		config.setClassName(ByteArray.class.getCanonicalName());
		config.setMethodArguments("");
		config.setMethodName("serialize");
		config.setStaticMethod(false);
		return config;
	}

}
