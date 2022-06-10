package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Set;

public class SHACLBeanValidator extends SHACLValidator {

    public SHACLBeanValidator(Model data, Model scheme){
        super(data, scheme);
    }

    @Override
    protected boolean shouldBeValidated(Resource resource){
        boolean retVal = false;
        Set<Statement> properties = resource.listProperties().toSet();
        for (Statement statement : properties) {
            if (statement.getPredicate().getLocalName().equals("type")) {
                if (statement.getObject().toString().contains("java:")) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

}
