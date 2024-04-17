/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

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
            type.addInterface(Literal.class.getCanonicalName());
            RDFType rdfType = new RDFType();
            rdfType.setName(RDFType.LANG_STRING);
            type.setRdfType(rdfType);
            this.setType(type);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(LangStringLiteralParam.class.getCanonicalName());
        serializationConfig.setMethodName("serialize");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        serializationConfig.setInputInterface(Literal.class.getCanonicalName());
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
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
            String text = input.substring(1, i - 1);
            lang = input.substring(i + 1);
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
