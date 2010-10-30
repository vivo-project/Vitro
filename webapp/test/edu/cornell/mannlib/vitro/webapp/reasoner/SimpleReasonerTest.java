/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;


import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * 
 * 
 * 
 */

public class SimpleReasonerTest {
	
	@Test
	public void addTypes(){
	
		//  create a Tbox with a simple class hierarchy. B and C are both subclasses of A.
		
		OntModel tBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classA.setLabel("class C", "en-US");

        classA.addSubClass(classB);
        classA.addSubClass(classC);
        
		//  create an Abox with a statement that individual x is of type B.
		
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);
		aBox.add(xisb);
		
		//  Verify that the reasoner inferred that x is of type A
		
		Model inf = ModelFactory.createDefaultModel();
		SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		simpleReasoner.addedStatement(xisb);
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
	}

	
	// To help in debugging the unit test
	void printModels(OntModel ontModel) {
	    
		System.out.println("\nThe model has " + ontModel.size() + " statements:");
		System.out.println("---------------------------------------------------");
		ontModel.writeAll(System.out,"N3",null);
		
	}
	
}
