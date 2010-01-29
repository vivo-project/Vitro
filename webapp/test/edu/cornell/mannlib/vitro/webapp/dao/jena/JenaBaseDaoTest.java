package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.StringReader;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;


public class JenaBaseDaoTest {
	
	@Test
	public void smartRemoveTestForIndivdiualDelete(){
		
		OntModel model = ModelFactory.createOntologyModel();
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( model );

		/* Need to have the DEPENDENT_RESOURCE class in the model */
		VClass cls = new VClass();
		cls.setURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getVClassDao().insertNewVClass(cls);
		} catch (InsertException e1) {
			Assert.fail("could not create class for dependentResourc");
		}
		
		/* Need to have an Object Property */
		ObjectProperty op = new ObjectProperty();
		op.setURI("http://example.com/prop1");
		try {
			wdfj.getObjectPropertyDao().insertObjectProperty(op);
		} catch (InsertException e1) {
			Assert.fail("Could not create object property.");
		}
		
		Individual ind = new IndividualImpl();
		ind.setURI("http://example.com/bob");
		ind.setName("Smith, Bob");
		try {
			wdfj.getIndividualDao().insertNewIndividual(ind);
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual Smith, Bob");
		}
				
		Individual indxyz = new IndividualImpl();
		indxyz.setURI("http://example.com/depResXYZ");
		indxyz.setName("depResXYZ");
		indxyz.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResXYZ");
		}				
		StmtIterator it = model.listStatements(model.createResource("http://example.com/depResXYZ"),
				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
		Assert.assertTrue("depResXYZ did not get rdf:type vitro:dependentResource" ,
				it != null && it.nextStatement() != null);
		
		Individual indAbc = new IndividualImpl();
		indAbc.setURI("http://example.com/depResNested");
		indAbc.setName("depResNested");
		indAbc.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getIndividualDao().insertNewIndividual(indAbc);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResNested");
		}	
		it = model.listStatements(model.createResource("http://example.com/depResNested"),
				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
		Assert.assertTrue("depResNested did not get rdf:type vitro:dependentResource" ,
				it != null && it.nextStatement() != null);
		
		
		ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/bob");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResXYZ");
		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
		
		ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/depResXYZ");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResNested");
		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
		
		String expected = "<rdf:RDF\n"+
		"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
		"    xmlns:j.0=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\"\n"+
		"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"+
		"    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
		"    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
		"    xmlns:j.1=\"http://example.com/\" > \n"+
		"  <rdf:Description rdf:about=\"http://example.com/bob\">\n"+
		"    <j.0:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-01-25T15:27:54</j.0:modTime>\n"+
		"    <rdfs:label xml:lang=\"en-US\">Smith, Bob</rdfs:label>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>\n"+
		"  </rdf:Description>\n"+
		"  <rdf:Description rdf:about=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource\">\n"+
		"    <j.0:displayRankAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayRankAnnot>\n"+
		"    <j.0:displayLimitAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayLimitAnnot>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Class\"/>\n"+
		"  </rdf:Description>\n"+
		"  <rdf:Description rdf:about=\"http://example.com/prop1\">\n"+
		"    <j.0:selectFromExistingAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</j.0:selectFromExistingAnnot>\n"+
		"    <j.0:displayLimitAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">5</j.0:displayLimitAnnot>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#ObjectProperty\"/>\n"+
		"  </rdf:Description>\n"+
		"</rdf:RDF>";
		
		wdfj.getIndividualDao().deleteIndividual("http://example.com/depResXYZ");
		
		
		Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "RDF/XML");
		
		//modtime times make it difficult to compare graphs
		wipeOutModTime(expectedModel);
		wipeOutModTime(model);
		
		Assert.assertTrue( model.isIsomorphicWith(expectedModel));		
	}
	
	@Test
	public void smartRemoveTestForObjPropStmtDelete(){
		
		OntModel model = ModelFactory.createOntologyModel();
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( model );

		/* Need to have the DEPENDENT_RESOURCE class in the model */
		VClass cls = new VClass();
		cls.setURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getVClassDao().insertNewVClass(cls);
		} catch (InsertException e1) {
			Assert.fail("could not create class for dependentResourc");
		}
		
		/* Need to have an Object Property */
		ObjectProperty op = new ObjectProperty();
		op.setURI("http://example.com/prop1");
		try {
			wdfj.getObjectPropertyDao().insertObjectProperty(op);
		} catch (InsertException e1) {
			Assert.fail("Could not create object property.");
		}
		
		Individual ind = new IndividualImpl();
		ind.setURI("http://example.com/bob");
		ind.setName("Smith, Bob");
		try {
			wdfj.getIndividualDao().insertNewIndividual(ind);
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual Smith, Bob");
		}
				
		Individual indxyz = new IndividualImpl();
		indxyz.setURI("http://example.com/depResXYZ");
		indxyz.setName("depResXYZ");
		indxyz.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResXYZ");
		}				
		StmtIterator it = model.listStatements(model.createResource("http://example.com/depResXYZ"),
				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
		Assert.assertTrue("depResXYZ did not get rdf:type vitro:dependentResource" ,
				it != null && it.nextStatement() != null);
		
		Individual indAbc = new IndividualImpl();
		indAbc.setURI("http://example.com/depResNested");
		indAbc.setName("depResNested");
		indAbc.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getIndividualDao().insertNewIndividual(indAbc);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResNested");
		}	
		it = model.listStatements(model.createResource("http://example.com/depResNested"),
				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
		Assert.assertTrue("depResNested did not get rdf:type vitro:dependentResource" ,
				it != null && it.nextStatement() != null);
		
		
		ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/bob");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResXYZ");
		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
		
		ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/depResXYZ");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResNested");
		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
		
		String expected = "<rdf:RDF\n"+
		"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
		"    xmlns:j.0=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\"\n"+
		"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"+
		"    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
		"    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
		"    xmlns:j.1=\"http://example.com/\" > \n"+
		"  <rdf:Description rdf:about=\"http://example.com/bob\">\n"+
		"    <j.0:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-01-25T15:27:54</j.0:modTime>\n"+
		"    <rdfs:label xml:lang=\"en-US\">Smith, Bob</rdfs:label>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>\n"+
		"  </rdf:Description>\n"+
		"  <rdf:Description rdf:about=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource\">\n"+
		"    <j.0:displayRankAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayRankAnnot>\n"+
		"    <j.0:displayLimitAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayLimitAnnot>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Class\"/>\n"+
		"  </rdf:Description>\n"+
		"  <rdf:Description rdf:about=\"http://example.com/prop1\">\n"+
		"    <j.0:selectFromExistingAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</j.0:selectFromExistingAnnot>\n"+
		"    <j.0:displayLimitAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">5</j.0:displayLimitAnnot>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#ObjectProperty\"/>\n"+
		"  </rdf:Description>\n"+
		"</rdf:RDF>";
								
		
		ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/bob");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResXYZ");
		wdfj.getObjectPropertyStatementDao().deleteObjectPropertyStatement(ops);
		
		Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "RDF/XML");
		
		//modtime times make it difficult to compare graphs
		wipeOutModTime(expectedModel);
		wipeOutModTime(model);
		
		Assert.assertTrue( model.isIsomorphicWith(expectedModel));		
	}
	
	@Test
	public void smartRemoveTestForObjPropDelete(){
		
		OntModel model = ModelFactory.createOntologyModel();
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( model );

		/* Need to have the DEPENDENT_RESOURCE class in the model */
		VClass cls = new VClass();
		cls.setURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getVClassDao().insertNewVClass(cls);
		} catch (InsertException e1) {
			Assert.fail("could not create class for dependentResourc");
		}
		
		/* Need to have an Object Property */
		ObjectProperty op = new ObjectProperty();
		op.setURI("http://example.com/prop1");
		try {
			wdfj.getObjectPropertyDao().insertObjectProperty(op);
		} catch (InsertException e1) {
			Assert.fail("Could not create object property.");
		}
		
		Individual ind = new IndividualImpl();
		ind.setURI("http://example.com/bob");
		ind.setName("Smith, Bob");
		try {
			wdfj.getIndividualDao().insertNewIndividual(ind);
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual Smith, Bob");
		}
				
		Individual indxyz = new IndividualImpl();
		indxyz.setURI("http://example.com/depResXYZ");
		indxyz.setName("depResXYZ");
		indxyz.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResXYZ");
		}				
		StmtIterator it = model.listStatements(model.createResource("http://example.com/depResXYZ"),
				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
		Assert.assertTrue("depResXYZ did not get rdf:type vitro:dependentResource" ,
				it != null && it.nextStatement() != null);
		
		Individual indAbc = new IndividualImpl();
		indAbc.setURI("http://example.com/depResNested");
		indAbc.setName("depResNested");
		indAbc.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
		try {
			wdfj.getIndividualDao().insertNewIndividual(indAbc);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResNested");
		}	
		it = model.listStatements(model.createResource("http://example.com/depResNested"),
				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
		Assert.assertTrue("depResNested did not get rdf:type vitro:dependentResource" ,
				it != null && it.nextStatement() != null);
		
		
		ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/bob");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResXYZ");
		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
		
		ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/depResXYZ");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResNested");
		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
		
		String expected = "<rdf:RDF\n"+
		"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
		"    xmlns:j.0=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\"\n"+
		"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"+
		"    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
		"    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
		"    xmlns:j.1=\"http://example.com/\" > \n"+
		"  <rdf:Description rdf:about=\"http://example.com/bob\">\n"+
		"    <rdfs:label xml:lang=\"en-US\">Smith, Bob</rdfs:label>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>\n"+
		"  </rdf:Description>\n"+
		"  <rdf:Description rdf:about=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource\">\n"+
		"    <j.0:displayRankAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayRankAnnot>\n"+
		"    <j.0:displayLimitAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayLimitAnnot>\n"+
		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Class\"/>\n"+
		"  </rdf:Description>\n"+
		"</rdf:RDF>";
								
		wdfj.getObjectPropertyDao().deleteObjectProperty(op);
		
		Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "RDF/XML");
		
		//modtime times make it difficult to compare graphs
		wipeOutModTime(expectedModel);
		wipeOutModTime(model);
		Assert.assertTrue( model.isIsomorphicWith(expectedModel));		
	}
	void printModels(Model expected, Model result){
    	System.out.println("Expected:");
    	expected.write(System.out);
    	System.out.println("Result:");
    	result.write(System.out);    
    }

	void wipeOutModTime(Model model){
		model.removeAll(null, model.createProperty(VitroVocabulary.MODTIME), null);
	}
	
	@Test 
	/**
	 * Tests that any statements with a property as predicate are removed
	 * when the property itself is removed
	 */
	public void testABoxAssertionsRemovedWhenPropertyRemoved() throws InsertException {
		OntModel preModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel postModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		
		WebappDaoFactoryJena preWadf = new WebappDaoFactoryJena(preModel);
		
		// make some other stuff that won't be deleted
		ObjectProperty objPropNotForDeletion = new ObjectProperty();
		objPropNotForDeletion.setURI("http://dont.delete.me/objProp");
		preWadf.getObjectPropertyDao().insertObjectProperty(objPropNotForDeletion);
		DataProperty dataPropNotForDeletion = new DataProperty();
		dataPropNotForDeletion.setURI("http://dont.delete.me/dataProp");
		preWadf.getDataPropertyDao().insertDataProperty(dataPropNotForDeletion);
		ObjectPropertyStatement objPropStmtNotForDeletion = new ObjectPropertyStatementImpl();
		objPropStmtNotForDeletion.setSubjectURI("http://individual.example.org/a/");
		objPropStmtNotForDeletion.setProperty(objPropNotForDeletion);
		objPropStmtNotForDeletion.setObjectURI("http://individual.example.org/b/");
		preWadf.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(objPropStmtNotForDeletion);
		DataPropertyStatement dataPropStmtNotForDeletion = new DataPropertyStatementImpl();
		dataPropStmtNotForDeletion.setIndividualURI("http://individual.example.org/a/");
		dataPropStmtNotForDeletion.setDatapropURI(dataPropNotForDeletion.getURI());
		dataPropStmtNotForDeletion.setData("junk");
		
		// copy the not-for-deletion data to the postModel
		postModel.add(preModel);
		
		// Make some properties and assertions that should be deleted.
		// After deletion, the "preModel" should match the "postModel," which
        // only contains the not-for-deletion statements.		
		ObjectProperty objProp = new ObjectProperty();
		objProp.setURI("http://example.org/objProp");
		preWadf.getObjectPropertyDao().insertObjectProperty(objProp);
		DataProperty dataProp = new DataProperty();
		dataProp.setURI("http://example.org/dataProp");
		preWadf.getDataPropertyDao().insertDataProperty(dataProp);
		ObjectPropertyStatement objPropStmtForDeletion = new ObjectPropertyStatementImpl();
		objPropStmtForDeletion.setSubjectURI("http://example.org/sdfadsf/");
		objPropStmtForDeletion.setProperty(objProp);
		objPropStmtForDeletion.setObjectURI("http://example.org/asdfasdfasfa/");
		preWadf.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(objPropStmtForDeletion);
		DataPropertyStatement dataPropStmtForDeletion = new DataPropertyStatementImpl();
		dataPropStmtForDeletion.setIndividualURI("http://example.org/asdf123/");
		dataPropStmtForDeletion.setDatapropURI(dataProp.getURI());
		dataPropStmtForDeletion.setData("I will be deleted!");
		preWadf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dataPropStmtForDeletion);
		
		// delete the object property and the data property.  
		// The abox assertions should go with them.
		preWadf.getObjectPropertyDao().deleteObjectProperty(objProp);
		preWadf.getDataPropertyDao().deleteDataProperty(dataProp);
		
		// the preModel and the postModel should now have the same statements
		//Assert.assertTrue(preModel.isIsomorphicWith(postModel));
		Assert.assertTrue(preModel.size() == postModel.size());
		
	}
}
