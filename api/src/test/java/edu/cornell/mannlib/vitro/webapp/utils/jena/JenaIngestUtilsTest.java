/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import org.junit.Assert;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class JenaIngestUtilsTest extends AbstractTestClass {

	@Test
	public void testRenameBNodes() {
		
		JenaIngestUtils jiu = new JenaIngestUtils();
		
		Model blankModel = ModelFactory.createDefaultModel();
		for (int i = 0; i < 20; i++) {
			blankModel.add(blankModel.createResource(), RDF.type, OWL.Thing);
		}
		Assert.assertTrue(blankModel.size() == 20);
		
		Model named = jiu.renameBNodes(blankModel, "http://example.org/resource");
		Assert.assertTrue(named.size() == blankModel.size());
		Assert.assertTrue(named.size() == 20);
		
		StmtIterator stmtIt = named.listStatements();
		while (stmtIt.hasNext()) {
			Statement stmt = stmtIt.nextStatement();
			Assert.assertEquals("http://example.org/", stmt.getSubject().getNameSpace());
		}
		
	}
	
}
