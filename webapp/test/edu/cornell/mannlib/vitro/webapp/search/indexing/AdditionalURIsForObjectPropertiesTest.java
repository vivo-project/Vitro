/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AdditionalURIsForObjectPropertiesTest {

    Model model;
    
    String testNS = "http://example.com/test#";
    String n3 = "" +
    	"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  . \n" +
        "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> . \n" +
        "@prefix test:  <"+ testNS + "> . \n" +
        "\n" +
        "test:bob rdfs:label \"Mr Bob\" .  \n" +
        "test:bob test:hatsize \"8 1/2 inches\" .  \n" +
        "test:bob test:likes test:icecream .  \n" +
        "test:bob test:likes test:onions .  \n" +
        "test:bob test:likes test:cheese .  \n" +
        "test:bob a test:Person .  \n" +
        "test:bob a owl:Thing .  \n" +
        "test:bob test:likes [ rdfs:label \"this is a blank node\" ] . ";
    
    @Before
    public void setUp() throws Exception {
        model = ModelFactory.createDefaultModel();
        model.read(new StringReader(n3 ), null , "N3");
    }

    @Test
    public void testChangeOfRdfsLabel() {
        AdditionalURIsForObjectProperties aufop = new AdditionalURIsForObjectProperties(model);
        List<String> uris = aufop.findAdditionalURIsToIndex( 
                ResourceFactory.createStatement(
                        ResourceFactory.createResource(testNS + "bob"),
                        RDFS.label,
                        ResourceFactory.createPlainLiteral("Some new label for bob")));
        
        Assert.assertNotNull(uris);
        Assert.assertTrue("uris was empty", uris.size() > 0 );        
        
        Assert.assertTrue("uris didn't not contain test:onions", uris.contains(testNS+"onions"));
        Assert.assertTrue("uris didn't not contain test:cheese", uris.contains(testNS+"cheese"));
        Assert.assertTrue("uris didn't not contain test:icecream", uris.contains(testNS+"icecream"));
        
        Assert.assertTrue("uris contained test:Person", !uris.contains(testNS+"Person"));
        Assert.assertTrue("uris contained owl:Thing", !uris.contains( OWL.Thing.getURI() ));
        
        Assert.assertEquals(3, uris.size());
    }
    
    @Test
    public void testChangeOfObjPropStmt() {
        
        AdditionalURIsForObjectProperties aufop = new AdditionalURIsForObjectProperties(model);
        List<String> uris = aufop.findAdditionalURIsToIndex( 
                ResourceFactory.createStatement(
                        ResourceFactory.createResource(testNS + "bob"),
                        ResourceFactory.createProperty(testNS+"likes"),
                        ResourceFactory.createResource(testNS+"cheese")));
        
        Assert.assertNotNull(uris);
        Assert.assertTrue("uris was empty", uris.size() > 0 );        
        
        Assert.assertTrue("uris didn't not contain test:cheese", uris.contains(testNS+"cheese"));
        
        Assert.assertTrue("uris contained test:Person", !uris.contains(testNS+"Person"));
        Assert.assertTrue("uris contained owl:Thing", !uris.contains( OWL.Thing.getURI() ));
        Assert.assertTrue("uris contained test:onions", !uris.contains(testNS+"onions"));        
        Assert.assertTrue("uris contained test:icecream", !uris.contains(testNS+"icecream"));
        
        Assert.assertEquals(1, uris.size());
    }
    
    @Test
    public void testOfDataPropChange() {
        AdditionalURIsForObjectProperties aufop = new AdditionalURIsForObjectProperties(model);
        List<String> uris = aufop.findAdditionalURIsToIndex( 
                ResourceFactory.createStatement(
                        ResourceFactory.createResource(testNS + "bob"),
                        ResourceFactory.createProperty(testNS+"hatsize"),
                        ResourceFactory.createPlainLiteral("Some new hat size for bob")));
        
        Assert.assertNotNull(uris);
        Assert.assertTrue("uris was not empty", uris.size() == 0 );                        
    }
    
    // For NIHVIVO-2902
    @Test
    public void testNIHVIVO_2902 (){
        //Update search index for research area when a statement is
        //removed between a person and the research area.
        
        Model model = ModelFactory.createDefaultModel();
        model.read(new StringReader( n3ForNIHVIVO_2902 ), null , "N3");
        
        AdditionalURIsForObjectProperties aufop = new AdditionalURIsForObjectProperties(model);
        List<String> uris = aufop.findAdditionalURIsToIndex( 
                ResourceFactory.createStatement(
                        ResourceFactory.createResource("http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2241"),
                        ResourceFactory.createProperty("http://vivoweb.org/ontology/core#hasResearchArea"),
                        ResourceFactory.createResource("http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n7416")));
        
        Assert.assertNotNull(uris);
        Assert.assertTrue("uris was empty", uris.size() > 0 );
        
        Assert.assertTrue("NIHVIVO-2902 regression, research area is not getting reindexed", uris.contains("http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n7416"));       
    }
    
    // For NIHVIVO-2902
    String n3ForNIHVIVO_2902 = 
        "@prefix dc:      <http://purl.org/dc/elements/1.1/> . \n" +
        "@prefix pvs:     <http://vivoweb.org/ontology/provenance-support#> . \n" +
        "@prefix geo:     <http://aims.fao.org/aos/geopolitical.owl#> . \n" +
        "@prefix foaf:    <http://xmlns.com/foaf/0.1/> . \n" +
        "@prefix scires:  <http://vivoweb.org/ontology/scientific-research#>  . \n" +
        "@prefix scripps:  <http://vivo.scripps.edu/> . \n" +
        "@prefix dcterms:  <http://purl.org/dc/terms/> . \n" +
        "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> . \n" +
        "@prefix swrl:    <http://www.w3.org/2003/11/swrl#> . \n" +
        "@prefix vitro:   <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>. \n" +
        "@prefix event:   <http://purl.org/NET/c4dm/event.owl#> . \n" +
        "@prefix bibo:    <http://purl.org/ontology/bibo/> . \n" +
        "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> . \n" +
        "@prefix owl:     <http://www.w3.org/2002/07/owl#> . \n" +
        "@prefix swrlb:   <http://www.w3.org/2003/11/swrlb#> . \n" +
        "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n" +
        "@prefix core:    <http://vivoweb.org/ontology/core#> . \n" +
        "@prefix skos:    <http://www.w3.org/2004/02/skos/core#> . \n" +
        "@prefix vivo:    <http://vivo.library.cornell.edu/ns/0.1#> . \n" +
        "@prefix dcelem:  <http://purl.org/dc/elements/1.1/> . \n" +
        "@prefix ero:     <http://purl.obolibrary.org/obo/> . \n" +
        " \n" +
        "<http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2241> \n" +
        "      a       core:FacultyMember , foaf:Person , owl:Thing , foaf:Agent ; \n" +
        "      rdfs:label \"Faculty, Jane\" ; \n" +
        "      vitro:modTime \"2011-07-15T15:08:35\"^^xsd:dateTime ; \n" +
        "      vitro:mostSpecificType \n" +
        "              core:FacultyMember ; \n" +
        "      core:hasResearchArea \n" +
        "              <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n7416> ; \n" +
        "      core:mailingAddress <http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n5993> ; \n" +
        "      foaf:firstName \"Jane\"^^xsd:string ; \n" +
        "      foaf:lastName \"Faculty\"^^xsd:string . ";
}
