/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


public class ObjectPropertyDaoJenaTest extends AbstractTestClass {  
        
	@Test
	public void testCollateBySubclass(){
		/* Check that we can save collateBySubclass */
		OntModel model = ModelFactory.createOntologyModel();
		WebappDaoFactory wdf = new WebappDaoFactoryJena(model);
		
		ObjectProperty op1 = new ObjectProperty();
		String propURI = "http://example.com/testObjectProp" ;		
		op1.setURI(propURI);
		Assert.assertFalse(op1.getCollateBySubclass());
		try {
			wdf.getObjectPropertyDao().insertObjectProperty(op1);
			ObjectProperty op2 = wdf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
			Assert.assertNotNull(op2);
			Assert.assertFalse(op2.getCollateBySubclass());
			
			op2.setCollateBySubclass(true);
			wdf.getObjectPropertyDao().updateObjectProperty(op2);
			
			ObjectProperty op3 = wdf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
			Assert.assertNotNull(op3);
			Assert.assertTrue(op3.getCollateBySubclass());
			
			op3.setCollateBySubclass(false);
			wdf.getObjectPropertyDao().updateObjectProperty(op3);
			
			ObjectProperty op4 = wdf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
			Assert.assertNotNull(op4);
			Assert.assertFalse(op4.getCollateBySubclass());
			
		} catch (InsertException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testStubObjectProperty(){
		/* Check that we can save collateBySubclass */
		OntModel model = ModelFactory.createOntologyModel();
		WebappDaoFactory wdf = new WebappDaoFactoryJena(model);
		
		ObjectProperty op1 = new ObjectProperty();
		String propURI = "http://example.com/testObjectProp" ;		
		op1.setURI(propURI);
		Assert.assertFalse(op1.getStubObjectRelation());
		try {
			wdf.getObjectPropertyDao().insertObjectProperty(op1);
			ObjectProperty op2 = wdf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
			Assert.assertNotNull(op2);
			Assert.assertFalse(op2.getStubObjectRelation());
			
			op2.setStubObjectRelation(true);
			wdf.getObjectPropertyDao().updateObjectProperty(op2);
			
			ObjectProperty op3 = wdf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
			Assert.assertNotNull(op3);
			Assert.assertTrue(op3.getStubObjectRelation());
			
			op3.setStubObjectRelation(false);
			wdf.getObjectPropertyDao().updateObjectProperty(op3);
			
			ObjectProperty op4 = wdf.getObjectPropertyDao().getObjectPropertyByURI(propURI);
			Assert.assertNotNull(op4);
			Assert.assertFalse(op4.getStubObjectRelation());
			
		} catch (InsertException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	// Test that the ObjectPropertyDaoJena::updateProperty method will only update the jena model for 
	// those properties in ObjectProperty that have a different value from what is already in the
	// jena model for that property. 
	//	
	// Specifically, updateProperty method should not remove a statement from the model and
	// then add the same statement back in. The reason for this is that in vitro the "immutable" properties
	// are stored in a sub-model and the user-editable properties are stored in a super-model and
	// all updates are performed against the super-model, so removing and then re-adding 
	// the same statement may result in a change of state (if the statement was in the sub-model 
	// it will migrate to the super-model) because of the way jena handles additions and
	// deletions with respect to super and sub models. This migration of statements may cause
	// undesirable behavior in the vitro application.
		
	public void minimalUpdates(){
	
		// 1. create two models and attach one as a sub-model of the other
		// 2. populate the sub-model with one statement for each of the properties represented in ObjectProperty
		// TODO still need to populate more of the properties, and set up some inverse and parent and inverse of
		// parent relationships in the test data.
		// 3. save the state of both the sub-model and the super-model
		// 4. populate an ObjectProperty object with the data in the (combined) model and call the updateProperty
		//    (having made no changes to the ObjectProperty object)
		// 5. verify that both the sub-model and the super-model are unchanged
		
		String propertyURI = "http://example.com/testObjectProp";

		OntModel superModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // this simulates the user-editable ontology in vivo
		OntModel subModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);   // this simulates the core ontology in vivo
		superModel.addSubModel(subModel);
		
		String rdfsLabel = "this is the rdfs label";
		String lang = "en-US";
		
		// populate sub-model
		com.hp.hpl.jena.ontology.ObjectProperty property1 = subModel.createObjectProperty(propertyURI); 
		
		property1.setLabel(rdfsLabel,lang);  
		
		property1.convertToTransitiveProperty();
		property1.convertToSymmetricProperty();
		property1.convertToFunctionalProperty();
		property1.convertToInverseFunctionalProperty();
		
		property1.setPropertyValue(RDFS.domain, subModel.createResource("http://thisIsTheDomainClassURI"));
		property1.setPropertyValue(RDFS.range, subModel.createResource("http://thisIsTheRangeClassURI"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.EXAMPLE_ANNOT), subModel.createTypedLiteral("this is the example"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.DESCRIPTION_ANNOT), subModel.createTypedLiteral("this is the description"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PUBLIC_DESCRIPTION_ANNOT), subModel.createTypedLiteral("this is the public description"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.DISPLAY_LIMIT), subModel.createTypedLiteral(6));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_ENTITYSORTFIELD), subModel.createTypedLiteral("this is the entity sort field"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_ENTITYSORTDIRECTION), subModel.createTypedLiteral("this is the entity sort direction"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.DISPLAY_RANK_ANNOT), subModel.createTypedLiteral(21));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_OBJECTINDIVIDUALSORTPROPERTY), subModel.createResource("http://thisIsTheObjectIndividualSortProperty"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT), subModel.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT), subModel.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_INPROPERTYGROUPANNOT), subModel.createResource("http://thisIsTheInPropertyGroupURI"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMENTRYFORMANNOT), subModel.createResource("http://thisIsTheCustomFormEntryURI"));
		property1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_SELECTFROMEXISTINGANNOT), subModel.createTypedLiteral(true));
		
		// Save copies of sub-model and super-model
		
		// uncommment the next two lines to debug failures
		//System.out.println("**Before updating data property:");		
		//printModels(superModel, subModel);		
		
		superModel.removeSubModel(subModel);
		
		OntModel origSubModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		origSubModel.add(subModel);
		OntModel origSuperModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		origSuperModel.add(superModel);
		
		superModel.addSubModel(subModel);
		
		// Populate the ObjectProperty with the data in the sub-model and then update the combined model
		// (from the unchanged object).
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena(superModel);
		ObjectPropertyDaoJena pdj = (ObjectPropertyDaoJena) wdfj.getObjectPropertyDao();
		ObjectProperty objectProperty = pdj.getObjectPropertyByURI(propertyURI); // the Property will be populated 
		                                                                         // with the information already in 
		                                                                         // the jena model.
		
		
       // check RDFS label here
		
        pdj.updateObjectProperty(objectProperty);       // we haven't changed any values here, so 
                                                        // the models should be unchanged.
 
        // Verify that the sub-model and super-model are both unchanged

        // uncommment the next two lines to debug failures
        //System.out.println("\n**After updating data property:");
        //printModels(superModel,subModel);
    
        superModel.removeSubModel(subModel);
        
		//modtime affects the diff but we don't care about that difference
		wipeOutModTime(origSubModel);
		wipeOutModTime(origSuperModel);
		wipeOutModTime(subModel);
		wipeOutModTime(superModel);
		
		Assert.assertTrue(subModel.isIsomorphicWith(origSubModel));	
	    Assert.assertTrue(superModel.isIsomorphicWith(origSuperModel));	    
	}
	
	
	void printModels(OntModel superModel, OntModel subModel) {

		// Detach the submodel for printing to get an accurate
		// account of what is in each.
		
	    superModel.removeSubModel(subModel);
	    
		System.out.println("\nThe sub-model has " + subModel.size() + " statements:");
		System.out.println("---------------------------------------------------");
		subModel.writeAll(System.out,"N3",null);

		System.out.println("\nThe super-model has " + superModel.size() + " statements:");
		System.out.println("---------------------------------------------------");
	    superModel.write(System.out,"N3",null);
		
	    superModel.addSubModel(subModel);
		
	}
	

	void wipeOutModTime(Model model){
		model.removeAll(null, model.createProperty(VitroVocabulary.MODTIME), null);
	}
		

}
