/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

public class StringPlainLiteralParam extends Parameter {

    private static final Log log = LogFactory.getLog(StringPlainLiteralParam.class);

    public StringPlainLiteralParam(String var) {
        this.setName(var);
        try {
            ParameterType type = getPlainStringLiteralType();
            this.setType(type);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public static ParameterType getPlainStringLiteralType() {
        ParameterType type = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();
        type.addFormat(defaultFormat);
        try {
            defaultFormat.setSerializationConfig(getSerializationConfig());
            defaultFormat.setDeserializationConfig(getDeserializationConfig());
            type.addInterface(Literal.class);
            RDFType rdfType = new RDFType();
            rdfType.setName("string");
            type.setRdfType(rdfType);
        } catch (ClassNotFoundException e) {
            log.error(e, e);
        }
        return type;
    }

    private static ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(Literal.class);
        serializationConfig.setMethodName("getLexicalForm");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        serializationConfig.setInputInterface(Literal.class);
        return serializationConfig;
    }

    private static ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(ResourceFactory.class);
        serializationConfig.setMethodName("createPlainLiteral");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }
}
