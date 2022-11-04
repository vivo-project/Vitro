package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Literal;

public class LangStringLiteralParam extends Parameter {

	private static final String TYPE_NAME = "lang string literal";
	private static final Log log = LogFactory.getLog(LangStringLiteralParam.class);

	public LangStringLiteralParam(String var) {
		this.setName(var);
		try {
			ParameterType type = new ParameterType();
			type.setName(TYPE_NAME);
			ImplementationType implType = new ImplementationType();
			type.setImplementationType(implType);
			implType.setSerializationConfig(getSerializationConfig());
			implType.setDeserializationConfig(getDeserializationConfig());	
			implType.setClassName(Literal.class.getCanonicalName());
			RDFType rdfType = new RDFType();
			rdfType.setName(RDFType.LANG_STRING);
			type.setRdfType(rdfType);
			this.setType(type);
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	private ImplementationConfig getSerializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(LangStringLiteralParam.class.getCanonicalName());
		serializationConfig.setMethodName("serialize");
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		return serializationConfig;
	}
	
	private ImplementationConfig getDeserializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(LangStringLiteralParam.class.getCanonicalName());
		serializationConfig.setMethodName("deserialize");
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		return serializationConfig;
	}
	
	public static Literal deserialize(String input) {
		boolean match = input.matches("^\".*\"@[a-zA-Z_-]+$");
		String lang = "";
		if (match) {
			int i = input.lastIndexOf("@");
			String text = input.substring(1,i-1);
			lang = input.substring(i+1);
			return ResourceFactory.createLangLiteral(text, lang);
		} 
		return ResourceFactory.createLangLiteral(input, lang);

	}
	
	public static String serialize(Literal literal) {
		String lang = literal.getLanguage();
		if (StringUtils.isBlank(lang)) {
			return literal.getLexicalForm();
		}
		return "" + literal.getLexicalForm() + "@" + lang;
	}
}
