package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Set;

public class SHACLBeanValidator extends SHACLValidator {

    public SHACLBeanValidator(Model data, Model scheme){
        super(data, scheme);
        queryText = "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX swrl:     <http://www.w3.org/2003/11/swrl#>\n" +
                "PREFIX swrlb:    <http://www.w3.org/2003/11/swrlb#>\n" +
                "PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
                "PREFIX bibo:     <http://purl.org/ontology/bibo/>\n" +
                "PREFIX c4o:      <http://purl.org/spar/c4o/>\n" +
                "PREFIX cito:     <http://purl.org/spar/cito/>\n" +
                "PREFIX dcterms:  <http://purl.org/dc/terms/>\n" +
                "PREFIX event:    <http://purl.org/NET/c4dm/event.owl#>\n" +
                "PREFIX fabio:    <http://purl.org/spar/fabio/>\n" +
                "PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX geo:      <http://aims.fao.org/aos/geopolitical.owl#>\n" +
                "PREFIX obo:      <http://purl.obolibrary.org/obo/>\n" +
                "PREFIX ocrer:    <http://purl.org/net/OCRe/research.owl#>\n" +
                "PREFIX ocresst:  <http://purl.org/net/OCRe/statistics.owl#>\n" +
                "PREFIX ocresd:   <http://purl.org/net/OCRe/study_design.owl#>\n" +
                "PREFIX ocresp:   <http://purl.org/net/OCRe/study_protocol.owl#>\n" +
                "PREFIX ro:       <http://purl.obolibrary.org/obo/ro.owl#>\n" +
                "PREFIX skos:     <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX swo:      <http://www.ebi.ac.uk/efo/swo/>\n" +
                "PREFIX vcard:    <http://www.w3.org/2006/vcard/ns#>\n" +
                "PREFIX vitro-public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n" +
                "PREFIX vivo:     <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX scires:   <http://vivoweb.org/ontology/scientific-research#>\n" +
                "PREFIX vann:     <http://purl.org/vocab/vann/>\n" +
                "PREFIX dynapi: <https://vivoweb.org/ontology/vitro-dynamic-api#> \n" +
                "\n" +
                "#\n" +
                "#\n" +
                "CONSTRUCT { ?s ?p ?o }\n" +
                "WHERE\n" +
                "{\n" +
                "  {?s ?p ?o. \n" +
                "  FILTER (?s = ?uri). }\n" +
                "  UNION\n" +
                "  {?uri ?p1 ?s .\n" +
                "  ?s ?p ?o. }\n" +
                "  \tUNION \n" +
                "  {?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  } \n" +
                "  \tUNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "   UNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?o3 .\n" +
                "    ?o3 ?p4 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "  UNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?o3 .\n" +
                "    ?o3 ?p4 ?o4 .\n" +
                "     ?o4 ?p5 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "  UNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?o3 .\n" +
                "    ?o3 ?p4 ?o4 .\n" +
                "     ?o4 ?p5 ?o5 .\n" +
                "     ?o5 ?p6 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "  \t  ?s rdf:type ?o9.\n" +
                "  \t  FILTER (strstarts(str(?o9), 'java:edu.cornell.mannlib.vitro.webapp.dynapi.components')). \n" +
                "}\n";
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
