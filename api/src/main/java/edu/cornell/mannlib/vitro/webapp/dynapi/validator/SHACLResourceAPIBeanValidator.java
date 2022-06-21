package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;

public class SHACLResourceAPIBeanValidator extends SHACLBeanValidator {

    private static final String RESOURCE_API_URI = "https://vivoweb.org/ontology/vitro-dynamic-api#resourceAPI";

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
                                                "   {?uri (dynapi:hasNextStep|dynapi:nextIfSatisfied|dynapi:nextIfNotSatisfied|dynapi:hasOperation|dynapi:hasValidator|dynapi:hasCondition|dynapi:nextIfNotSatisfied|dynapi:providesParameter|dynapi:nextIfNotSatisfied|dynapi:requiresAccess|dynapi:nextIfNotSatisfied|dynapi:requiresParameter|dynapi:hasModel|dynapi:hasType|dynapi:hasElementsOfType|dynapi:hasInternalElement|dynapi:nextIfNotSatisfied|dynapi:onGet|dynapi:onGetAll|dynapi:onPost|dynapi:onDelete|dynapi:onPut|dynapi:onPatch|dynapi:hasCustomRESTAction|dynapi:forwardsTo|dynapi:hasDefaultMethod)* ?s . \n" +
                                                "          ?s ?p ?o .\n" +
                                                "   }\n" +
                                                "   ?s rdf:type ?type . \n"  +
                                                "   FILTER (strstarts(str(?type), 'java:edu.cornell.mannlib.vitro.webapp.dynapi.components')) . \n" +
                                                "}\n";

    public SHACLResourceAPIBeanValidator(Model data, Model scheme){
        super(data, scheme, SHACLResourceAPIBeanValidator.RESOURCE_API_URI, SHACLResourceAPIBeanValidator.SPARQL_QUERY);
    }

}
