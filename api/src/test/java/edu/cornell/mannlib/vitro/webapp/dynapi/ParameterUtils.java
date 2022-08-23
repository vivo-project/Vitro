package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.ModelWriterTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.N3TemplateTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;

public class ParameterUtils {

	public static ImplementationConfig createConfig(String className, String methodArgs, String method, boolean isStatic)
			throws ClassNotFoundException {
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
		implType.setName(Model.class.getCanonicalName());
		paramType.setImplementationType(implType);
	
		ImplementationConfig serConfig = createConfig(ModelWriterTest.MODEL_CONVERSION_CLASS, "input", "serialize", true);
		implType.setSerializationConfig(serConfig);
		ImplementationConfig desConfig = createConfig(ModelWriterTest.MODEL_CONVERSION_CLASS, "input", "deserialize", true);
		implType.setDeserializationConfig(desConfig);
		uri1Param.setType(paramType);
		uri1Param.setName(name);
		return uri1Param;
	}

	public static Statement addStatement(OntModelImpl additionModel, String s, String p, String o) {
		final Resource resource = ResourceFactory.createResource(s);
		final Property property = ResourceFactory.createProperty(p);
		final Literal literal = ResourceFactory.createPlainLiteral(o);
		Statement stmt = ResourceFactory.createStatement(resource, property, literal);
		additionModel.add(stmt);
		return stmt;
	}

	public static Parameter createUriParameter(String name) throws Exception {
		Parameter uri1Param = new Parameter();
	    ParameterType uri1ParamType = new ParameterType();
	    ImplementationType uri1ImplType = new ImplementationType();
	    
	    ImplementationConfig config = new ImplementationConfig();
		
		config.setClassName("java.lang.String");
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		uri1ImplType.setDeserializationConfig(config);
		uri1ImplType.setSerializationConfig(config);
		uri1ImplType.setName("java.lang.String");
	
		RDFType rdfType = new RDFType();
		rdfType.setName("anyURI");
		uri1ParamType.setRdfType(rdfType);
		
		uri1ParamType.setImplementationType(uri1ImplType );
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
		
		config.setClassName("java.lang.String");
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		impltype.setDeserializationConfig(config);
		impltype.setSerializationConfig(config);
		impltype.setName("java.lang.String");
		
		RDFType rdfType = new RDFType();
		rdfType.setName("string");
		uri1ParamType.setRdfType(rdfType);
	
		uri1ParamType.setImplementationType(impltype );
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
		
		config.setClassName("java.lang.String");
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		uri1ImplType.setDeserializationConfig(config);
		uri1ImplType.setSerializationConfig(config);
		uri1ImplType.setName("java.lang.String");
	
		RDFType rdfType = new RDFType();
		rdfType.setName("boolean");
		uri1ParamType.setRdfType(rdfType);
		
		uri1ParamType.setImplementationType(uri1ImplType );
		uri1Param.setType(uri1ParamType);
	    uri1ParamType.setSerializationType(N3TemplateTest.anyURI);
	    uri1Param.setName(name);
		return uri1Param;
	}

}
