package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.jfact;

import java.io.StringReader;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Assert;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.BasicTBoxReasonerDriver;

public class JFactTBoxReasonerTest {
	
	private final static String axioms = "@prefix obo: <http://purl.obolibrary.org/obo/> .\r\n" + 
			"@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n" + 
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
			"@prefix xml: <http://www.w3.org/XML/1998/namespace> .\r\n" + 
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n" + 
			"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n" + 
			"\r\n" + 
			"<http://vivo.mydomain.edu/individual/class_c> rdf:type owl:Class ;\r\n" + 
			"         \r\n" + 
			"         rdfs:subClassOf [ rdf:type owl:Class ;\r\n" + 
			"                           owl:intersectionOf ( <http://vivo.mydomain.edu/individual/class_a>\r\n" + 
			"                                                <http://vivo.mydomain.edu/individual/class_b>\r\n" + 
			"                                              )\r\n" + 
			"                         ] .\r\n" + 
			"\r\n" + 
			"<http://vivo.mydomain.edu/individual/class_a>\r\n" + 
			"        a                        owl:Class ;\r\n" + 
			"        rdfs:label               \"Class A\"@en-US .\r\n" + 
			"\r\n" + 
			"<http://vivo.mydomain.edu/individual/class_b>\r\n" + 
			"        a                        owl:Class ;\r\n" + 
			"        rdfs:label               \"Class B\"@en-US .\r\n" + 
			"\r\n" + 
			"<http://vivo.mydomain.edu/individual/class_c>\r\n" + 
			"        a                        owl:Class ;\r\n" + 
			"        rdfs:label               \"Class C\"@en-US .\r\n";
	
	/**
	 * Test that axioms containing blank nodes can be removed from the reasoner
	 *  even if the internal blank node IDs are different from when first added.
	 */
	@Test
	public void testRemoveAxiomsWithBlankNodes() {
		OntModel tboxAssertions = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel tboxInferences = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel tboxUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
				ModelFactory.createUnion(tboxAssertions, tboxInferences));
		JFactTBoxReasoner reasoner = new JFactTBoxReasoner();
		BasicTBoxReasonerDriver driver = new BasicTBoxReasonerDriver(
				tboxAssertions, tboxInferences.getBaseModel(), tboxUnion, reasoner,
				ReasonerConfiguration.DEFAULT);
		Model additions = ModelFactory.createDefaultModel();
		additions.read(new StringReader(axioms), null, "TTL");
		// Reading again will generate new internal blank node IDs
		Model subtractions = ModelFactory.createDefaultModel();
		subtractions.read(new StringReader(axioms), null, "TTL");
		// Confirm that directly subtracting the models doesn't work because
		// the blank node IDs do not match
		Model incorrectSubtraction = additions.difference(subtractions);
		Assert.assertFalse(incorrectSubtraction.isEmpty());
		tboxAssertions.getBaseModel().add(additions);
		tboxAssertions.getBaseModel().notifyEvent(new EditEvent(null, false));
		waitForTBoxReasoning(driver);
		// Confirm that union model now contains inferred triples
		Assert.assertTrue(tboxUnion.size() > additions.size());
		JenaModelUtils.removeWithBlankNodesAsVariables(subtractions, tboxAssertions.getBaseModel());
		tboxAssertions.getBaseModel().notifyEvent(new EditEvent(null, false));
		waitForTBoxReasoning(driver);
		// Confirm that no statements related to classes a, b or c remain in the
		// TBox union model.  (The inference model may not be completely empty, because
		// the reasoner may supply unrelated triples related to OWL and RDFS vocabulary.)
		Assert.assertFalse(tboxUnion.contains(tboxUnion.getResource(
				"http://vivo.mydomain.edu/individual/class_a"), null, (RDFNode) null));
		Assert.assertFalse(tboxUnion.contains(tboxUnion.getResource(
				"http://vivo.mydomain.edu/individual/class_b"), null, (RDFNode) null));
		Assert.assertFalse(tboxUnion.contains(tboxUnion.getResource(
				"http://vivo.mydomain.edu/individual/class_c"), null, (RDFNode) null));
	}
	
	private void waitForTBoxReasoning(BasicTBoxReasonerDriver driver) {
		int sleeps = 0;
		// sleep at least once to make sure the TBox reasoning gets started
		while ((0 == sleeps) || ((sleeps < 1000) && driver.getStatus().isReasoning())) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			sleeps++;
		}
	}
	
}
