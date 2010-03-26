/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;


import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * 
 * 
 */

public class VClassDaoTest {
	
	@Test
	// Test that the VClassDaoJena::updateVClass method will only update the jena model for 
	// those properties in VClass that have a different value from what is already in the
	// jena model for that property. 
	//	
	// Specifically, VClass should not remove a statement from the model and then add the
	// same statement back in. The reason for this is that in vivo the "immutable" properties
	// are stored in a sub-model and the user-editable properties are stored in a super-model and
	// all updates are performed against the super-model, so removing and then re-adding 
	// the same statement may result in a change of state (if the statement was in the sub-model 
	// it will migrate to the super-model) because of the way jena handles additions and
	// deletions with respect to super and sub models. This migration of statements may cause
	// undesirable behavior in the vivo/vitro application.
	
	public void modelIsolation(){
	
		// 1. create two models and attach one as a sub-model of the other
		// 2. populate the sub-model with one statement for each of the 14 properties represented in VClass
		// 3. save the state of both the sub-model and the super-model
		// 4. populate a VClass object with the data in the (combined) model and call the updateVClass method
		// 5. verify that both the sub-model and the super-model are unchanged
		
		String class1URI = "http://test.vivo/AcademicDegree";

		OntModel superModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // this simulates the user-editable ontology in vivo
		OntModel subModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);   // this simulates the core ontology in vivo
		superModel.addSubModel(subModel);
		
		String rdfsLabel = "this is the rdfs label";
		String lang = "en-US";
		
		// populate sub-model
		OntClass class1 = subModel.createClass(class1URI);
		
		class1.setLabel(rdfsLabel,lang);   //rdfs:label		
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.IN_CLASSGROUP), subModel.createResource("http://thisIsTheClassGroupURI"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.SHORTDEF), subModel.createTypedLiteral("this is the short definition"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.EXAMPLE_ANNOT), subModel.createTypedLiteral("this is the example - why is this a string?"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.DESCRIPTION_ANNOT), subModel.createTypedLiteral("this is the description"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.DISPLAY_LIMIT), subModel.createTypedLiteral(-1));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.DISPLAY_RANK_ANNOT), subModel.createTypedLiteral(-11));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.SEARCH_BOOST_ANNOT), subModel.createTypedLiteral(2.4f));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT), subModel.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT), subModel.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMENTRYFORMANNOT), subModel.createTypedLiteral("this is the custom entry form annotation"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMDISPLAYVIEWANNOT), subModel.createTypedLiteral("this is the custom display view annotation"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMSHORTVIEWANNOT), subModel.createTypedLiteral("this is the custom short view annotation"));
		class1.setPropertyValue(subModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMSEARCHVIEWANNOT), subModel.createTypedLiteral("this is the custom search view annotation"));
	
		
		// Save copies of sub-model and super-model
		
		// uncommment the next two lines to debug failures
		//System.out.println("**Before updating VClass:");		
		//printModels(superModel, subModel);
		
		superModel.removeSubModel(subModel);
		
		OntModel origSubModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		origSubModel.add(subModel);
		OntModel origSuperModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		origSuperModel.add(superModel);
		
		superModel.addSubModel(subModel);
		
		// Populate the VClass with the data in the sub-model and then update the combined model
		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena(superModel);
		VClassDaoJena vcdj = (VClassDaoJena) wdfj.getVClassDao();
		VClass vClass = vcdj.getVClassByURI(class1URI);           // the VClass will be populated with the
		                                                          // information already in the jena model.
		
		
		Assert.assertEquals(vClass.getName(), class1.getLabel(lang));  //
		
		
        vcdj.updateVClass(vClass);                                // we haven't changed any values here, so 
                                                                  // the models should be unchanged.
 
        // Verify that the sub-model and super-model are both unchanged

        // uncommment the next two lines to debug failures
        //System.out.println("\n**After updating VClass:");
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
