package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Set;

public class SHACLActionBeanValidator extends SHACLBeanValidator {

    private static final String ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api#action";

    public SHACLActionBeanValidator(Model data, Model scheme){
        super(data, scheme, SHACLActionBeanValidator.ACTION_URI);
    }

}
