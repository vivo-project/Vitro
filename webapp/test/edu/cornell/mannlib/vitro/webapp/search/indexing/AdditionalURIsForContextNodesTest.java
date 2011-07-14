/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class AdditionalURIsForContextNodesTest {

    @Test 
    public void testPositionChanges(){
        String n3 = 
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://vivoweb.org/ontology/core#organizationForPosition> <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> <http://vivoweb.org/ontology/core#AcademicDepartment> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#Department> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Organization> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#AcademicDepartment> . \n" +
                        
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#DependentResource> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#Position> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#FacultyPosition> . \n" +

        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932> <http://vivoweb.org/ontology/core#personInPosition> <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5431> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> <http://vivoweb.org/ontology/core#FacultyMember> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#FacultyMember> . \n" +                       
                
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5398> <http://vivoweb.org/ontology/core#start> <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5425> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5398> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> <http://vivoweb.org/ontology/core#DateTimeInterval> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5398> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5398> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#DateTimeInterval> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5425> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5425> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/core#DateTimeValue> . \n" +

        "<http://vivoweb.org/ontology/core#AcademicDepartment> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +        
        "<http://vivoweb.org/ontology/core#Position> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
        "<http://vivoweb.org/ontology/core#FacultyMember> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
        "<http://xmlns.com/foaf/0.1/Organization> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
        "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
        "<http://xmlns.com/foaf/0.1/Agent> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
        "<http://vivoweb.org/ontology/core#DateTimeInterval> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
        "<http://vivoweb.org/ontology/core#Department> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" ;
        
        //make a test model with an person, an authorship context node and a book 
        OntModel model = ModelFactory.createOntologyModel();
        model.read( new StringReader(n3), null,  "N3");
                
        //make an AdditionalURIsForContextNodesTest object with that model
        AdditionalURIsForContextNodes uriFinder = new AdditionalURIsForContextNodes( model );
        
        //if the person changes then the org needs to be updated
        List<String> uris = uriFinder.findAdditionalURIsToIndex( "http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932");       
        assertTrue("did not find org for context node", uris.contains("http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423" ));
        
        //if the org changes then the person needs to be updated
        uris = uriFinder.findAdditionalURIsToIndex( "http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5423");       
        assertTrue("did not find person for context node", uris.contains("http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n932" ));                        
    }
    
    @Test
    public void testPersonOnOrgChange() {                

        String n3 ="@prefix dc:      <http://purl.org/dc/elements/1.1/> . \n" +
        "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> . \n" +
        "@prefix swrl:    <http://www.w3.org/2003/11/swrl#> . \n" +
        "@prefix vitro:   <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> . \n" +
        "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> . \n" +
        "@prefix swrlb:   <http://www.w3.org/2003/11/swrlb#> . \n" +
        "@prefix owl:     <http://www.w3.org/2002/07/owl#> . \n" +
        "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n" +
        "@prefix core:    <http://vivoweb.org/ontology/core#> . \n" +
        "@prefix vivo:    <http://vivo.library.cornell.edu/ns/0.1#> . \n" +
        " " +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2577> \n" +
        "      a       owl:Thing , core:Role , core:LeaderRole ; \n" +
        "      rdfs:label \"head\"^^xsd:string ; \n" +
        "      vitro:mostSpecificType \n" +
        "              core:LeaderRole ; \n" +
        "      core:dateTimeInterval \n" +
        "              <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2594> ; \n" +
        "      core:leaderRoleOf <http://vivo.scripps.edu/individual/n14979> ; \n" +
        "      core:roleIn <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2592> . \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2592> \n" +
        "      a       <http://xmlns.com/foaf/0.1/Organization> , owl:Thing , <http://xmlns.com/foaf/0.1/Agent> , core:ClinicalOrganization ; \n" +
        "      rdfs:label \"Organization XYZ\"^^xsd:string ; \n" +
        "      vitro:mostSpecificType \n" +
        "              core:ClinicalOrganization ; \n" +
        "      core:relatedRole <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2577> . \n"; 
        
        
        //make a test model with an person, an authorship context node and a book 
        OntModel model = ModelFactory.createOntologyModel();
        model.read( new StringReader(n3), null,  "N3");
       
         
        //make an AdditionalURIsForContextNodesTest object with that model
        AdditionalURIsForContextNodes uriFinder = new AdditionalURIsForContextNodes( model );
        
        //get additional uris for org
        List<String> uris = uriFinder.findAdditionalURIsToIndex( "http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2592");
       
        assertTrue("did not find person for context node", uris.contains("http://vivo.scripps.edu/individual/n14979" ));
                
    }
    
    

}
