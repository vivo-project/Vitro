/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

public class SimpleReasonerPropertyTest extends AbstractTestClass {
	
	long delay = 50;
  
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.OFF);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.OFF);
	}
	
	/*
	* basic scenarios around adding abox data
	* 
	* Create a Tbox with property P inverseOf property Q.
    * Pellet will compute TBox inferences. Add a statement
    * a P b, and verify that b Q a is inferred.
    * Add a statement c Q d and verify that d Q c 
	* is inferred. 
	*/
	@Test
	public void addABoxAssertion1() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

	    P.addInverseOf(Q);
	            
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
		
		// b Q a is inferred from a P b
		aBox.add(a,P,b);		
		Assert.assertTrue(inf.contains(b,Q,a));	
		
        // d P c is inferred from c Q d.			
		aBox.add(c,Q,d);		
		Assert.assertTrue(inf.contains(d,P,c));	
	}
	
	/*
	 * don't infer if it's already in the abox
	*/
	@Test
	public void addABoxAssertion2() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	    P.addInverseOf(Q);
	    
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and add statement b Q a
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		aBox.add(b,Q,a);
		
		// register SimpleReasoner
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
				
		// b Q a is inferred from a P b, but it is already in the abox
		aBox.add(a,P,b);
		Assert.assertFalse(inf.contains(b,Q,a));	
	}

	/*
	 * don't infer if it's already in the abox
	*/
	@Test
	public void addABoxAssertion3() {
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	    P.addInverseOf(Q);
	    
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register SimpleReasoner
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
				
		// b Q a is inferred from a P b, but it is already in the abox
		aBox.add(a,P,b);
		aBox.add(b,Q,a);
		Assert.assertFalse(inf.contains(b,Q,a));	
	}
	
	/*
	* adding abox data where the property has an inverse and
	* and equivalent property.
	*/
	@Test
	public void addABoxAssertion4() {

		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty R = tBox.createOntProperty("http://test.vivo/R");
	    R.setLabel("property R", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

	    R.addEquivalentProperty(P);
	    P.addEquivalentProperty(R);
	    P.addInverseOf(Q);
	            
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");

        // b Q a is inferred from a R b.			
		aBox.add(a,R,b);		
		Assert.assertTrue(inf.contains(b,Q,a));	
					
        // d P c is inferred from c Q d.			
		aBox.add(c,Q,d);		
		Assert.assertTrue(inf.contains(d,P,c));	
		Assert.assertTrue(inf.contains(d,R,c));	
	}
	
	/*
	 *  basic scenarios around removing abox data
	 */ 
	@Test
	public void removedABoxAssertion1() {
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
		OntProperty T = tBox.createOntProperty("http://test.vivo/T");
		Q.setLabel("property T", "en-US");
	    P.addInverseOf(Q);
	    P.addInverseOf(T);
	            
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
		
        // b Q a is inferred from a P b.		
		aBox.add(a,P,b);
		Assert.assertTrue(inf.contains(b,Q,a));	
		
        // d P c is inferred from c Q d and from c T d			
		aBox.add(c,Q,d);		
		aBox.add(c,T,d);
		Assert.assertTrue(inf.contains(d,P,c));	
		
		aBox.remove(a,P,b);
		Assert.assertFalse(inf.contains(b,Q,a));
				
		aBox.remove(c,Q,d);
		Assert.assertTrue(inf.contains(d,P,c));
	}
	
	/*
	 *  removing abox data with equivalent and inverse properties
	 */ 
	@Test
	public void removedABoxAssertion2() {
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
		OntProperty T = tBox.createOntProperty("http://test.vivo/T");
		Q.setLabel("property T", "en-US");
	    P.addInverseOf(Q);
	    Q.addInverseOf(P);
	    P.addEquivalentProperty(T);
        T.addEquivalentProperty(P);
	    
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		
        // b Q a is inferred from a P b and a T b.		
		aBox.add(a,P,b);
		aBox.add(a,T,b);
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertFalse(inf.contains(a,P,b));
		
		aBox.remove(a,P,b);
		Assert.assertTrue(inf.contains(b,Q,a));
	}
	
	/*
	 *  removing abox data with equivalent and inverse properties
	 */ 
	@Test
	public void removedABoxAssertion3() {
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	    P.addInverseOf(Q);
	    
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		aBox.add(a,P,b);

		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
		aBox.add(b,Q,a);
		
		Assert.assertFalse(inf.contains(b,Q,a));
		Assert.assertFalse(inf.contains(a,P,b));
		
		aBox.remove(a,P,b);
		Assert.assertTrue(inf.contains(a,P,b));
	}
	
	/*
	 * Basic scenario around adding an inverseOf assertion to the
	 * TBox
	 */
	@Test
	public void addTBoxInverseAssertion1() throws InterruptedException {
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

        // set up TBox and Abox
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
        // abox statements			
		aBox.add(a,P,b);	
	    aBox.add(c,P,d);
	    aBox.add(b,Q,a);
		
        // Assert P and Q as inverses and wait for SimpleReasoner TBox
	    // thread to end
	    
	    Q.addInverseOf(P);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    	    
		// Verify inferences	
		Assert.assertTrue(inf.contains(d,Q,c));
		Assert.assertFalse(inf.contains(b,Q,a));
		
		simpleReasonerTBoxListener.setStopRequested();
	}
		
	/*
	 * Basic scenario around removing an inverseOf assertion to the
	 * TBox
	 */
	@Test
	public void removeTBoxInverseAssertion1() throws InterruptedException {
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

        // set up TBox and Abox
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/propP");
	    P.setLabel("property P", "en-US");

		OntProperty Q = tBox.createOntProperty("http://test.vivo/propQ");
		Q.setLabel("property Q", "en-US");

	    Q.addInverseOf(P);
	    
        // Individuals a, b, c and d
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
        // abox statements				
	    aBox.add(c,P,d);
		
		Assert.assertTrue(inf.contains(d,Q,c));
		
        // Remove P and Q inverse relationship and wait for
	    // SimpleReasoner TBox thread to end.
		
	    Q.removeInverseProperty(P);
	    
	    Thread.sleep(delay);
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
 	    
		// Verify inferences	
		Assert.assertFalse(inf.contains(d,Q,c));
				
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	/*
	 * Basic scenario around recomputing the ABox inferences
	 */
	@Test
	public void recomputeABox1() throws InterruptedException {
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

        // set up TBox and Abox
		OntProperty P = tBox.createOntProperty("http://test.vivo/propP");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/propQ");
		Q.setLabel("property Q", "en-US");
	    Q.addInverseOf(P);
	    
		OntProperty X = tBox.createOntProperty("http://test.vivo/propX");
	    P.setLabel("property X", "en-US");
		OntProperty Y = tBox.createOntProperty("http://test.vivo/propY");
		Q.setLabel("property Y", "en-US");
		X.addInverseOf(Y);
		
        Thread.sleep(delay*3);

        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");

        // abox statements				
	    aBox.add(a,P,b);
        aBox.add(c,X,d);
	    
	    simpleReasoner.recompute();
	    
	    Thread.sleep(delay);
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty() || simpleReasoner.isRecomputing()) {
	    	Thread.sleep(delay);
	    }
 	    
		// Verify inferences	
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertTrue(inf.contains(d,Y,c));				
		
		simpleReasonerTBoxListener.setStopRequested();
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