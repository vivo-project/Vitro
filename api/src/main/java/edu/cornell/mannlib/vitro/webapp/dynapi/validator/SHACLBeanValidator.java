package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Set;

public class SHACLBeanValidator extends SHACLValidator {

    private static final String JAVA_BINDING_PREFIX = "java:edu.cornell.mannlib.vitro.webapp.dynapi.components";

    private static final String SPARQL_QUERY =  "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                                "CONSTRUCT { ?s ?p ?o }\n" +
                                                "WHERE\n" +
                                                "{\n" +
                                                "  {?s ?p ?o. \n" +
                                                "  FILTER (?s = ?uri). }\n" +
                                                "   ?s rdf:type ?type. \n"  +
                                                "   FILTER (strstarts(str(?type), 'java:edu.cornell.mannlib.vitro.webapp.dynapi.components')). \n" +
                                                "}\n";

    public SHACLBeanValidator(Model data, Model scheme, String rootUri, String sparqlQuery){
        super(data, scheme, rootUri, (sparqlQuery   !=  null)  ?   sparqlQuery :   SHACLBeanValidator.SPARQL_QUERY);
    }


    @Override
    protected boolean shouldBeValidated(Resource resource){
        boolean retVal = false;
        if (!map.containsKey(resource.getURI())) {
            Set<Statement> properties = resource.listProperties().toSet();
            for (Statement statement : properties) {
                if (statement.getPredicate().getLocalName().equals("type")) {
                    if (statement.getObject().toString().contains(SHACLBeanValidator.JAVA_BINDING_PREFIX)) {
                        retVal = true;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

}
