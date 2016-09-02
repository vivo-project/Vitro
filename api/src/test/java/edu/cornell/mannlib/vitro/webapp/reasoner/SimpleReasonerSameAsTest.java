/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

public class SimpleReasonerSameAsTest extends SimpleReasonerTBoxHelper {
	
	long delay = 50;
	private static final String mostSpecificTypePropertyURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";
	
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.OFF);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.OFF);
		setLoggerLevel(ABoxRecomputer.class, Level.OFF);
	}
	
	/*
	* basic scenario of adding an abox sameAs assertion 
	//*/
	@Test
	public void addSameAsABoxAssertion1() {
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");
		OntProperty S = createObjectProperty(tBox, "http://test.vivo/S", "property S");
		OntProperty T = createObjectProperty(tBox, "http://test.vivo/T", "property T");
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
        
        //run same test with sameAs = false
        inf = ModelFactory.createDefaultModel();
        aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner sres = new SimpleReasoner(tBox,aBox,inf);
        sres.setSameAsEnabled( false );
		aBox.register(sres);
		
		a = aBox.createResource("http://test.vivo/a");
		b = aBox.createResource("http://test.vivo/b");
		c = aBox.createResource("http://test.vivo/c");
		d = aBox.createResource("http://test.vivo/d");
		
        aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
		aBox.add(a,OWL.sameAs,b);

        //these are now false since sameAs is off
		Assert.assertFalse(inf.contains(b,OWL.sameAs,a));
		Assert.assertFalse(inf.contains(b,P,c));
		Assert.assertFalse(inf.contains(b,S,literal1));
		Assert.assertFalse(inf.contains(a,Q,d));
		Assert.assertFalse(inf.contains(a,T,literal2));
        //these still shouldn't be in the abox
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
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");
		OntProperty S = createObjectProperty(tBox, "http://test.vivo/S", "property S");
		OntProperty T = createObjectProperty(tBox, "http://test.vivo/T", "property T");
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
	* adding abox assertion for individual in sameAs chain.
	*/
	@Test
	public void addABoxAssertion1() {
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");
		OntProperty S = createObjectProperty(tBox, "http://test.vivo/S", "property S");
		OntProperty T = createObjectProperty(tBox, "http://test.vivo/T", "property T");
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
		Resource e = aBox.createResource("http://test.vivo/e");
		Resource f = aBox.createResource("http://test.vivo/f");
		
		aBox.add(a,OWL.sameAs,b);
		aBox.add(b,OWL.sameAs,e);
		aBox.add(e,OWL.sameAs,f);

		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(e,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(e,OWL.sameAs,b));
		Assert.assertTrue(inf.contains(a,OWL.sameAs,e));
		Assert.assertTrue(inf.contains(f,OWL.sameAs,e));
		Assert.assertTrue(inf.contains(f,OWL.sameAs,b));
		Assert.assertTrue(inf.contains(f,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,OWL.sameAs,f));
		Assert.assertTrue(inf.contains(a,OWL.sameAs,f));

		
		aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
	
		Assert.assertTrue(inf.contains(b,P,c));
		Assert.assertTrue(inf.contains(b,S,literal1));
		Assert.assertTrue(inf.contains(a,Q,d));
		Assert.assertTrue(inf.contains(a,T,literal2));
		Assert.assertTrue(inf.contains(e,P,c));
		Assert.assertTrue(inf.contains(e,S,literal1));
		Assert.assertTrue(inf.contains(e,Q,d));
		Assert.assertTrue(inf.contains(e,T,literal2));
		Assert.assertTrue(inf.contains(f,P,c));
		Assert.assertTrue(inf.contains(f,S,literal1));
		Assert.assertTrue(inf.contains(f,Q,d));
		Assert.assertTrue(inf.contains(f,T,literal2));
		
		aBox.remove(b,OWL.sameAs,e);

		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertFalse(inf.contains(e,OWL.sameAs,a));
		Assert.assertFalse(inf.contains(e,OWL.sameAs,b));
		Assert.assertFalse(inf.contains(a,OWL.sameAs,e));
		Assert.assertTrue(inf.contains(f,OWL.sameAs,e));
		Assert.assertFalse(inf.contains(f,OWL.sameAs,b));
		Assert.assertFalse(inf.contains(f,OWL.sameAs,a));
		Assert.assertFalse(inf.contains(b,OWL.sameAs,f));
		Assert.assertFalse(inf.contains(a,OWL.sameAs,f));

		Assert.assertTrue(inf.contains(b,P,c));
		Assert.assertTrue(inf.contains(b,S,literal1));
		Assert.assertTrue(inf.contains(a,Q,d));
		Assert.assertTrue(inf.contains(a,T,literal2));
		Assert.assertFalse(inf.contains(e,P,c));
		Assert.assertFalse(inf.contains(e,S,literal1));
		Assert.assertFalse(inf.contains(e,Q,d));
		Assert.assertFalse(inf.contains(e,T,literal2));
		Assert.assertFalse(inf.contains(f,P,c));
		Assert.assertFalse(inf.contains(f,S,literal1));
		Assert.assertFalse(inf.contains(f,Q,d));
		Assert.assertFalse(inf.contains(f,T,literal2));		
	}

    /**
     * test of enableSameAs( false )
     */
	@Test
	public void disabledSameAs() {
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");
		OntProperty S = createObjectProperty(tBox, "http://test.vivo/S", "property S");
		OntProperty T = createObjectProperty(tBox, "http://test.vivo/T", "property T");
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
        
        //run same test with sameAs = false
        inf = ModelFactory.createDefaultModel();
        aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        SimpleReasoner sres = new SimpleReasoner(tBox,aBox,inf);
        sres.setSameAsEnabled( false );
		aBox.register(sres);
		
		a = aBox.createResource("http://test.vivo/a");
		b = aBox.createResource("http://test.vivo/b");
		c = aBox.createResource("http://test.vivo/c");
		d = aBox.createResource("http://test.vivo/d");
		
        aBox.add(a,P,c);
		aBox.add(a,S,literal1);
		aBox.add(b,Q,d);
		aBox.add(b,T,literal2);
		aBox.add(a,OWL.sameAs,b);

        //these are now false since sameAs is off
		Assert.assertFalse(inf.contains(b,OWL.sameAs,a));
		Assert.assertFalse(inf.contains(b,P,c));
		Assert.assertFalse(inf.contains(b,S,literal1));
		Assert.assertFalse(inf.contains(a,Q,d));
		Assert.assertFalse(inf.contains(a,T,literal2));
        //these still shouldn't be in the abox
		Assert.assertFalse(aBox.contains(b,OWL.sameAs,a));
		Assert.assertFalse(aBox.contains(b,P,c));
		Assert.assertFalse(aBox.contains(b,S,literal1));
		Assert.assertFalse(aBox.contains(a,Q,d));
		Assert.assertFalse(aBox.contains(a,T,literal2));
	}

	/*
	* sameAs with datatype properties
	*/
	@Test
	public void addABoxAssertion2() {
		OntModel tBox = createTBoxModel(); 
			     		
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
	
		Assert.assertTrue(inf.contains(a,desc,desc2));
		Assert.assertTrue(inf.contains(b,desc,desc1));
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
	}
	
	/*
	* basic scenario of removing an abox assertion for
	* an individual is sameAs another. 
	*/
	@Test
	public void removeABoxAssertion1() {
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");
		OntProperty S = createObjectProperty(tBox, "http://test.vivo/S", "property S");
		OntProperty T = createObjectProperty(tBox, "http://test.vivo/T", "property T");
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
	 * adding and removing an inverseOf assertion for individuals who
	 * are sameAs each other.
	 */
	@Test
	public void tBoxInverseAssertion1() throws InterruptedException {
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");

		// Create ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox

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
			    
	    setInverse(Q, P);
	    
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    	    	
		Assert.assertTrue(inf.contains(b,Q,a));
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,P,b));
		Assert.assertTrue(inf.contains(a,Q,a));

		removeInverse(Q, P);

	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }

		Assert.assertFalse(inf.contains(b,Q,a));
		Assert.assertTrue(inf.contains(b,OWL.sameAs,a));
		Assert.assertTrue(inf.contains(b,P,b));
		Assert.assertFalse(inf.contains(a,Q,a));
		
		simpleReasonerTBoxListener.setStopRequested();
	}
	
	
	/*
	 * adding and removing a type assertion for an individual who has
	 * a sameAs individual.
	 */
	//@Test
	public void tBoxTypeAssertion1() {
		// Create a Tbox with a simple class hierarchy. B is a subclass of A.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
	    addSubclass(classA, classB);
	            
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
		Resource x = aBox.createResource("http://test.vivo/x");
		Resource y = aBox.createResource("http://test.vivo/y");
		Resource z = aBox.createResource("http://test.vivo/z");
		
       	aBox.add(x,OWL.sameAs,y);
       	aBox.add(y,OWL.sameAs,z);
		aBox.add(x,RDF.type,classB);		
	
		Assert.assertTrue(inf.contains(y,RDF.type,classB));
		Assert.assertTrue(inf.contains(y,RDF.type,classA));
		Assert.assertTrue(inf.contains(z,RDF.type,classB));
		Assert.assertTrue(inf.contains(z,RDF.type,classA));
		
		aBox.remove(x,RDF.type,classB);
		Assert.assertFalse(inf.contains(y,RDF.type,classB));
		Assert.assertFalse(inf.contains(y,RDF.type,classA));
		Assert.assertFalse(inf.contains(z,RDF.type,classB));
		Assert.assertFalse(inf.contains(z,RDF.type,classA));
	}

	/*
	 * adding and removing subclass assertion when there is an 
	 * individual member who has a sameAs individual.
	 */
	//@Test
	public void tBoxSubclassAssertion1() throws InterruptedException {
		// Create a Tbox with a simple class hierarchy. B is a subclass of A.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classB = createClass(tBox, "http://test.vivo/B", "class B"); 
		OntClass classC = createClass(tBox, "http://test.vivo/C", "class C"); 

		//create aBox and SimpleReasoner to listen to them
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		SimpleReasonerTBoxListener simpleReasonerTBoxListener = getTBoxListener(simpleReasoner);
		tBox.register(simpleReasonerTBoxListener);

	    // set up ABox
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		
		aBox.add(a, RDF.type, classC);		
	    aBox.add(a, OWL.sameAs, b);
	    aBox.add(c, OWL.sameAs, a);
			    
	    // update TBox
	    addSubclass(classA, classB);
	    	    
	    // wait for SimpleReasonerTBoxListener thread to end
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
	    addSubclass(classB, classC);
	    	    
	    // wait for SimpleReasonerTBoxListener thread to end
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// Verify inferences
		Assert.assertFalse(inf.contains(a, RDF.type, classC));
		Assert.assertTrue(inf.contains(a, RDF.type, classB));
		Assert.assertTrue(inf.contains(a, RDF.type, classA));
		
		Assert.assertTrue(inf.contains(b, RDF.type, classC));
		Assert.assertTrue(inf.contains(b, RDF.type, classB));
		Assert.assertTrue(inf.contains(b, RDF.type, classA));

		Assert.assertTrue(inf.contains(c, RDF.type, classC));
		Assert.assertTrue(inf.contains(c, RDF.type, classB));
		Assert.assertTrue(inf.contains(c, RDF.type, classA));

	    // update TBox
		removeSubclass(classA, classB);

	    // wait for SimpleReasonerTBoxListener thread to end
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// Verify inferences	
		Assert.assertFalse(inf.contains(a, RDF.type, classC));
		Assert.assertTrue(inf.contains(a, RDF.type, classB));
		Assert.assertFalse(inf.contains(a, RDF.type, classA));
		
		Assert.assertTrue(inf.contains(b, RDF.type, classC));
		Assert.assertTrue(inf.contains(b, RDF.type, classB));
		Assert.assertFalse(inf.contains(b, RDF.type, classA));

		Assert.assertTrue(inf.contains(c, RDF.type, classC));
		Assert.assertTrue(inf.contains(c, RDF.type, classB));
		Assert.assertFalse(inf.contains(c, RDF.type, classA));
	    
	    // update TBox
		removeSubclass(classB, classC);
	    
	    // wait for SimpleReasonerTBoxListener thread to end
	    while (!VitroBackgroundThread.getLivingThreads().isEmpty()) {
	    	Thread.sleep(delay);
	    }
	    
		// Verify inferences	
		Assert.assertFalse(inf.contains(a, RDF.type, classC));
		Assert.assertFalse(inf.contains(a, RDF.type, classB));
		Assert.assertFalse(inf.contains(a, RDF.type, classA));
		
		Assert.assertTrue(inf.contains(b, RDF.type, classC));
		Assert.assertFalse(inf.contains(b, RDF.type, classB));
		Assert.assertFalse(inf.contains(b, RDF.type, classA));

		Assert.assertTrue(inf.contains(c, RDF.type, classC));
		Assert.assertFalse(inf.contains(c, RDF.type, classB));
		Assert.assertFalse(inf.contains(c, RDF.type, classA));

		simpleReasonerTBoxListener.setStopRequested();
	}	

	/*
	 * test that mostSpecificType inferences propagate to sameAs 
	 * individuals
	 */
	//@Test
	public void mostSpecificTypeTest1() {
		// Create a Tbox with a simple class hierarchy. B is a subclass of A.
		OntModel tBox = createTBoxModel(); 
		OntClass classA = createClass(tBox, "http://test.vivo/A", "class A"); 
		OntClass classC = createClass(tBox, "http://test.vivo/B", "class C"); 
		OntClass classD = createClass(tBox, "http://test.vivo/B", "class D"); 
		OntClass classE = createClass(tBox, "http://test.vivo/B", "class E");
		addSubclass(classA, classC);
		addSubclass(classC, classD);
		addSubclass(classC, classE);

		// SimpleReasonerTBoxListener is not being used.
		AnnotationProperty mostSpecificType = tBox.createAnnotationProperty(mostSpecificTypePropertyURI);

		// this will receive the abox inferences
        Model inf = ModelFactory.createDefaultModel();
        
        // abox
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 

        // set up SimpleReasoner and register it with abox		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
	    
        // add & remove ABox type statements and verify inferences
		Resource a = aBox.createResource("http://test.vivo/a");
		Resource b = aBox.createResource("http://test.vivo/b");
		Resource c = aBox.createResource("http://test.vivo/c");
		Resource d = aBox.createResource("http://test.vivo/d");
		
		aBox.add(a, OWL.sameAs, b);
		aBox.add(c, OWL.sameAs, b);
		aBox.add(d, OWL.sameAs, a);		
		
		aBox.add(a, RDF.type, classD);	
		aBox.add(d, RDF.type, classC);
		Assert.assertFalse(inf.contains(a,RDF.type,classD));
		Assert.assertTrue(inf.contains(a,RDF.type,classC));
		Assert.assertTrue(inf.contains(b,RDF.type, classD));
		Assert.assertTrue(inf.contains(b,RDF.type, classC));
		Assert.assertTrue(inf.contains(c,RDF.type, classD));
		Assert.assertTrue(inf.contains(c,RDF.type, classC));
		Assert.assertTrue(inf.contains(d,RDF.type, classD));
		Assert.assertFalse(inf.contains(d,RDF.type, classC));
		
		Assert.assertTrue(inf.contains(a, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertTrue(inf.contains(b, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertTrue(inf.contains(c, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertTrue(inf.contains(d, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertFalse(inf.contains(a, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertFalse(inf.contains(b, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertFalse(inf.contains(c, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertFalse(inf.contains(d, mostSpecificType, ResourceFactory.createResource(classC.getURI())));

		aBox.remove(a, RDF.type, classD);
		Assert.assertFalse(inf.contains(a,RDF.type,classD));
		Assert.assertTrue(inf.contains(a,RDF.type,classC));
		Assert.assertFalse(inf.contains(b,RDF.type, classD));
		Assert.assertTrue(inf.contains(b,RDF.type, classC));
		Assert.assertFalse(inf.contains(c,RDF.type, classD));
		Assert.assertTrue(inf.contains(c,RDF.type, classC));
		Assert.assertFalse(inf.contains(d,RDF.type, classD));
		Assert.assertFalse(inf.contains(d,RDF.type, classC));
		Assert.assertTrue(inf.contains(a, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertTrue(inf.contains(b, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertTrue(inf.contains(c, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertTrue(inf.contains(d, mostSpecificType, ResourceFactory.createResource(classC.getURI())));
		Assert.assertFalse(inf.contains(a, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertFalse(inf.contains(b, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertFalse(inf.contains(c, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
		Assert.assertFalse(inf.contains(d, mostSpecificType, ResourceFactory.createResource(classD.getURI())));
	}	

	/*
	 * Basic scenario around recomputing the ABox inferences
	 */
	@Test
	public void recomputeABox1() throws InterruptedException {
		OntModel tBox = createTBoxModel(); 
		OntProperty P = createObjectProperty(tBox, "http://test.vivo/P", "property P");
		OntProperty Q = createObjectProperty(tBox, "http://test.vivo/Q", "property Q");
		OntProperty S = createObjectProperty(tBox, "http://test.vivo/S", "property S");
		OntProperty T = createObjectProperty(tBox, "http://test.vivo/T", "property T");
		Literal literal1 = tBox.createLiteral("Literal value 1");
		Literal literal2 = tBox.createLiteral("Literal value 2");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		
        // Individuals a, b, c and d
		Resource a = aBox.createIndividual("http://test.vivo/a", OWL.Thing);
		Resource b = aBox.createIndividual("http://test.vivo/b", OWL.Thing);
		Resource c = aBox.createIndividual("http://test.vivo/c", OWL.Thing);
		Resource d = aBox.createIndividual("http://test.vivo/d", OWL.Thing);
		
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
		
		inf.remove(b,OWL.sameAs,a);
		inf.remove(b,P,c);
		inf.remove(b,S,literal1);
		inf.remove(a,Q,d);
		inf.remove(a,T,literal2);
		
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
