/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api;

import static org.junit.Assert.*;

import java.io.StringReader;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

/**
 * Test that the SparqlQueryApiExecutor can handle all query types and all
 * formats.
 */
public class SparqlUpdateApiTest extends AbstractTestClass {

    private final String GRAPH_URI = "http://example.org/graph"; 
    
    private final String updateStr1 = 
        "INSERT DATA { GRAPH <" + GRAPH_URI + "> { \n" +
        "    <http://here.edu/n1> a <http://here.edu/Class1> . \n" +        
        "} } ; \n" +
        "INSERT { GRAPH <" + GRAPH_URI + "> { \n " +
        "     ?x a <http://here.edu/Class2> . \n " +  
        "} } WHERE { \n" +
        "    GRAPH <" + GRAPH_URI + "> { ?x a <http://here.edu/Class1> } \n " + 
        "}";

    private final String result1 = 
        "<http://here.edu/n1> a <http://here.edu/Class1> . \n" +
        "<http://here.edu/n1> a <http://here.edu/Class2> ." ;
    
    // look at how the SimpleReasoner is set up.
    
    private Model model;
    private RDFService rdfService;

    @Before
    public void setup() {
        model = ModelFactory.createDefaultModel();
        Dataset ds = DatasetFactory.createMem();
        ds.addNamedModel(GRAPH_URI, model);
        rdfService = new RDFServiceModel(ds);
    }

    // ----------------------------------------------------------------------
    // Tests
    // ----------------------------------------------------------------------

    @Test
    public void nullRdfService() throws Exception {
        model.removeAll();
        Model desiredResults = ModelFactory.createDefaultModel();
        desiredResults.read(new StringReader(result1), null, "N3");
        Dataset ds = new RDFServiceDataset(rdfService);
        GraphStore graphStore = GraphStoreFactory.create(ds);
        try {
            if(ds.supportsTransactions()) {
                ds.begin(ReadWrite.WRITE);
                System.out.println("yep");
            }
            UpdateAction.execute(UpdateFactory.create(updateStr1), graphStore);
        } finally {
            if(ds.supportsTransactions()) {
                ds.commit();
                ds.end();
            }
        }
        assertEquals("updateStr1 yields result1", desiredResults.toString(), model.toString());
    }

}
