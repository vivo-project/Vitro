package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicyTest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualDaoJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collection;

public class EntityDaoJenaTest extends AbstractTestClass {

    OntModel dorkHobbyModel;
    
    @Before
    public void setUp() throws Exception {
    	// Suppress error logging.
		setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

		Model model = ModelFactory.createDefaultModel();        
        InputStream in = JenaNetidPolicyTest.class.getResourceAsStream("resources/dorkyhobbies.owl");
        model.read(in,null);
        dorkHobbyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,model);
    }

	/**
	 * This is a class that had no tests, so Brian and I pulled one back. But it
	 * doesn't compile, and it appears to be intended to test a method that has
	 * not been implemented. So for now, we give it a pass.
	 */
    @Test
    public void testGetEntitiesByProperties() {
    	// This is the class that had no tests, so we pulled one back.
//        IndividualDaoJena edj = new IndividualDaoJena();
//        edj.setOntModel(dorkHobbyModel);
//        String propURI="http://test.mannlib.cornell.edu#hasHobby",
//            ignoreEntURI="http://test.mannlib.cornell.edu#bob",
//            classURI=null;
//        
//        //bob hasHobby x
//        Collection ents = 
//            edj.getIndividualsByObjectProperty(propURI, ignoreEntURI, classURI, true);        
//        assertNotNull(ents);                        
    }

}
