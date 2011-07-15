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
}
