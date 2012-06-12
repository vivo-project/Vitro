package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static junit.framework.Assert.*;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration;


public class LocalAppNamespaceSetupTest {

	
	@Test
	public void newLANSFromDefaultNSTest(){
		
		String ns = LocalAppNamespaceSetup.newLANSFromDefaultNS("http://example/individual/");				
		assertEquals("http://example/application/", ns);
		
		ns = LocalAppNamespaceSetup.newLANSFromDefaultNS("http://example/ns#");
		assertEquals("http://example/ns/application/", ns);
		
		ns = LocalAppNamespaceSetup.newLANSFromDefaultNS("http://example/ns/");
		assertEquals("http://example/ns/application/", ns);
	}
	
	@Test
	public void fromModelTest(){
		OntModel appModel = ModelFactory.createOntologyModel();
		
		LocalAppNamespaceSetup setup = new LocalAppNamespaceSetup();
		String ns = setup.getLANSFromDisplayModel(appModel, null);
		assertNull( ns );
		
		appModel.createIndividual("http://example/application/ConfigXYZ123", ApplicationConfiguration.ClassEditConfig);
		ns = setup.getLANSFromDisplayModel(appModel, null);
		assertNotNull( ns );
		assertEquals("http://example/application/", ns);		
	}
}
