package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;

public class SHACLResourceAPIBeanValidator extends SHACLBeanValidator {

    private static final String RESOURCE_API_URI = "https://vivoweb.org/ontology/vitro-dynamic-api#action";

    public SHACLResourceAPIBeanValidator(Model data, Model scheme){
        super(data, scheme, SHACLResourceAPIBeanValidator.RESOURCE_API_URI);
    }

}
