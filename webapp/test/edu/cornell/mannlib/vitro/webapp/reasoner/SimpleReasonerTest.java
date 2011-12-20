/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;


import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;


public class SimpleReasonerTest extends AbstractTestClass {
	
	private Resource objectProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#ObjectProperty");
	private static final String mostSpecificTypePropertyURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";
	long delay = 50;
  
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.OFF);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.OFF);
	}
	
	/*
	* Test that when an individual is asserted to be of a type,
	* its asserted type is not added to the inference graph
	*/
	@Test
	public void addABoxTypeAssertion1(){
			
		// Create a Tbox with a simple class hierarchy. B is a subclass of A.
		// Pellet will compute TBox inferences
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addSubClass(classB);
	            
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individual x 
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		
        // add a statement to the ABox that individual x is of type (i.e. is an instance of) B.		
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		aBox.add(xisb);		

		// Verify that "x is of type B" was not inferred	
		Assert.assertFalse(inf.contains(xisb));	
	}

	/*
	* Test that when an individual is asserted have a type,
	* that inferences are materialized that it has the types
	* of its superclasses
	*/
	@Test
	public void addABoxTypeAssertion2(){
	
		// Create a Tbox with a simple class hierarchy. D and E are subclasses
		// of C. B and C are subclasses of A. Pellet will compute TBox inferences.
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classE.setLabel("class E", "en-US");
	    
	    classC.addSubClass(classD);
	    classC.addSubClass(classE);
	    
        classA.addSubClass(classB);
        classA.addSubClass(classC);
        
        // this is the model to receive inferences
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

	/*
	* Test inference based on class equivalence
	*/ 
	@Test
	public void addABoxTypeAssertion3() throws InterruptedException {
				
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

		// Add classes A, B and C to the TBox
	    // A is equivalent to B
		// C is a subclass of A
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    classA.addEquivalentClass(classB);
	    classA.addSubClass(classC);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	       
        // Add a statement that individual x is of type C to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));		
		
		// Verify that "x is of type B" was inferred
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertTrue(inf.contains(xisb));	
		
		simpleReasonerTBoxListener.setStopRequested();;
	}
	
	/*
	 * Test inference based on class equivalence
	 */ 
	@Test
	public void addABoxTypeAssertion4() throws InterruptedException {
				
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
		
		// Add classes A and B to the TBox
	    // A is equivalent to B
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addEquivalentClass(classB);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual x is of type B to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classB);		
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	

		simpleReasonerTBoxListener.setStopRequested();
	}
	

	/*
	 * Test inference based on class equivalence
	 */
	@Test
	public void addABoxTypeAssertion5() throws InterruptedException {
				
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

		// Add classes classes A and B to the TBox
	    // A is equivalent to B
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addEquivalentClass(classB);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual x is of type B to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classB);		
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
		
		// Remove the statement that x is of type B from the ABox
		aBox.remove(ind_x, RDF.type, classB);
		
		// Verify that "x is of type A" was removed from the inference graph
		Assert.assertFalse(inf.contains(xisa));	

		simpleReasonerTBoxListener.setStopRequested();
	}

	/*
	* Test that when it is retracted that an individual is of a type,
	* that the inferences that it is of the type of all superclasses
	* of the retracted type are retracted from the inference graph.
	* However, any assertions that are otherwise entailed (by the
	* TBox, ABox and inference graph minus the retracted type statement)
	* should not be retracted.
	*/
	@Test
	public void removeABoxTypeAssertion1(){
	
		// Create a Tbox with a simple class hierarchy. C is a subclass of B
		// and B is a subclass of A. Pellet will compute TBox inferences.
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");
	    
	    classB.addSubClass(classC);
	    classA.addSubClass(classB);
        
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // add a statement to the ABox that individual x is of type C.
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		

        // add a statement to the ABox that individual x is of type B.
		aBox.add(ind_x, RDF.type, classB);		

		// remove the statement that individual x is of type C
		aBox.remove(ind_x, RDF.type, classC);
		
		// Verify that the inference graph contains the statement that x is of type A.
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
		
		// Hopefully the assertion that x is b got removed from
		// the inference graph
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertFalse(inf.contains(xisb));	

	}
	
	/*
	 * Test the addition of a subClassOf statement to 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox. The existing
	 * instance of the newly declared subclass should
	 * be inferred to have the type of the superclass. 
	 * There are also a few checks that the instance
	 * is not inferred to have the types of some other
	 * random classes.
	 * 
	 * Since the addition of an owl:equivalentClass
	 * statement is implemented as two calls to the
	 * method that handles the addition of an
	 * rdfs:subClassOf statement, this test serves
	 * as a test of equivalentClass statements also.
	 */
	@Test
	public void addTBoxSubClassAssertion1() throws InterruptedException {
				
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

		// Add classes classes A, B, C and D to the TBox
	
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    	   
        // Add a statement that individual x is of type C to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		
	    
        // Add a statement that C is a subclass of A to the TBox	
	    
	    classA.addSubClass(classC);
		
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));

		// Verify that "x is of type B" was not inferred
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertFalse(inf.contains(xisb));	

		// Verify that "x is of type D" was not inferred
		Statement xisd = ResourceFactory.createStatement(ind_x, RDF.type, classD);	
		Assert.assertFalse(inf.contains(xisd));	
		
		simpleReasonerTBoxListener.setStopRequested();
	}

	/*
	 * Test the addition of a subClassOf statement to 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox graph and the
	 * inference graph. The existing instance of the
	 * subclass of the newly added subclass should
	 * be inferred to have the type of the superclass
	 * of the newly added subclass. 
	 * 
	 * Since the addition of an owl:equivalentClass
	 * statement is implemented as two calls to the
	 * method that handles the addition of an
	 * rdfs:subClassOf statement, this test serves
	 * as some test of equivalentClass statements also.
	 * 
	 */
	@Test
	public void addTBoxSubClassAssertion2() throws InterruptedException {
				
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
		
		// Add classes classes A, B, C and D to the TBox
	    // D is a subclass of C
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    	   
	    classC.addSubClass(classD);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual x is of type D to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classD);		
	    
        // Add a statement that C is a subclass of A to the TBox	
	    classA.addSubClass(classC);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	
	/*
	 * Test the removal of a subClassOf statement from 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox graph and the
	 * inference graph. 
	 * 
	 */
	@Test
	public void removeTBoxSubClassAssertion1() throws InterruptedException {
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

		// Add classes A, B, C, D, E, F, G and H to the TBox.
		// B, C and D are subclasses of A.
		// E is a subclass of B.
		// F and G are subclasses of C.
		// H is a subclass of D.
	
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classE.setLabel("class E", "en-US");
	    
	    OntClass classF = tBox.createClass("http://test.vivo/F");
	    classF.setLabel("class F", "en-US");

	    OntClass classG = tBox.createClass("http://test.vivo/G");
	    classG.setLabel("class G", "en-US");

	    OntClass classH = tBox.createClass("http://test.vivo/H");
	    classH.setLabel("class H", "en-US");

	    classA.addSubClass(classB);
	    classA.addSubClass(classC);
	    classA.addSubClass(classD);
	    classB.addSubClass(classE);
	    classC.addSubClass(classF);
	    classC.addSubClass(classG);
	    classD.addSubClass(classH);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual x is of type E to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classE);		
	    
		// Remove the statement that B is a subclass of A from the TBox
		classA.removeSubClass(classB);
		
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
		
		// Verify that "x is of type A" is not in the inference graph
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertFalse(inf.contains(xisa));
		
		// Verify that "x is of type B" is in the inference graph
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertTrue(inf.contains(xisb));	

        // Add statements that individual y is of types F and H to the ABox
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_y, RDF.type, classF);	
		aBox.add(ind_y, RDF.type, classH);
		
		// Remove the statement that C is a subclass of A from the TBox
		classA.removeSubClass(classC);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
		
		// Verify that "y is of type A" is in the inference graph
		Statement yisa = ResourceFactory.createStatement(ind_y, RDF.type, classA);	
		Assert.assertTrue(inf.contains(yisa));
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	/*
	 * Test the removal of a subClassOf statement from 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox graph and the
	 * inference graph. 
	 * 
	 */
	//@Test  - enable this in 1.5 - will need to add in PelletListener infrastructure
	public void removeTBoxSubClassAssertion2() throws InterruptedException {
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
		
		// Add classes LivingThing, Flora, Brassica to the TBox
		// Brassica is a subClass of Flora and Flora is a subclass of Brassica
	
		OntClass LivingThing = tBox.createClass("http://test.vivo/LivingThing");
		LivingThing.setLabel("Living Thing", "en-US");

		OntClass Flora = tBox.createClass("http://test.vivo/Flora");
	    Flora.setLabel("Flora", "en-US");

		OntClass Brassica = tBox.createClass("http://test.vivo/Brassica");
	    Brassica.setLabel("Brassica", "en-US");

	    LivingThing.addSubClass(Flora);
	    Flora.addSubClass(Brassica);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual kale is of type Brassica to the ABox
		Resource kale = aBox.createResource("http://test.vivo/kale");
		aBox.add(kale, RDF.type, Brassica);		
	    
		// Remove the statement that Brassica is a subclass of Flora from the TBox
		Flora.removeSubClass(Brassica);
		
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
		
		// Verify that "kale is of type Flora" is not in the inference graph
		Statement kaleIsFlora = ResourceFactory.createStatement(kale, RDF.type, Flora);	
		Assert.assertFalse(inf.contains(kaleIsFlora));
		
		// Verify that "kale is of type LivingThing" is not in the inference graph
		Statement kaleIsLivingThing = ResourceFactory.createStatement(kale, RDF.type, LivingThing);	
		Assert.assertFalse(inf.contains(kaleIsLivingThing));
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	/*
	 * tests rdfs:subPropertyOf materialization for object properties.
	 */
	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void addABoxAssertion1() throws InterruptedException {
				
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

		// Add object properties propA and propB to the TBox.
	    // propB is a sub-property of propA.
		
		OntProperty propA = tBox.createOntProperty("http://test.vivo/propA");
        tBox.add(propA, RDF.type, objectProperty);
		propA.setLabel("propA", "en-US");
	    
		OntProperty propB = tBox.createOntProperty("http://test.vivo/propB");
        tBox.add(propB, RDF.type, objectProperty);
		propB.setLabel("propB", "en-US");
	    	   
	    propA.addSubProperty(propB);
	    
        // Add the assertion that x propB y to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_x, propB, ind_y);		
	    		
		// Verify that x propA y was inferred
		Statement xay = ResourceFactory.createStatement(ind_x, propA, ind_y);	
		Assert.assertTrue(inf.contains(xay));		
	}
	
	/*
	* Test that when a statement is asserted, that it not
	* added to the inference graph.
	*/
	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void addABoxAssertion2(){
			
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
		
		// Add object properties propA and propB to the TBox.
	    // propB is a sub-property of propA.
		
		OntProperty propA = tBox.createOntProperty("http://test.vivo/propA");
        tBox.add(propA, RDF.type, objectProperty);
		propA.setLabel("propA", "en-US");
	    
		OntProperty propB = tBox.createOntProperty("http://test.vivo/propB");
        tBox.add(propB, RDF.type, objectProperty);
		propB.setLabel("propB", "en-US");
	    	   
	    propA.addSubProperty(propB);
	    
        // Add the assertion that x propB y to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_x, propB, ind_y);		
	    		
		// Verify that x propA y was inferred
		Statement xby = ResourceFactory.createStatement(ind_x, propB, ind_y);	
		Assert.assertFalse(inf.contains(xby));		
	}
		
	/*
	* Test inference based on property equivalence
	*/ 
	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void addABoxAssertion4(){
				
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
		
		// Add properties A, B and C to the TBox
	    // A is equivalent to B
		// C is a subclass of A
		
		OntProperty propA = tBox.createOntProperty("http://test.vivo/A");
        tBox.add(propA, RDF.type, objectProperty);
	    propA.setLabel("property A", "en-US");

	    OntProperty propB = tBox.createOntProperty("http://test.vivo/B");
        tBox.add(propB, RDF.type, objectProperty);
	    propB.setLabel("property B", "en-US");

	    OntProperty propC = tBox.createOntProperty("http://test.vivo/C");
        tBox.add(propC, RDF.type, objectProperty);
	    propC.setLabel("property C", "en-US");

	    propA.addEquivalentProperty(propB);
	    propA.addSubProperty(propC);
	    
        // Add a statement that individual x is of type C to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		
		aBox.add(ind_x, propC, ind_y);		
	    
		// Verify that "x A y" was inferred
		Statement xAy = ResourceFactory.createStatement(ind_x, propA, ind_y);	
		Assert.assertTrue(inf.contains(xAy));		
		
		// Verify that "x B y" was inferred
		Statement xBy = ResourceFactory.createStatement(ind_x, propB, ind_y);	
		Assert.assertTrue(inf.contains(xBy));		
	}
	
	/*
	 * The sub-property is not of the same type as the super
	 * property so no inference should be materialized.
	 */

	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void addABoxAssertion5(){
				
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

		// Add object properties propA and propB to the TBox.
	    // propB is a sub-property of propA.
		
		OntProperty propA = tBox.createOntProperty("http://test.vivo/propA");
        tBox.add(propA, RDF.type, objectProperty);
		propA.setLabel("propA", "en-US");
	    
		OntProperty propB = tBox.createOntProperty("http://test.vivo/propB");
		Resource datatypeProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#DatatypeProperty");
        tBox.add(propB, RDF.type, datatypeProperty);
		propB.setLabel("propB", "en-US");
	    	   
	    propA.addSubProperty(propB);
	    
        // Add the assertion that x propB y to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_x, propB, ind_y);		
	    		
		// Verify that x propA y was not inferred
		Statement xay = ResourceFactory.createStatement(ind_x, propA, ind_y);	
		Assert.assertFalse(inf.contains(xay));		
	}
	
	/*
	 * Test inference based on property equivalence
	 */
	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void addABoxAssertion6() {
				
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
		
		// Add properties A and B to the TBox
	    // A is equivalent to B
		
		OntProperty propA = tBox.createOntProperty("http://test.vivo/A");
        tBox.add(propA, RDF.type, objectProperty);
	    propA.setLabel("property A", "en-US");

	    OntProperty propB = tBox.createOntProperty("http://test.vivo/B");
        tBox.add(propB, RDF.type, objectProperty);
	    propB.setLabel("property B", "en-US");

	    propA.addEquivalentProperty(propB);
	    
        // Add the statement x B y to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_x, propB, ind_y);		
	    
		// Verify that "x A y" was inferred
		Statement xAy = ResourceFactory.createStatement(ind_x, propA, ind_y);	
		Assert.assertTrue(inf.contains(xAy));	
		
		// Remove the statement that x B y from the ABox
		aBox.remove(ind_x, propB, ind_y);
		
		// Verify that "x is of type A" was removed from the inference graph
		Assert.assertFalse(inf.contains(xAy));	
	}

	
	/*
	 * Test the addition of a subPropertyOf statement to 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox. 
	 * 
	 * Since the addition of an owl:equivalentProperty
	 * statement is implemented as two calls to the
	 * method that handles the addition of an
	 * rdfs:subPropetyOf statement, this test serves
	 * as a test of equivalentProperty assertions also.
	 */
	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void addTBoxSubPropertyAssertion1(){
				
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
		
		// Add properties A, B, C and D to the TBox	  
		OntProperty propA = tBox.createOntProperty("http://test.vivo/A");
        tBox.add(propA, RDF.type, objectProperty);
	    propA.setLabel("property A", "en-US");

	    OntProperty propB = tBox.createOntProperty("http://test.vivo/B");
        tBox.add(propB, RDF.type, objectProperty);
	    propB.setLabel("property B", "en-US");
	    
	    OntProperty propC = tBox.createOntProperty("http://test.vivo/C");
        tBox.add(propC, RDF.type, objectProperty);
	    propB.setLabel("property C", "en-US");
	    
	    OntProperty propD = tBox.createOntProperty("http://test.vivo/D");
        tBox.add(propD, RDF.type, objectProperty);
	    propB.setLabel("property D", "en-US");

	    // Add the statement "x C y" to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_x, propC, ind_y);		
	    
        // Add a statement that C is a sub-property of A to the TBox	
	    
	    propA.addSubProperty(propC);
		
		// Verify that "x A y" was inferred
		Statement xAy = ResourceFactory.createStatement(ind_x, propA, ind_y);	
		Assert.assertTrue(inf.contains(xAy));

		// Verify that "x B y" was not inferred
		Statement xBy = ResourceFactory.createStatement(ind_x, propB, ind_y);	
		Assert.assertFalse(inf.contains(xBy));	

		// Verify that "x D y" was not inferred
		Statement xDy = ResourceFactory.createStatement(ind_x, propD, ind_y);	
		Assert.assertFalse(inf.contains(xDy));	
		
	}
	
	/*
	 * Test the removal of a subPropertyOf statement from 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox graph and the
	 * inference graph. 
	 * 
	 * Since the addition of an owl:equivalentProperty
	 * statement is implemented as two calls to the
	 * method that handles the addition of an
	 * rdfs:subPropertyOf statement, this test serves
	 * as a test of equivalentProperty assertions also.
	 * 
	 */
	// @Test  uncomment when sub/equiv property inferencing is enabled. sjm222 5/13/2011
	public void removeTBoxSubPropertyAssertion1(){
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
		
		// Add properties A, B, C, D, E, F, G and H to the TBox.
		// B, C and D are subproperties of A.
		// E is a subproperty of B.
		// F and G are subproperties of C.
		// H is a subproperty of D.
	
		OntProperty propA = tBox.createOntProperty("http://test.vivo/A");
        tBox.add(propA, RDF.type, objectProperty);
	    propA.setLabel("property A", "en-US");

	    OntProperty propB = tBox.createOntProperty("http://test.vivo/B");
        tBox.add(propB, RDF.type, objectProperty);
	    propB.setLabel("property B", "en-US");
	    
	    OntProperty propC = tBox.createOntProperty("http://test.vivo/C");
        tBox.add(propC, RDF.type, objectProperty);
	    propB.setLabel("property C", "en-US");
	    
	    OntProperty propD = tBox.createOntProperty("http://test.vivo/D");
        tBox.add(propD, RDF.type, objectProperty);
	    propB.setLabel("property D", "en-US");

	    OntProperty propE = tBox.createOntProperty("http://test.vivo/E");
        tBox.add(propE, RDF.type, objectProperty);
	    propB.setLabel("property E", "en-US");

	    OntProperty propF = tBox.createOntProperty("http://test.vivo/F");
        tBox.add(propF, RDF.type, objectProperty);
	    propB.setLabel("property F", "en-US");

	    OntProperty propG = tBox.createOntProperty("http://test.vivo/G");
        tBox.add(propG, RDF.type, objectProperty);
	    propB.setLabel("property G", "en-US");

	    OntProperty propH = tBox.createOntProperty("http://test.vivo/H");
        tBox.add(propH, RDF.type, objectProperty);
	    propB.setLabel("property H", "en-US");

	    propA.addSubProperty(propB);
	    propA.addSubProperty(propC);
	    propA.addSubProperty(propD);
	    propB.addSubProperty(propE);
	    propC.addSubProperty(propF);
	    propC.addSubProperty(propG);
	    propD.addSubProperty(propH);
	    
        // Add the statement "x E y" to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_x, propE, ind_y);		
	    
		// Remove the statement that B is a subproperty of A from the TBox
		propA.removeSubProperty(propB);
		
		// Verify that "x A y" is not in the inference graph
		Statement xAy = ResourceFactory.createStatement(ind_x, propA, ind_y);	
		Assert.assertFalse(inf.contains(xAy));
		
		// Verify that "x B y" is in the inference graph
		Statement xBy = ResourceFactory.createStatement(ind_x, propB, ind_y);	
		Assert.assertTrue(inf.contains(xBy));	

        // Add statements "w F z" and "w H z" to the ABox
		Resource ind_z = aBox.createResource("http://test.vivo/z");
		Resource ind_w = aBox.createResource("http://test.vivo/w");
		aBox.add(ind_w, propF, ind_z);	
		aBox.add(ind_w, propH, ind_z);
		
		// Remove the statement that C is a subproperty of A from the TBox
		propA.removeSubProperty(propC);

		// Verify that "w A z" is in the inference graph
		Statement wAz = ResourceFactory.createStatement(ind_w, propA, ind_z);	
		Assert.assertTrue(inf.contains(wAz));
	}
	
	

	/*
     * Test computation of mostSpecificType annotations in response
     * to an added/removed ABox type assertion.
	 */
	@Test
	public void mstTest1() throws InterruptedException {
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
		
		// Set up the Tbox with a class hierarchy. C is a subclass of A
		// and Y. D and E are subclasses C. B is a subclass of D.
		// Pellet will compute TBox inferences.

		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classE.setLabel("class E", "en-US");

	    OntClass classY = tBox.createClass("http://test.vivo/Y");
	    classE.setLabel("class Y", "en-US");

	    classY.addSubClass(classC);
	    classA.addSubClass(classC);	   
	    
	    classC.addSubClass(classD);
	    classC.addSubClass(classE);
	    
	    classD.addSubClass(classB);
  
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add the statement individual x is of type Y to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classD);		
		
		// Verify ind_x mostSpecificType annotation for D
		Assert.assertTrue(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classD.getURI())));	
		
		// Verify ind_x doesn't have a mostSpecificType annotation for 
		// A, Y, C, E or B.
		Assert.assertFalse(aBox.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classA.getURI())));	
		Assert.assertFalse(aBox.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classY.getURI())));
		Assert.assertFalse(aBox.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classC.getURI())));	
		Assert.assertFalse(aBox.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classE.getURI())));
		Assert.assertFalse(aBox.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classB.getURI())));	
		
		aBox.remove(ind_x, RDF.type, classD); // retract assertion that x is of type D.
		// Verify that D is not longer the most specific type
		Assert.assertFalse(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classD.getURI())));	
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	

	/*
     * Test computation of mostSpecificType annotations in response
     * to an added ABox type assertion.
	 */
	@Test
	public void mstTest2() throws InterruptedException {
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
		
		// Set up the Tbox with a class hierarchy. B is a subclass of A,
		// C is a subclass of B, and A is a subclass of C.
		// Pellet should infer these three classes to be equivalent.

		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    classA.addSubClass(classB);
	    classB.addSubClass(classC);	   
	    classC.addSubClass(classA);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
	    // Add the statement individual x is of type B to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classB);		
	    				
		// Verify ind_x mostSpecificType annotation for A, B and C
		Assert.assertTrue(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classA.getURI())));	
		Assert.assertTrue(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classB.getURI())));	
		Assert.assertTrue(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classC.getURI())));	
		
		simpleReasonerTBoxListener.setStopRequested();
	}
		
	/*
     * Test computation of mostSpecificType annotations in response
     * to an added/removed TBox assertions.
	 */
	@Test
	public void mstTest3() throws InterruptedException {
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
		
		OntClass OWL_THING = tBox.createClass(OWL.Thing.getURI());
		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);

		// Set up the Tbox with classes A, B, C, D,
		// E, F and G
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classE.setLabel("class E", "en-US");

	    OntClass classF = tBox.createClass("http://test.vivo/F");
	    classF.setLabel("class F", "en-US");

	    OntClass classG = tBox.createClass("http://test.vivo/G");
	    classE.setLabel("class G", "en-US");
		
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// add individuals x, y and z to the aBox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		
		aBox.add(ind_x, RDF.type, OWL_THING);	
		aBox.add(ind_y, RDF.type, classD);	
	
		Assert.assertTrue(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(OWL.Thing.getURI())));	
		Assert.assertTrue(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		
		aBox.add(ind_x, RDF.type, classC);
		aBox.add(ind_y, RDF.type, classF);
		
		Assert.assertFalse(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(OWL.Thing.getURI())));	
		Assert.assertTrue(inf.contains(ind_x, mostSpecificType, ResourceFactory.createResource(classC.getURI())));	
		Assert.assertTrue(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertTrue(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classF.getURI())));
		
		// Set up a class hierarchy. 
		// Pellet will compute TBox inferences.
		
	    classA.addSubClass(classB);
	    classA.addSubClass(classC);
	    classA.addSubClass(classD);
	   
	    classC.addSubClass(classE);
	    
	    classD.addSubClass(classF);
	    classD.addSubClass(classG);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		Assert.assertFalse(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertTrue(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classF.getURI())));
	    
		// If F is removed as a subclass of D, then D should once again be a most specific type
		// for y.
		classD.removeSubClass(classF);
		
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
		
		Assert.assertTrue(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classD.getURI())));	
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
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