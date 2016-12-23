/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public class SimpleReasonerPluginTest extends SimpleReasonerTBoxHelper {
	long delay = 50;
	
	private final static String DEFAULT_NS = "http://vivoweb.org/individual/";
	
	private final static String DCTERMS_NS = "http://purl.org/dc/terms/";
	private final static String VIVOCORE_NS = "http://vivoweb.org/ontology/core#";
	
	private final static String creator_URI = DCTERMS_NS + "creator";
	private final static String authorInAuthorship_URI = VIVOCORE_NS + "authorInAuthorship";
	private final static String linkedAuthor_URI = VIVOCORE_NS + "linkedAuthor";
	private final static String informationResourceInAuthorship_URI = VIVOCORE_NS + "informationResourceInAuthorship";
	private final static String linkedInformationResource_URI = VIVOCORE_NS + "linkedInformationResource";
		
	@Before
	public void suppressErrorOutput() {
		//suppressSyserr();
        //Turn off log messages to console
		setLoggerLevel(SimpleReasoner.class, Level.DEBUG);
		setLoggerLevel(SimpleReasonerTBoxListener.class, Level.DEBUG);
	}
	
	/*
	* testing samplePlugin - based on dcterms:creator plugin
	* 
	*/
	@Test
	public void test1()  {
		OntModel tBox = createTBoxModel(); 
		
		OntProperty authorInAuthorship = tBox.createObjectProperty(authorInAuthorship_URI);
		OntProperty linkedAuthor = tBox.createObjectProperty(linkedAuthor_URI);
		OntProperty informationResourceInAuthorship = tBox.createObjectProperty(informationResourceInAuthorship_URI);
		OntProperty linkedInformationResource = tBox.createObjectProperty(linkedInformationResource_URI);
		
		authorInAuthorship.addInverseOf(linkedAuthor);
		informationResourceInAuthorship.addInverseOf(linkedInformationResource);
				
		Literal title1 = tBox.createLiteral("My Findings");
		Literal name1 = tBox.createLiteral("Priscilla Powers");
		
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an ABox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
	
		// register plugin with SimpleReasoner
		List<ReasonerPlugin> pluginList = new ArrayList<ReasonerPlugin>();	
		String pluginClassName = "edu.cornell.mannlib.vitro.webapp.reasoner.plugin.SamplePlugin";
		
		try {
		    ReasonerPlugin plugin = (ReasonerPlugin) Class.forName(pluginClassName).getConstructors()[0].newInstance();
		    plugin.setSimpleReasoner(simpleReasoner);
		    pluginList.add(plugin);
	        simpleReasoner.setPluginList(pluginList);		
		} catch (Exception e) {
			System.out.println("Exception trying to instantiate plugin: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		Property dctermsCreator = ResourceFactory.createProperty(creator_URI);
		
        // add abox data for person, authorship and article.	
		// note, they aren't actually typed in this test tbox
		Resource prissy = aBox.createResource(DEFAULT_NS + "prissy");
	
		// assert same as
		
		Resource authorship1 = aBox.createResource(DEFAULT_NS + "authorship1");
		Resource article1 = aBox.createResource(DEFAULT_NS + "article1");
		Resource article100 = aBox.createResource(DEFAULT_NS + "article100");		
		
		aBox.add(prissy,RDFS.label,name1);
		aBox.add(prissy,authorInAuthorship,authorship1);
		
		aBox.add(authorship1,linkedAuthor,prissy);
		aBox.add(authorship1,linkedInformationResource,article1);
		
		aBox.add(article1,RDFS.label,title1);
		aBox.add(article1,informationResourceInAuthorship,authorship1);
        aBox.add(article1, OWL.sameAs, article100);

		Assert.assertTrue(inf.contains(article1,dctermsCreator,prissy));
		Assert.assertTrue(inf.contains(article100,dctermsCreator,prissy));	
		
		aBox.remove(authorship1,linkedAuthor,prissy);
		
		Assert.assertFalse(inf.contains(article1,dctermsCreator,prissy));
		Assert.assertFalse(inf.contains(article100,dctermsCreator,prissy));				
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