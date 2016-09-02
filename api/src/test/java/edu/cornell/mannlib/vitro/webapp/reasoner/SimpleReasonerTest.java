/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;


public class SimpleReasonerTest extends SimpleReasonerTBoxHelper {
	
	private static final String mostSpecificTypePropertyURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";
	long delay = 50;
  
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.OFF);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.OFF);
	}
	
    @Test
    public void addABoxTypeAssertion1Test(){
        addABoxTypeAssertion1(true);
    }

    @Test
    public void addABoxTypeAssertion1NoSameAsTest(){
        addABoxTypeAssertion1(false);
    }
	/*
	* Test that when an individual is asserted to be of a type,
	* its asserted type is not added to the inference graph
	*/
	public void addABoxTypeAssertion1( boolean sameAsEnabled ){
			
		// Create a Tbox with a simple class hierarchy. B is a subclass of A.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
	    classA.addSubClass(classB);
	            
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
        simpleReasoner.setSameAsEnabled( sameAsEnabled );
		aBox.register(simpleReasoner);
		
        // Individual x 
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		
        // add a statement to the ABox that individual x is of type (i.e. is an instance of) B.		
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		aBox.add(xisb);		

		// Verify that "x is of type B" was not inferred	
		Assert.assertFalse(inf.contains(xisb));	
	}

    @Test
    public void addABoxTypeAssertion2Test(){
        addABoxTypeAssertion2(true);
    }
    @Test
    public void addABoxTypeAssertion2NoSameAs(){
        addABoxTypeAssertion2(false);
    }
	/*
	* Test that when an individual is asserted have a type,
	* that inferences are materialized that it has the types
	* of its superclasses
	*/
	public void addABoxTypeAssertion2(boolean enableSameAs){
	
		// Create a Tbox with a simple class hierarchy. D and E are subclasses
		// of C. B and C are subclasses of A. 
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/D", "class D"); 
		OntClass classE = createClass(tBox, "http://test.vivo/E", "class E"); 
		addSubclass(classA, classB);
		addSubclass(classA, classC);
		addSubclass(classC, classD);
		addSubclass(classC, classE);
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
		aBox.register( sr );
		
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

    @Test
    public void addABoxTypeAssertion3Test() throws InterruptedException{
        addABoxTypeAssertion3(true);
    }
    @Test
    public void addABoxTypeAssertion3NoSameAs() throws InterruptedException{
        addABoxTypeAssertion3(false);
    }
	/*
	* Test inference based on class equivalence
	*/ 
	public void addABoxTypeAssertion3(boolean enableSameAs) throws InterruptedException {

		// Add classes A, B and C to the TBox
	    // A is equivalent to B
		// C is a subclass of A
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C");
		addSubclass(classA, classC);
		setEquivalent(classA, classB);

		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

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
	
    @Test
    public void addABoxTypeAssertion4Test()throws InterruptedException{
        addABoxTypeAssertion4(true);
    }
    @Test
    public void addABoxTypeAssertion4NoSameAs()throws InterruptedException{
        addABoxTypeAssertion4(false);
    }
	/*
	 * Test inference based on class equivalence
	 */ 
	public void addABoxTypeAssertion4(boolean enableSameAs) throws InterruptedException {
				
		// Add classes A and B to the TBox
		// A is equivalent to B
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		setEquivalent(classA, classB);
		
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);
		
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
	

    @Test
    public void addABoxTypeAssertion5Test()throws InterruptedException{
        addABoxTypeAssertion5(true);
    }
    @Test
    public void addABoxTypeAssertion5NoSameAs()throws InterruptedException{
        addABoxTypeAssertion5(false);
    }
	/*
	 * Test inference based on class equivalence
	 */
	public void addABoxTypeAssertion5(boolean enableSameAs) throws InterruptedException {

		// Add classes classes A and B to the TBox
		// A is equivalent to B
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		setEquivalent(classA, classB);
	    
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

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

    @Test
    public void removeABoxTypeAssertion1Test() {
        removeABoxTypeAssertion1(true);
    }
    @Test
    public void removeABoxTypeAssertion1NoSameAs() {
        removeABoxTypeAssertion1(false);
    }
	/*
	* Test that when it is retracted that an individual is of a type,
	* that the inferences that it is of the type of all superclasses
	* of the retracted type are retracted from the inference graph.
	* However, any assertions that are otherwise entailed (by the
	* TBox, ABox and inference graph minus the retracted type statement)
	* should not be retracted.
	*/
	public void removeABoxTypeAssertion1(boolean enableSameAs){
	
		// Create a Tbox with a simple class hierarchy. C is a subclass of B
		// and B is a subclass of A.
		
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C");
		addSubclass(classB, classC);
		addSubclass(classA, classB);
	    
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
		aBox.register( sr );
		
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
	
    @Test
    public void addTBoxSubClassAssertion1Test()throws InterruptedException{
        addTBoxSubClassAssertion1(true);
    }
    @Test
    public void addTBoxSubClassAssertion1NoSameAs()throws InterruptedException{
        addTBoxSubClassAssertion1(false);
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
	public void addTBoxSubClassAssertion1(boolean enableSameAs) throws InterruptedException {
		// Create the TBox and add classes A, B, C and D
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/D", "class D"); 
		
		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

        // Add a statement that individual x is of type C to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		
	    
        // Add a statement that C is a subclass of A to the TBox	
	    addSubclass(classA, classC);
		
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

    @Test
    public void addTBoxSubClassAssertion2Test()throws InterruptedException{
        addTBoxSubClassAssertion2(true);
    }
    @Test
    public void addTBoxSubClassAssertion2NoSameAs()throws InterruptedException{
        addTBoxSubClassAssertion2(false);
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
	public void addTBoxSubClassAssertion2(boolean enableSameAs) throws InterruptedException {
				
		// Create the TBox and add classes A, B, C and D.
		// D is a subclass of C.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/D", "class D");
		addSubclass(classC, classD);
		
		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);
		
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual x is of type D to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classD);		
	    
        // Add a statement that C is a subclass of A to the TBox	
		addSubclass(classA, classC);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	
    @Test
    public void removeTBoxSubClassAssertion1Test()throws InterruptedException{
        removeTBoxSubClassAssertion1(true);
    }
    @Test
    public void removeTBoxSubClassAssertion1NoSameAs()throws InterruptedException{
        removeTBoxSubClassAssertion1(false);
    }
	/*
	 * Test the removal of a subClassOf statement from 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox graph and the
	 * inference graph. 
	 * 
	 */
	public void removeTBoxSubClassAssertion1(boolean enableSameAs) throws InterruptedException {
		// Create the TBox and add classes A, B, C, D, E, F, G and H.
		// B, C and D are subclasses of A.
		// E is a subclass of B.
		// F and G are subclasses of C.
		// H is a subclass of D.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/D", "class D");
		OntClass classE = createClass(tBox, "http://test.vivo/E", "class E");
		OntClass classF = createClass(tBox, "http://test.vivo/F", "class F");
		OntClass classG = createClass(tBox, "http://test.vivo/G", "class G");
		OntClass classH = createClass(tBox, "http://test.vivo/H", "class H");
		addSubclass(classA, classB);
		addSubclass(classA, classC);
		addSubclass(classA, classD);
		addSubclass(classB, classE);
		addSubclass(classC, classF);
		addSubclass(classC, classG);
		addSubclass(classD, classH);
		
		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual x is of type E to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classE);		
	    
		// Remove the statement that B is a subclass of A from the TBox
		removeSubclass(classA, classB);
		
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
	
    @Ignore(" needs TBoxReasoner infrastructure which is not in this suite.")
    @Test
    public void bcdTest()throws InterruptedException{
        bcd(true);
    }

    @Ignore(" needs TBoxReasoner infrastructure which is not in this suite.")
    @Test
    public void bcdNoSameAsTest()throws InterruptedException{
        bcd(false);
    }
	/*
	 * Test the removal of a subClassOf statement from 
	 * the TBox. The instance data that is the basis
	 * for the inference is in the ABox graph and the
	 * inference graph. 
	 * 
	 */
	// this test would need TBoxReasoner infrastructure, which we're not
	// testing in this suite. The reason it doesn't work as it is because
	// the SimpleReasonerTBoxListener is not listening to the tBox inference
	// model as the TBoxReasoner is updating it. I could simulate it by adding to the
	// tBox assertions what we can count on the TBoxReasoner to infer.
	public void bcd(boolean enableSameAs) throws InterruptedException {
		OntModel tBox = createTBoxModel();

		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
        simpleReasoner.setSameAsEnabled( enableSameAs );
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);
		
		// Add classes LivingThing, Flora, Brassica to the TBox
		// Brassica is a subClass of Flora and Flora is a subclass of Brassica
		OntClass LivingThing = createClass(tBox, "http://test.vivo/LivingThing", "Living Thing");
		OntClass Flora = createClass(tBox, "http://test.vivo/Flora", "Flora");
		OntClass Brassica = createClass(tBox, "http://test.vivo/Brassica", "Brassica");
		addSubclass(LivingThing, Flora);
		addSubclass(Flora, Brassica);
	    
	    tBox.rebind();
	    tBox.prepare();
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
        // Add a statement that individual kale is of type Brassica to the ABox
		Resource kale = aBox.createResource("http://test.vivo/kale");
		aBox.add(kale, RDF.type, Brassica);		
	    
		// Remove the statement that Brassica is a subclass of Flora from the TBox
		Flora.removeSubClass(Brassica);

	    tBox.rebind();
	    tBox.prepare();
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
	
    @Test
    public void mstTest1Test()throws InterruptedException{
        mstTest1(true);
    }
    @Test
    public void mstTest1NoSameAs()throws InterruptedException{
        mstTest1(false);
    }
	/*
     * Test computation of mostSpecificType annotations in response
     * to an added/removed ABox type assertion.
	 */
	public void mstTest1(boolean enableSameAs) throws InterruptedException {
		// Set up the Tbox with a class hierarchy. C is a subclass of A
		// and Y. D and E are subclasses of C. B is a subclass of D.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/D", "class D");
		OntClass classE = createClass(tBox, "http://test.vivo/E", "class E");
		OntClass classY = createClass(tBox, "http://test.vivo/Y", "class Y");
		addSubclass(classA, classC);
		addSubclass(classY, classC);
		addSubclass(classC, classD);
		addSubclass(classC, classE);
		addSubclass(classD, classB);

		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);
		
		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);
		
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
	

    @Test
    public void mstTest2Test()throws InterruptedException{
        mstTest2(true);
    }
    @Test
    public void mstTest2NoSameAs()throws InterruptedException{
        mstTest2(false);
    }
	/*
     * Test computation of mostSpecificType annotations in response
     * to an added ABox type assertion.
	 */
	public void mstTest2(boolean enableSameAs) throws InterruptedException {
		// Set up the Tbox with a class hierarchy. A, B, and C are all equivalent.
		// This implies that they are all subclasses of each other.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		setEquivalent(classA, classB);
		setEquivalent(classB, classC);

		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);
		
		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);
		
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
	
    @Test
    public void mstTest3Test()throws InterruptedException{
        mstTest3(true);
    }
    @Test
    public void mstTest3NoSameAs()throws InterruptedException{
        mstTest3(false);
    }
	/*
     * Test computation of mostSpecificType annotations in response
     * to an added/removed TBox assertions.
	 */
	public void mstTest3(boolean enableSameAs) throws InterruptedException {
		// Set up the Tbox with classes A, B, C, D, E, F and G
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/D", "class D"); 
		OntClass classE = createClass(tBox, "http://test.vivo/E", "class E"); 
		OntClass classF = createClass(tBox, "http://test.vivo/F", "class F"); 
		OntClass classG = createClass(tBox, "http://test.vivo/G", "class G"); 

		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner sr = new SimpleReasoner(tBox, aBox, inf);
        sr.setSameAsEnabled( enableSameAs );
        SimpleReasoner simpleReasoner =  sr ;
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);
		
		OntClass OWL_THING = tBox.createClass(OWL.Thing.getURI());
		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);
		
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
		addSubclass(classA, classB);
		addSubclass(classA, classC);
		addSubclass(classA, classD);
		addSubclass(classC, classE);
		addSubclass(classD, classF);
		addSubclass(classD, classG);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		Assert.assertFalse(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertTrue(inf.contains(ind_y, mostSpecificType, ResourceFactory.createResource(classF.getURI())));
	    
		// If F is removed as a subclass of D, then D should once again be a most specific type
		// for y.
		removeSubclass(classD, classF);
		
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
