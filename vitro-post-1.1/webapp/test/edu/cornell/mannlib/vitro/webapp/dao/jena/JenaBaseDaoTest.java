/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

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
	String isDependentRelation =
		" <"+VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT+"> \"true\"^^xsd:boolean .\n" ;
	
	String nosePropIsDependentRel = 
	"<"+VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT+"> rdf:type owl:AnnotationProperty .\n" +
    " ex:hasNose " + isDependentRelation;
	
    String prefixesN3 = 
        "@prefix vitro: <" + VitroVocabulary.vitroURI + "> . \n" +
        "@prefix xsd: <" + XSD.getURI() + "> . \n " +                   
        "@prefix rdf:  <" + RDF.getURI() + "> . \n"+
        "@prefix rdfs: <" + RDFS.getURI() + "> . \n"+
        "@prefix owl:  <" + OWL.getURI() + "> . \n" +
        "@prefix ex: <http://example.com/> . \n" ;
    
	@Test
	public void smartRemoveTestForIndivdiualDelete(){
		
		String n3 = prefixesN3 + 
			"ex:prop1 rdf:type owl:ObjectProperty ." +
			"ex:prop1 rdfs:label \"Prop 1 Dependent Relation\" ." +
			"ex:prop1 " + isDependentRelation;
					
		Model readInModel = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.add(readInModel);
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( ontModel);
		
		try {
			ObjectProperty prop1 = wdfj.getObjectPropertyDao().getObjectPropertyByURI("http://example.com/prop1");
			Assert.assertNotNull(prop1);
			
			Individual ind = new IndividualImpl();
			ind.setURI("http://example.com/bob");
			ind.setName("Smith, Bob");		
			
			wdfj.getIndividualDao().insertNewIndividual(ind);			
					
			Individual indxyz = new IndividualImpl();
			indxyz.setURI("http://example.com/depResXYZ");
			indxyz.setName("depResXYZ");			
			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
			
			Individual indAbc = new IndividualImpl();
			indAbc.setURI("http://example.com/depResNested");
			indAbc.setName("depResNested");					
			wdfj.getIndividualDao().insertNewIndividual(indAbc);								
		
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
			
			wdfj.getIndividualDao().deleteIndividual("http://example.com/depResXYZ");
			
			String expected =
				"@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> . "+
				"@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> ."+
				"@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."+
				"@prefix owl:     <http://www.w3.org/2002/07/owl#> . "+
				"<http://example.com/bob>      a       owl:Thing ; " +
				"    rdfs:label \"Smith, Bob\"@en-US . "+
				"<http://example.com/prop1>  " +
				"    a       owl:ObjectProperty ; " +
				"    rdfs:label \"Prop 1 Dependent Relation\" ; " +
			         isDependentRelation ;
										
			Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "N3");

			assertEquivalentModels(expectedModel, ontModel);
			} catch (InsertException e) {
				Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void smartRemoveTestForObjPropStmtDelete(){
		String n3 = prefixesN3 + 
			"ex:prop1 rdf:type owl:ObjectProperty ." +
			"ex:prop1 rdfs:label \"Prop 1 Dependent Relation\" ." +
			"ex:prop1 " + isDependentRelation;
				
		Model readInModel = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
		OntModel model = ModelFactory.createOntologyModel();
		model.add(readInModel);
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( model);								
				
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
		try {
			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResXYZ");
		}				
		
		Individual indAbc = new IndividualImpl();
		indAbc.setURI("http://example.com/depResNested");
		indAbc.setName("depResNested");
		try {
			wdfj.getIndividualDao().insertNewIndividual(indAbc);			
		} catch (InsertException e) {
			Assert.fail("Could not create new Individual depResNested");
		}			
		
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
		
		ops = new ObjectPropertyStatementImpl();
		ops.setSubjectURI("http://example.com/bob");
		ops.setPropertyURI("http://example.com/prop1");
		ops.setObjectURI("http://example.com/depResXYZ");
		wdfj.getObjectPropertyStatementDao().deleteObjectPropertyStatement(ops);
		
		String expected =
			"@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> . "+
			"@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> ."+
			"@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."+
			"@prefix owl:     <http://www.w3.org/2002/07/owl#> . "+
			"<http://example.com/bob>      a       owl:Thing ; " +
			"    rdfs:label \"Smith, Bob\"@en-US . "+
			"<http://example.com/prop1>  " +
			"    a       owl:ObjectProperty ; " +
			"    rdfs:label \"Prop 1 Dependent Relation\" ; " +
			     isDependentRelation ;
									
		Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "N3");		
				
		assertEquivalentModels(expectedModel, model);
	}
	
	
	@Test
	public void smartRemoveTestForObjPropDelete(){				
		String n3 = prefixesN3 + 
			"ex:prop1 rdf:type owl:ObjectProperty ." +
			"ex:prop1 rdfs:label \"Prop 1 Dependent Relation\" ." +
			"ex:prop1 " + isDependentRelation;
					
		Model readInModel = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.add(readInModel);
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( ontModel);
		
		try {
			ObjectProperty prop1 = wdfj.getObjectPropertyDao().getObjectPropertyByURI("http://example.com/prop1");
			Assert.assertNotNull(prop1);
			
			Individual ind = new IndividualImpl();
			ind.setURI("http://example.com/bob");
			ind.setName("Smith, Bob");		
			
			wdfj.getIndividualDao().insertNewIndividual(ind);			
					
			Individual indxyz = new IndividualImpl();
			indxyz.setURI("http://example.com/depResXYZ");
			indxyz.setName("depResXYZ");			
			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
			
			Individual indAbc = new IndividualImpl();
			indAbc.setURI("http://example.com/depResNested");
			indAbc.setName("depResNested");					
			wdfj.getIndividualDao().insertNewIndividual(indAbc);								
		
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
			
			wdfj.getObjectPropertyDao().deleteObjectProperty(prop1);
			
			String expected =
				"@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> . "+
				"@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> ."+
				"@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."+
				"@prefix owl:     <http://www.w3.org/2002/07/owl#> . "+
				"<http://example.com/bob>      a       owl:Thing ; " +
				"    rdfs:label \"Smith, Bob\"@en-US . " ;
										
			Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "N3");
			
			assertEquivalentModels(expectedModel, ontModel);
			} catch (InsertException e) {
				Assert.fail(e.getMessage());
		}
	}
	
//	@Test
//	public void smartRemoveTestForObjPropDelete(){
//		
//		OntModel model = ModelFactory.createOntologyModel();
//		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena( model );
//
//		/* Need to have the DEPENDENT_RESOURCE class in the model */
//		VClass cls = new VClass();
//		//cls.setURI(VitroVocabulary.DEPENDENT_RESOURCE);
//		try {
//			wdfj.getVClassDao().insertNewVClass(cls);
//		} catch (InsertException e1) {
//			Assert.fail("could not create class for dependentResourc");
//		}
//		
//		/* Need to have an Object Property */
//		ObjectProperty op = new ObjectProperty();
//		op.setURI("http://example.com/prop1");
//		try {
//			wdfj.getObjectPropertyDao().insertObjectProperty(op);
//		} catch (InsertException e1) {
//			Assert.fail("Could not create object property.");
//		}
//		
//		Individual ind = new IndividualImpl();
//		ind.setURI("http://example.com/bob");
//		ind.setName("Smith, Bob");
//		try {
//			wdfj.getIndividualDao().insertNewIndividual(ind);
//		} catch (InsertException e) {
//			Assert.fail("Could not create new Individual Smith, Bob");
//		}
//				
//		Individual indxyz = new IndividualImpl();
//		indxyz.setURI("http://example.com/depResXYZ");
//		indxyz.setName("depResXYZ");
//		//indxyz.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
//		try {
//			wdfj.getIndividualDao().insertNewIndividual(indxyz);			
//		} catch (InsertException e) {
//			Assert.fail("Could not create new Individual depResXYZ");
//		}				
////		StmtIterator it = model.listStatements(model.createResource("http://example.com/depResXYZ"),
////				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
////		Assert.assertTrue("depResXYZ did not get rdf:type vitro:dependentResource" ,
////				it != null && it.nextStatement() != null);
//		
//		Individual indAbc = new IndividualImpl();
//		indAbc.setURI("http://example.com/depResNested");
//		indAbc.setName("depResNested");
////		indAbc.setVClassURI(VitroVocabulary.DEPENDENT_RESOURCE);
//		try {
//			wdfj.getIndividualDao().insertNewIndividual(indAbc);			
//		} catch (InsertException e) {
//			Assert.fail("Could not create new Individual depResNested");
//		}	
////		it = model.listStatements(model.createResource("http://example.com/depResNested"),
////				RDF.type, model.createResource(VitroVocabulary.DEPENDENT_RESOURCE));
////		Assert.assertTrue("depResNested did not get rdf:type vitro:dependentResource" ,
////				it != null && it.nextStatement() != null);
//		
//		
//		ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
//		ops.setSubjectURI("http://example.com/bob");
//		ops.setPropertyURI("http://example.com/prop1");
//		ops.setObjectURI("http://example.com/depResXYZ");
//		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
//		
//		ops = new ObjectPropertyStatementImpl();
//		ops.setSubjectURI("http://example.com/depResXYZ");
//		ops.setPropertyURI("http://example.com/prop1");
//		ops.setObjectURI("http://example.com/depResNested");
//		wdfj.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(ops);
//		
//		String expected = "<rdf:RDF\n"+
//		"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
//		"    xmlns:j.0=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\"\n"+
//		"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"+
//		"    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
//		"    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
//		"    xmlns:j.1=\"http://example.com/\" > \n"+
//		"  <rdf:Description rdf:about=\"http://example.com/bob\">\n"+
//		"    <rdfs:label xml:lang=\"en-US\">Smith, Bob</rdfs:label>\n"+
//		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>\n"+
//		"  </rdf:Description>\n"+
//		"  <rdf:Description rdf:about=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource\">\n"+
//		"    <j.0:displayRankAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayRankAnnot>\n"+
//		"    <j.0:displayLimitAnnot rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">-1</j.0:displayLimitAnnot>\n"+
//		"    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Class\"/>\n"+
//		"  </rdf:Description>\n"+
//		"</rdf:RDF>";
//								
//		wdfj.getObjectPropertyDao().deleteObjectProperty(op);
//		
//		Model expectedModel = (ModelFactory.createOntologyModel()).read(new StringReader(expected), "", "RDF/XML");
//		
//		//modtime times make it difficult to compare graphs
//		wipeOutModTime(expectedModel);
//		wipeOutModTime(model);
//		Assert.assertTrue( model.isIsomorphicWith(expectedModel));		
//	}

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
		
	@Test
	/**
	 * Test that removing classes or properties used in restrictions
	 * does not leave behind broken, syntactically-invalid restrictions.
	 * The restrictions should be deleted.
	 */
	public void testPreventInvalidRestrictionsOnDeletion() {
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		WebappDaoFactoryJena wadf = new WebappDaoFactoryJena(m);
		
		String ns = "http://example.org/ontology/";
		String class1URI = ns + "Class1";
		String class2URI = ns + "Class2";
		String propURI = ns + "property";
		
		OntClass class1 = m.createClass(class1URI);
		OntClass class2 = m.createClass(class2URI);
		OntProperty prop = m.createObjectProperty(propURI);
		Restriction rest = m.createAllValuesFromRestriction(null, prop, class2);
		class1.addSuperClass(rest);
		
		ObjectProperty op = wadf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
		wadf.getObjectPropertyDao().deleteObjectProperty(op);
		
		Assert.assertEquals(class1.listSuperClasses().toSet().size(), 0);
		Assert.assertEquals(m.size(), 2); // just rdf:type owl:Class for Class1 and Class2
		
		prop = m.createObjectProperty(propURI);
		rest = m.createAllValuesFromRestriction(null, prop, class2);
		class1.addSuperClass(rest);
		
		VClass vclass = wadf.getVClassDao().getVClassByURI(class2URI);
		wadf.getVClassDao().deleteVClass(vclass);
		
		Assert.assertEquals(class1.listSuperClasses().toSet().size(), 0);
		Assert.assertEquals(m.size(), 2); // just rdf:type for Class1 and Prop
		
	}	

	/**
	 * Compare the contents of the expected model with the actual model (not counting modification times).
	 */
	private void assertEquivalentModels(Model expected, Model actual) {
		// modtime times make it difficult to compare graphs
		wipeOutModTime(expected);
		wipeOutModTime(actual);

		if (actual.isIsomorphicWith(expected)) {
			return;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream p = new PrintStream(out, true);
		p.println("Models do not match: expected <");
		expected.write(out);
		p.println("> but was <");
		actual.write(out);
		p.println(">");
		Assert.fail(out.toString());
	}

	private void wipeOutModTime(Model model){
		model.removeAll(null, model.createProperty(VitroVocabulary.MODTIME), null);
	}
	

}
