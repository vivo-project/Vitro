/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.*;

import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class ObjectPropertyStatementDaoJenaTest {

    /**
     * Test if jena lib can parse N3 that it generates.
     * owl:sameAs has been a problem when it is represetned
     * in N3 with the character =
     */
    @Test
    public void testN3WithSameAs() {
        
        String n3WithSameAs = " <http://example.com/bob> = <http://example.com/robert> .";
        
        try{
            Model m = ModelFactory.createDefaultModel();
            m.read(n3WithSameAs, null, "N3");
            fail( "If this test fails it means that jena now correctly parses = when reading N3.");
        }catch(Exception ex ){
            
            
        }
    }

}
