/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class PrimitiveRdfEditTest {
    
    OntModel testModel;
    WebappDaoFactory wdf;

	private String testN3a = 
		"<http://example.com/motorcycles/honda/vtl1000> <http://example.com/engines/displacement> \"1000cm3\" ." +
		"<http://example.com/motorcycles/honda/919> <http://example.com/engines/displacement> \"919cm3\" ." ;
	
	private String testN3b = 
		"<http://example.com/motorcycles/honda/919> <http://example.com/motorcycles/relatedTo> <http://exmaple.com/motorcycle/honda/599> ." ;
	
	
	@Before
	public void setUp() throws Exception { }

	@Test
	public void testProcessChanges() throws Exception {
		OntModel writeModel = ModelFactory.createOntologyModel();
        
		int totalStmts = 3;
		
		PrimitiveRdfEdit pre = new PrimitiveRdfEdit();
		String params[] = { testN3a, testN3b };
		Set<Model> models = pre.parseRdfParam(params, "N3");
		Assert.assertNotNull(models);
		Assert.assertTrue( models.size() == 2);
				
		Assert.assertNotNull( writeModel );
		long size = writeModel.size();
		pre.processChanges("uri:fakeEditorUri", writeModel,
				pre.mergeModels(models), ModelFactory.createDefaultModel());
		Assert.assertEquals(size+totalStmts, writeModel.size());
				
		String params3[] = { testN3b };
		Set<Model> retracts = pre.parseRdfParam( params3, "N3");		
		pre.processChanges("uri:fakeEditorUri", writeModel, 
				ModelFactory.createDefaultModel(), pre.mergeModels(retracts));
		Assert.assertEquals(size+totalStmts-1, writeModel.size());		
	}

	@Test
	public void testParseRdfParam() throws Exception {
		PrimitiveRdfEdit pre = new PrimitiveRdfEdit();
		String params[] = { testN3a, testN3b };
		Set<Model> models = pre.parseRdfParam(params, "N3");
		Assert.assertNotNull(models);
		Assert.assertTrue( models.size() == 2);
	}
}
