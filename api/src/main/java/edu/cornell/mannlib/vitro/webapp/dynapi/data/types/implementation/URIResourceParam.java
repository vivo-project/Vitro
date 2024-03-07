package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
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
        ImplementationConfig serializationConfig = getSerializationConfig();
        ImplementationConfig deserializationConfig = getDeserializationConfig();
        implType.setSerializationConfig(serializationConfig);
        implType.setDeserializationConfig(deserializationConfig);
        implType.setClassName(Resource.class.getCanonicalName());
        RDFType rdfType = new RDFType();
        rdfType.setName(RDFType.ANY_URI);
        type.setRdfType(rdfType);
        return type;
    }

    private static ImplementationConfig getSerializationConfig() throws ClassNotFoundException {
        ImplementationConfig serializationConfig = new ImplementationConfig();
        serializationConfig.setClassName(Resource.class.getCanonicalName());
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        return serializationConfig;
    }

    private static ImplementationConfig getDeserializationConfig() throws ClassNotFoundException {
        ImplementationConfig serializationConfig = new ImplementationConfig();
        serializationConfig.setClassName(ResourceImpl.class.getCanonicalName());
        // invoke constructor
        serializationConfig.setMethodName("");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }
}
