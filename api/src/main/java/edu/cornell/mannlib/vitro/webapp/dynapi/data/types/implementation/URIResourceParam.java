/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class URIResourceParam extends Parameter {

    private static final String TYPE_NAME = "uri";
    private static final Log log = LogFactory.getLog(URIResourceParam.class);

    public URIResourceParam(String var) {
        this.setName(var);
        try {
            ParameterType type = getUriResourceType();
            this.setType(type);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public static ParameterType getUriResourceType() throws ClassNotFoundException {
        ParameterType type = new ParameterType();
        type.setName(TYPE_NAME);
        ImplementationType implType = new ImplementationType();
        type.setImplementationType(implType);
        ConversionConfiguration serializationConfig = getSerializationConfig();
        ConversionConfiguration deserializationConfig = getDeserializationConfig();
        implType.setSerializationConfig(serializationConfig);
        implType.setDeserializationConfig(deserializationConfig);
        type.addInterface(Resource.class.getCanonicalName());
        RDFType rdfType = new RDFType();
        rdfType.setName(RDFType.ANY_URI);
        type.setRdfType(rdfType);
        return type;
    }

    private static ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(Resource.class.getCanonicalName());
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        serializationConfig.setInputInterface(Resource.class.getCanonicalName());
        return serializationConfig;
    }

    private static ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(ResourceImpl.class.getCanonicalName());
        // invoke constructor
        serializationConfig.setMethodName("");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }
}
