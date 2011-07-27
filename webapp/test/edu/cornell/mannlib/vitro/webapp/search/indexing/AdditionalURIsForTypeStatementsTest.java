/* $This file is distributed under the terms of the license in /doc/license.txt$ */
/**
 * 
 */
package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author bdc34
 *
 */
public class AdditionalURIsForTypeStatementsTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link edu.cornell.mannlib.vitro.webapp.search.indexing.AdditionalURIsForTypeStatements#findAdditionalURIsToIndex(com.hp.hpl.jena.rdf.model.Statement)}.
     */
    @Test
    public void testFindAdditionalURIsToIndex() {
        AdditionalURIsForTypeStatements aufts = new AdditionalURIsForTypeStatements();

        String subject = "http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n3270";
        Statement typeChangeStatement =  ResourceFactory.createStatement(                
                ResourceFactory.createResource(subject),
                ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                ResourceFactory.createResource( "http://caruso-laptop.mannlib.cornell.edu:8090/vivo/ontology/localOnt#LocalInternalClass"));
                  
        
        List<String> uris = aufts.findAdditionalURIsToIndex( typeChangeStatement );
        
        Assert.assertNotNull(uris);
        Assert.assertTrue("Did not contain subject of type change statement", uris.contains(subject));
    }

}
