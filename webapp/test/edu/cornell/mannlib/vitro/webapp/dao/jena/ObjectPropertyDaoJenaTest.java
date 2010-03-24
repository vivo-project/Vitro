/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


public class ObjectPropertyDaoJenaTest {

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
}
