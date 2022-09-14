package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;

public class LiteralParamFactory {

	private static final String LANG_STRING_DATA_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
	private static final String PLAIN_STRING_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#string";

	public static Parameter createLiteral(Literal literal, String var) {
		RDFDatatype dataType = literal.getDatatype();
		String dataTypeUri = dataType.getURI();

		if (LANG_STRING_DATA_TYPE.equals(dataTypeUri)) {
			return new LangStringLiteralParam(var);
		}

		if (PLAIN_STRING_DATA_TYPE.equals(dataTypeUri)) {
			return new StringPlainLiteralParam(var);
		}
		throw new RuntimeException(
				"Parameter for " + dataTypeUri + " not implemented in " + LiteralParamFactory.class.getSimpleName());
	}
}
