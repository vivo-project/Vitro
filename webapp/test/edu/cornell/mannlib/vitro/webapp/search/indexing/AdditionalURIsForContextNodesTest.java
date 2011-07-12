package edu.cornell.mannlib.vitro.webapp.search.indexing;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class AdditionalURIsForContextNodesTest {

    @Test
    public void testFindAdditionalURIsToIndex() {
                
//        //make a test model with an person, an authorship context node and a book 
//        OntModel model = ModelFactory.createOntologyModel();        
//        
//        //make an AdditionalURIsForContextNodesTest object with that model
//        AdditionalURIsForContextNodes uriFinder = new AdditionalURIsForContextNodes( model );
//        
//        //execute the method and check the results
//        List<String> uris = uriFinder.findAdditionalURIsToIndex( "http://example.com/personA");
//       
//        assertTrue("could not find authorship context node", uris.contains("http://example.com/authorshipNode"));
//        assertTrue("could not find book indivdiual", uris.contains("http://example.com/bookA"));        
    }

}
