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

public class SimpleReasonerInversePropertyTest extends AbstractTestClass {
	
	long delay = 50;
  
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.OFF);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.OFF);
		setLoggerLevel(ABoxRecomputer.class, Level.OFF);
	}
	
    @Test
    public void addABoxAssertion1Test(){
        addABoxAssertion1(true);        
    }

    @Test
    public void addABoxAssertion1NoSameAsTest(){
        addABoxAssertion1(false);
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
	public void addABoxAssertion1(boolean sameAs ) {

		// set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

	    P.addInverseOf(Q);
	            
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 

        SimpleReasoner sr = new SimpleReasoner(tBox,aBox,inf);
        sr.setSameAsEnabled( sameAs );

		aBox.register( sr );
		
        // add assertions to the abox and verify inferences
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,P,b);		
		Assert.assertTrue(inf.contains(b,Q,a));				
		aBox.add(c,Q,d);		
		Assert.assertTrue(inf.contains(d,P,c));	       
	}

    @Test
	public void addABoxAssertion2Test() {
        addABoxAssertion2(true);
    }

    @Test
	public void addABoxAssertion2NoSameAsTest() {
        addABoxAssertion2(false);
    }

	/*
	 * don't infer statements already in the abox
	 * (never infer because it's in the abox already)
	*/
	public void addABoxAssertion2(boolean sameAs ) {

		// set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	    P.addInverseOf(Q);
	    
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and add data (no inferencing happening yet)
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Individuals a, b, c and d
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		aBox.add(b,Q,a);
		
		// register SimpleReasoner
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
				
		Assert.assertFalse(inf.contains(b,Q,a));
		
		// add data and verify inferences
		aBox.add(a,P,b);
		Assert.assertFalse(inf.contains(b,Q,a));	
	}

    @Test
	public void addABoxAssertion3Test() {
        addABoxAssertion3(true);
    }
    @Test
    public void addABoxAssertion3NoSameAsTest(){
        addABoxAssertion3(false);
    }

	/*
	 * don't infer statements already in the abox
	 * (remove the inference when it is asserted)
	*/
	public void addABoxAssertion3(boolean sameAs) {
		
		// set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	    P.addInverseOf(Q);
	    
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create abox and register SimpleReasoner
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( sameAs );

		aBox.register( sr );
		
        // add statements to the abox and verify inferences
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
				
		aBox.add(a,P,b);
		Assert.assertTrue(inf.contains(b,Q,a));		
		aBox.add(b,Q,a); // this should cause the inference to be removed
		Assert.assertFalse(inf.contains(b,Q,a));	
	}

    @Test
	public void addABoxAssertion4Test() {
        addABoxAssertion4(true);
    }
    @Test
	public void addABoxAssertion4NoSameAsTest() {
        addABoxAssertion4(false);
    }

	/*
	* adding abox data where the property has an inverse and
	* and equivalent property.
	*/
	public void addABoxAssertion4( boolean sameAs ) {

		// set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty R = tBox.createOntProperty("http://test.vivo/R");
	    R.setLabel("property R", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

	    R.addEquivalentProperty(P);
	    P.addInverseOf(Q);
	            
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 

        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( sameAs );
		aBox.register( sr );
		
        // add abox statements and verify inferences
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");

		aBox.add(a,R,b);		
		Assert.assertTrue(inf.contains(b,Q,a));	
								
		aBox.add(c,Q,d);		
		Assert.assertTrue(inf.contains(d,P,c));	
		Assert.assertTrue(inf.contains(d,R,c));	
	}
	
    @Test
    public void removedABoxAssertion1Test(){
        removedABoxAssertion1(true);
    }

    @Test
    public void removedABoxAssertion1NoSameAsTest(){
        removedABoxAssertion1(false);
    }

	/*
	 *  basic scenarios around removing abox data
	 *  don't remove an inference if it's still 
	 *  entailed by something else in the abox.
	 */ 
	public void removedABoxAssertion1(boolean sameAs) {
		
		// set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
		OntProperty T = tBox.createOntProperty("http://test.vivo/T");
		Q.setLabel("property T", "en-US");
	    P.addInverseOf(Q);
	    P.addInverseOf(T);
	            
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( sameAs );
		aBox.register( sr );
		
        // add statements to the abox and verify inferences
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a,P,b);
		Assert.assertTrue(inf.contains(b,Q,a));	
		
        // d P c is inferred from c Q d and also from c T d			
		aBox.add(c,Q,d);		
		aBox.add(c,T,d);
		Assert.assertTrue(inf.contains(d,P,c));	
		
		aBox.remove(a,P,b);
		Assert.assertFalse(inf.contains(b,Q,a));
				
		aBox.remove(c,Q,d); 
		Assert.assertTrue(inf.contains(d,P,c)); // still inferred from c T d
	}
	
    @Test
    public void removedABoxAssertion2Test(){
        removedABoxAssertion2(true);
    }

    @Test
    public void removedABoxAssertion2NoSameAsTest(){
        removedABoxAssertion2(false);
    }

	/*
	 *  removing abox data with equivalent and inverse properties
	 *  don't remove inference if it's still inferred.
	 */ 
	public void removedABoxAssertion2(boolean sameAs) {
		
		// set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
		OntProperty T = tBox.createOntProperty("http://test.vivo/T");
		Q.setLabel("property T", "en-US");
	    P.addInverseOf(Q);
	    P.addEquivalentProperty(T);
       
	    // not clear what these will do
	    tBox.rebind();
	    tBox.prepare();
	    
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( sameAs );
		aBox.register( sr );
		
        // add abox data and verify inferences
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		
        // b Q a is inferred from a P b and  also from a T b.		
		aBox.add(a,P,b);
		aBox.add(a,T,b);
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertFalse(inf.contains(a,P,b));
		Assert.assertFalse(inf.contains(a,T,b));
		
		aBox.remove(a,P,b);
		Assert.assertTrue(inf.contains(b,Q,a));
	}
	
    @Test
    public void removedABoxAssertion3Test(){
        removedABoxAssertion3(true);
    }

    @Test
    public void removedABoxAssertion3NoSameAsTest(){
        removedABoxAssertion3(false);
    }

	/*
	 *  removing abox data with equivalent and inverse properties
	 */ 
	public void removedABoxAssertion3(boolean sameAs) {
		
		//set up the tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");
	    P.addInverseOf(Q);
	    
	    tBox.rebind(); // not sure what effect this has
	    
        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create the abox and add some data - no reasoning is happening yet
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		aBox.add(a,P,b);

		// register the SimpleReasoner
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( sameAs );
		aBox.register( sr );
		
		// add abox statements and verify inferences
		aBox.add(b,Q,a);
		Assert.assertFalse(inf.contains(b,Q,a));
		Assert.assertFalse(inf.contains(a,P,b)); // this could be inferred from b Q a, but
		                                         // it's already in the abox
		aBox.remove(a,P,b);
		Assert.assertTrue(inf.contains(a,P,b));  // now it gets added to inference model
		                                         // when it's removed from the abox
	}
	
    @Test
    public void addTBoxInverseAssertion1Test() throws InterruptedException {
        addTBoxInverseAssertion1(true);
    }

    @Test
    public void addTBoxInverseAssertion1NoSameAsTest() throws InterruptedException {
        addTBoxInverseAssertion1(false);
    }

	/*
	 * adding an inverseOf assertion to the tbox
	 */
	public void addTBoxInverseAssertion1(boolean sameAs) throws InterruptedException {
         
		// Set up the TBox. 
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntProperty P = tBox.createOntProperty("http://test.vivo/P");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/Q");
		Q.setLabel("property Q", "en-US");

        // this is the model to receive abox inferences
        Model inf = ModelFactory.createDefaultModel();

        // abox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
		// set up SimpleReasoner and register it with abox. register 
		// SimpleReasonerTBoxListener with the tbox.
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf );
        simpleReasoner.setSameAsEnabled( sameAs );        
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

        // add abox data
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
				
		aBox.add(a,P,b);	
	    aBox.add(c,P,d);
	    aBox.add(b,Q,a);
		
        // Assert P and Q as inverses and wait for
	    // SimpleReasonerTBoxListener thread to end
	    
	    Q.addInverseOf(P);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    	    
		// Verify inferences	
		Assert.assertTrue(inf.contains(d,Q,c));
		Assert.assertFalse(inf.contains(b,Q,a));
		Assert.assertFalse(inf.contains(a,P,b));
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
    @Test
    public void removeTBoxInverseAssertion1Test() throws InterruptedException {
        removeTBoxInverseAssertion1(true);
    }	

    @Test
    public void removeTBoxInverseAssertion1NoSameAsTest() throws InterruptedException {
        removeTBoxInverseAssertion1(false);
    }

	/*
	 * removing an inverseOf assertion from the tbox
	 */
	public void removeTBoxInverseAssertion1(boolean sameAs) throws InterruptedException {
				
		// set up the tbox.
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 

		OntProperty P = tBox.createOntProperty("http://test.vivo/propP");
	    P.setLabel("property P", "en-US");
		OntProperty Q = tBox.createOntProperty("http://test.vivo/propQ");
		Q.setLabel("property Q", "en-US");
	    Q.addInverseOf(P);
	    
		// this is the model to receive abox inferences
		Model inf = ModelFactory.createDefaultModel();

		// abox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        
		// set up SimpleReasoner and SimpleReasonerTBox listener,
		// register them with abox and tbox
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
        simpleReasoner.setSameAsEnabled( sameAs );                         
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);	    
		
        // add statements to the abox and verify inference
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
	    aBox.add(c,P,d);
		Assert.assertTrue(inf.contains(d,Q,c));
		
        // Remove P and Q inverse relationship and wait for
	    // SimpleReasoner TBox thread to end.
		
	    Q.removeInverseProperty(P);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
 	    
		// Verify inference has been removed	
		Assert.assertFalse(inf.contains(d,Q,c));
				
		simpleReasonerTBoxListener.setStopRequested();
	}
	
    @Test
    public void recomputeABox1Test() throws InterruptedException {
        recomputeABox1(true);
    }

    @Test
    public void recomputeABox1NoSameAsTest() throws InterruptedException {
        recomputeABox1(false);
    }

	/*
	 * Basic scenario around recomputing the ABox inferences
	 */
	public void recomputeABox1(boolean sameAs) throws InterruptedException {
				
        // set up tbox
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 

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

		// create abox and abox inf model and register simplereasoner
		// with abox.
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
        simpleReasoner.setSameAsEnabled( sameAs );
		aBox.register(simpleReasoner);
		
        // abox statements
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");

	    aBox.add(a,P,b);
        aBox.add(c,X,d);
	    
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertTrue(inf.contains(d,Y,c));				
                
        inf.remove(b,Q,a);
        inf.remove(d,Y,c);
        
        //recompute whole abox
	    simpleReasoner.recompute();
	    
	    while (simpleReasoner.isRecomputing()) {
	    	Thread.sleep(delay);
	    }
 	    
		// Verify inferences	
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertTrue(inf.contains(d,Y,c));				
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
