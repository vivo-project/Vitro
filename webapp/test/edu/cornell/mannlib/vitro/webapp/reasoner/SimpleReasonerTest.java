/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;


import org.junit.Assert;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;


public class SimpleReasonerTest {
	
	@Test
	public void addTypes(){
	
		// Create a Tbox with a simple class hierarchy. D and E are subclasses of C. B and C are subclasses of A.
		// Pellet will compute TBox inferences
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classA.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classA.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classA.setLabel("class E", "en-US");
	    
	    classC.addSubClass(classD);
	    classC.addSubClass(classE);
	    
        classA.addSubClass(classB);
        classA.addSubClass(classC);
        
        // this is the model to receive ABox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // add a statement to the ABox that individual x is of type E.
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classE);		

		// Verify that "x is of type C" was inferred
		Statement xisc = ResourceFactory.createStatement(ind_x, RDF.type, classC);	
		Assert.assertTrue(inf.contains(xisc));	
		
		// Verify that "x is of type A" was inferred
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
