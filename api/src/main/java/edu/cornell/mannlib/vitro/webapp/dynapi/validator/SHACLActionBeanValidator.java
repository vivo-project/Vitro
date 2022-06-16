package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Set;

public class SHACLActionBeanValidator extends SHACLBeanValidator {

    private static final String ACTION_URI = "https://vivoweb.org/ontology/vitro-dynamic-api#action";

    private static final String SPARQL_QUERY =  "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                                "PREFIX dynapi: <https://vivoweb.org/ontology/vitro-dynamic-api#>\n" +
                                                "CONSTRUCT \n" +
                                                "{ \n" +
                                                "   ?s ?p ?o \n" +
                                                "}\n" +
                                                "WHERE\n" +
                                                "{\n" +
                                                "   {   ?s ?p ?o . \n" +
                                                "       FILTER (?s = ?uri) . } \n " +
                                                "  \n" +
                                                "   UNION \n" +
                                                "  \n" +
                                                "   {?uri (dynapi:hasFirstStep|dynapi:hasNextStep|dynapi:nextIfSatisfied|dynapi:nextIfNotSatisfied|dynapi:hasOperation|dynapi:hasValidator|dynapi:hasCondition|dynapi:nextIfNotSatisfied|dynapi:providesParameter|dynapi:nextIfNotSatisfied|dynapi:requiresAccess|dynapi:nextIfNotSatisfied|dynapi:requiresParameter|dynapi:hasModel|dynapi:hasType|dynapi:hasElementsOfType|dynapi:hasInternalElement|dynapi:nextIfNotSatisfied|dynapi:hasAssignedRPC|dynapi:hasDefaultMethod)* ?s . \n" +
                                                "          ?s ?p ?o .\n" +
                                                "   }\n" +
                                                "   ?s rdf:type ?type . \n"  +
                                                "   FILTER (strstarts(str(?type), 'java:edu.cornell.mannlib.vitro.webapp.dynapi.components')) . \n" +
                                                "}\n";

    public SHACLActionBeanValidator(Model data, Model scheme){
        super(data, scheme, SHACLActionBeanValidator.ACTION_URI, SHACLActionBeanValidator.SPARQL_QUERY);
    }

}
