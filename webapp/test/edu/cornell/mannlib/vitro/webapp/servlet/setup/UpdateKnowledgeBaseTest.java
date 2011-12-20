/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.StringReader;

import junit.framework.Assert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class UpdateKnowledgeBaseTest extends AbstractTestClass {

	//@org.junit.Test
	public void testMigrateDisplayModel12() {
		
		String version12DisplayModel = "\n" + 
		       " <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#PrimaryLuceneIndex> " +
		       "     a <http://www.w3.org/2002/07/owl#Thing> . \n" +
		       " <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Organizations> \n" +
		       "	  a       <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#ClassGroupPage> , \n" +
		       "              <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Page> ; \n" +
		       "      <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#forClassGroup> \n " +
		       "              <http://vivoweb.org/ontology#vitroClassGrouporganizations> ; \n " +
		       "      <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#title> \n" +
		       "              \"Organizations\" ; \n" +
		       "      <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#urlMapping> \n " +
		       "              \"/organizations\" . ";
		
		String version13DisplayModel = "\n" +
    	       " <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#SearchIndex> " +
	           "     a <http://www.w3.org/2002/07/owl#Thing> . \n" +
		       " <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Organizations> \n" +
		       "     a       <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Page> ; \n " +
		       "     <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#hasDataGetter> \n " +
		       "             <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#OrganizationsDataGetter1> ; " +
		       "     <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#title> \n " +
		       "             \"Organizations\" ; \n" +
		       "     <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#urlMapping> \n" +
		       "             \"/organizations\" . \n" +
		       " <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#OrganizationsDataGetter1> \n" + 
		       "     a       <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#ClassGroupPage> ; \n" +
		       "     <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#forClassGroup> \n" +
		       "             <http://vivoweb.org/ontology#vitroClassGrouporganizations> .";
		
		Model preMigrate = ModelFactory.createDefaultModel().read(new StringReader(version12DisplayModel), null, "N3");
		Model postMigrate = ModelFactory.createDefaultModel().read(new StringReader(version13DisplayModel), null, "N3");
		UpdateKnowledgeBase.migrateDisplayModel(preMigrate);
		Assert.assertTrue(preMigrate.isIsomorphicWith(postMigrate));
		
	}
	
	@org.junit.Test
	public void testMigrateDisplayModel13() {
		
      return;		
	}
	
}
