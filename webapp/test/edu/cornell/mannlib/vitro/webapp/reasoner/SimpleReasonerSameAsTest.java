/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

public class SimpleReasonerSameAsTest extends AbstractTestClass {
	
	long delay = 50;
  
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.OFF);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.OFF);
	}
	
	/*
	* basic scenario of adding an abox sameAs assertion 
	//*/
	@Test
	public void addSameAsABoxAssertion1() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createObjectProperty("http://test.vivo/P");		
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createObjectProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	            
		OntProperty S = tBox.createDatatypeProperty("http://test.vivo/");
		S.setLabel("property S", "en-US");
		
		OntProperty T = tBox.createDatatypeProperty("http://test.vivo/");
		T.setLabel("property T", "en-US");
		
		Literal literal1 = tBox.createLiteral("Literal value 1");
		Literal literal2 = tBox.createLiteral("Literal value 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
		aBox.add(a,OWL.sameAs,b);

		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,P,c));
		Assert.assertTrue(inf.contains(b,S,literal1));
		Assert.assertTrue(inf.contains(a,Q,d));
		Assert.assertTrue(inf.contains(a,T,literal2));

		Assert.assertFalse(aBox.contains(b,OWL.sameAs,a));
		Assert.assertFalse(aBox.contains(b,P,c));
		Assert.assertFalse(aBox.contains(b,S,literal1));
		Assert.assertFalse(aBox.contains(a,Q,d));
		Assert.assertFalse(aBox.contains(a,T,literal2));
	}
			
	/*
	* basic scenario of removing an abox sameAs assertion 
	*/
	@Test
	public void removeSameAsABoxAssertion1() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createObjectProperty("http://test.vivo/P");		
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createObjectProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	            
		OntProperty S = tBox.createDatatypeProperty("http://test.vivo/");
		S.setLabel("property S", "en-US");
		
		OntProperty T = tBox.createDatatypeProperty("http://test.vivo/");
		T.setLabel("property T", "en-US");
		
		Literal literal1 = tBox.createLiteral("Literal value 1");
		Literal literal2 = tBox.createLiteral("Literal value 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
		aBox.add(a,OWL.sameAs,b);
		
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,P,c));
		Assert.assertTrue(inf.contains(b,S,literal1));
		Assert.assertTrue(inf.contains(a,Q,d));
		Assert.assertTrue(inf.contains(a,T,literal2));

		aBox.remove(a,OWL.sameAs,b);

		Assert.assertFalse(inf.contains(b,OWL.sameAs,a));
		Assert.assertFalse(inf.contains(b,P,c));
		Assert.assertFalse(inf.contains(b,S,literal1));
		Assert.assertFalse(inf.contains(a,Q,d));
		Assert.assertFalse(inf.contains(a,T,literal2));
	}
	
	/*
	* basic scenario of adding an abox assertion for
	* an individual is sameAs another. 
	*/
	@Test
	public void addABoxAssertion1() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createObjectProperty("http://test.vivo/P");		
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createObjectProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	            
		OntProperty S = tBox.createDatatypeProperty("http://test.vivo/");
		S.setLabel("property S", "en-US");
		
		OntProperty T = tBox.createDatatypeProperty("http://test.vivo/");
		T.setLabel("property T", "en-US");
		
		Literal literal1 = tBox.createLiteral("Literal value 1");
		Literal literal2 = tBox.createLiteral("Literal value 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,OWL.sameAs,b);

		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		
		aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
	
		Assert.assertTrue(inf.contains(b,P,c));
		Assert.assertTrue(inf.contains(b,S,literal1));
		Assert.assertTrue(inf.contains(a,Q,d));
		Assert.assertTrue(inf.contains(a,T,literal2));
	}
	
	/*
	* adding abox assertion for individuals that are sameAs
	* each other. 
	*/
	@Test
	public void addABoxAssertion2() {
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
			     
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addSubClass(classB);
		
		OntProperty desc = tBox.createDatatypeProperty("http://test.vivo/desc");
		desc.setLabel("property desc", "en-US");
		
		Literal desc1 = tBox.createLiteral("individual 1");
		Literal desc2 = tBox.createLiteral("individual 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a and b
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");

		aBox.add(a,desc,desc1);
		aBox.add(b,desc,desc2);
		aBox.add(a,OWL.sameAs,b);
		aBox.add(a, RDF.type, classB);
	
		Assert.assertTrue(inf.contains(a,desc,desc2));
		Assert.assertTrue(inf.contains(a,RDF.type,classA));
		Assert.assertTrue(inf.contains(b,desc,desc1));
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,RDF.type,classB));
		Assert.assertTrue(inf.contains(b,RDF.type,classA));
	}
	
	/*
	* basic scenario of removing an abox assertion for
	* an individual is sameAs another. 
	*/
	@Test
	public void removeABoxAssertion1() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createObjectProperty("http://test.vivo/P");		
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createObjectProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	            
		OntProperty S = tBox.createDatatypeProperty("http://test.vivo/");
		S.setLabel("property S", "en-US");
		
		OntProperty T = tBox.createDatatypeProperty("http://test.vivo/");
		T.setLabel("property T", "en-US");
		
		Literal literal1 = tBox.createLiteral("Literal value 1");
		Literal literal2 = tBox.createLiteral("Literal value 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
		aBox.add(a,OWL.sameAs,b);
		
		aBox.remove(a,P,c);
		aBox.remove(a,S,literal1);

		Assert.assertFalse(inf.contains(b,P,c));
		Assert.assertFalse(inf.contains(b,S,literal1));
	}
	
	/*
	 * adding an inverseOf assertion for individuals who are sameAs
	 * each other.
	 */
	@Test
	public void addTBoxInverseAssertion1() throws InterruptedException {
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

        // Individuals a and b
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		
        // abox statements			
		aBox.add(a,P,b);	
        aBox.add(a, OWL.sameAs,b);
		
        // Assert P and Q as inverses and wait for SimpleReasoner TBox
	    // thread to end
	    
	    Q.addInverseOf(P);
	    
	    tBox.rebind();
	    tBox.prepare();
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    	    
		// Verify inferences	
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,P,b));
		Assert.assertTrue(inf.contains(a,Q,a));
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	/*
	 * Basic scenario around recomputing the ABox inferences
	 */
	@Test
	public void recomputeABox1() throws InterruptedException {
				
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createObjectProperty("http://test.vivo/P");		
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createObjectProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	            
		OntProperty S = tBox.createDatatypeProperty("http://test.vivo/");
		S.setLabel("property S", "en-US");
		
		OntProperty T = tBox.createDatatypeProperty("http://test.vivo/");
		T.setLabel("property T", "en-US");
		
		Literal literal1 = tBox.createLiteral("Literal value 1");
		Literal literal2 = tBox.createLiteral("Literal value 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
		aBox.add(a,OWL.sameAs,b);
                
	    simpleReasoner.recompute();
	    
	    while (simpleReasoner.isRecomputing()) {
	    	Thread.sleep(delay);
	    }
 	    
		// Verify inferences	
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,P,c));
		Assert.assertTrue(inf.contains(b,S,literal1));
		Assert.assertTrue(inf.contains(a,Q,d));
		Assert.assertTrue(inf.contains(a,T,literal2));
	}
	
	//==================================== Utility methods ====================
	SimpleReasonerTBoxListener getTBoxListener(SimpleReasoner simpleReasoner) {
	    return new SimpleReasonerTBoxListener(simpleReasoner, new Exception().getStackTrace()[1].getMethodName());
	}
		
	// To help in debugging the unit test
	void printModel(Model model, String modelName) {
	    
		System.out.println("\nThe " + modelName + " model has " + model.size() + " statements:");
		System.out.println("---------------------------------------------------------------------");
		model.write(System.out);		
	}
	
	// To help in debugging the unit test
	void printModel(OntModel ontModel, String modelName) {
	    
		System.out.println("\nThe " + modelName + " model has " + ontModel.size() + " statements:");
		System.out.println("---------------------------------------------------------------------");
		ontModel.writeAll(System.out,"N3",null);
		
	}
}