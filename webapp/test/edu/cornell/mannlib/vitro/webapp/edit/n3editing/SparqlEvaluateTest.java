/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.SparqlEvaluate;

public class SparqlEvaluateTest {
    SparqlEvaluate sEval;
    
    @Before
    public void setUp() throws Exception {
        EditConfiguration edConfig = new EditConfiguration();
        
        Model model = ModelFactory.createDefaultModel(); //just used to parse sparql        
        sEval = new SparqlEvaluate(model);
    }

    @Test
    public void testForNoSolution() {
        String uri = sEval.queryToUri("SELECT ?cat WHERE { ?cat <http://cornell.edu#hasOwner> <http://cornell.edu#bdc34>} ");
        Assert.assertNull( uri );      
    }

}
