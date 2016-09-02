/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.ingest;

import java.io.StringWriter;

import org.junit.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;

public class JenaIngestUtilsTest {
    
    private final Log log = LogFactory.getLog(JenaIngestUtilsTest.class);
    
    protected JenaIngestUtils utils = new JenaIngestUtils();
    
    protected Model makeModel() {
        Model base = ModelFactory.createDefaultModel();
        RDFService rdfService = new RDFServiceModel(base);
        return RDFServiceGraph.createRDFServiceModel(
                new RDFServiceGraph(rdfService));
    }
    
    @Test
    public void testSmush() {
        Model model = makeModel();
        model.read(JenaIngestUtilsTest.class.getResourceAsStream(
                "smush.start.n3"), null, "N3");
        JenaIngestUtils utils = new JenaIngestUtils();
        Model actualResult = utils.smushResources(
                model, model.getProperty("http://example.com/ns/duckCode"));
        boolean matchesPossibleResult = false;
        for(int i = 1; i < 7; i++) {
            Model possibleResult = ModelFactory.createDefaultModel();
            possibleResult.read(JenaIngestUtilsTest.class.getResourceAsStream(
                    "smush.end." + i + ".n3"), null, "N3");
            if(actualResult.isIsomorphicWith(possibleResult)) {
                matchesPossibleResult = true;
                break;
            }
        }
        if (!matchesPossibleResult) {
            StringWriter s = new StringWriter();
            actualResult.write(s, "N3");
            Assert.fail("Smushed model does not match one of the possible results:\n" +
            		s.toString());
        }
    }
    
    @Test
    public void testRenameBNodes() {
        Model initialState = ModelFactory.createDefaultModel();
        initialState.read(JenaIngestUtilsTest.class.getResourceAsStream(
                "renameBlank.n3"), null, "N3");
        Model renamedState = utils.renameBNodes(
                initialState, "http://example.org/node/n");
        Assert.assertEquals("Post-rename model is not the same size as the " + 
                "initial model", initialState.size(), renamedState.size());
        StmtIterator sit = renamedState.listStatements();
        boolean lingeringBNodes = false;
        while(sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if(stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
                lingeringBNodes = true;
            }
        }
        if(lingeringBNodes) {
            StringWriter s = new StringWriter();
            renamedState.write(s, "N3");            
            Assert.fail("Renamed model still contains blank nodes \n" + 
                    s.toString());
        }
    }
    
    @Test
    public void testGenerateTBox() {
        Model abox = ModelFactory.createDefaultModel();
        abox.read(JenaIngestUtilsTest.class.getResourceAsStream(
                "abox.n3"), null, "N3");
        Model tbox = ModelFactory.createDefaultModel();
        tbox.read(JenaIngestUtilsTest.class.getResourceAsStream(
                "tbox.n3"), null, "N3");
        Model generatedTBox = utils.generateTBox(abox);
        //log.warn(tbox.toString());
        Assert.assertTrue("Generated TBox does not match expected result", 
                tbox.isIsomorphicWith(generatedTBox));
    }
    
    @Test 
    public void testDoMerge() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, makeModel());
        OntModel tbox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, makeModel());
        model.read(JenaIngestUtilsTest.class.getResourceAsStream(
                "merge.n3"), null, "N3");
        tbox.read(JenaIngestUtilsTest.class.getResourceAsStream("tbox.n3"), null, "N3");
        Model expectedMergeMultipleLabels = model.read(
                JenaIngestUtilsTest.class.getResourceAsStream(
                        "mergeResultMultipleLabels.n3"), null, "N3");
        utils.doMerge("http://example.com/ns/n1", "http://example.com/ns/n1", model, tbox, false);
        Assert.assertTrue("Merged model with multiple labels does not match " +
                "expected result", expectedMergeMultipleLabels.isIsomorphicWith(model));
        
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, makeModel());
        model.read(JenaIngestUtilsTest.class.getResourceAsStream(
                "merge.n3"), null, "N3");
        Model expectedMergeSingleLabel = model.read(
                JenaIngestUtilsTest.class.getResourceAsStream(
                        "mergeResultSingleLabel.n3"), null, "N3");
        utils.doMerge("http://example.com/ns/n1", "http://example.com/ns/n1", model, tbox, true);
        Assert.assertTrue("Merged model with multiple labels does not match " +
                "expected result", expectedMergeSingleLabel.isIsomorphicWith(model));

    }

}
