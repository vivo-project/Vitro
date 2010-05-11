package edu.cornell.mannlib.vitro.webapp.controller;

import org.junit.Assert;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;


public class EntityControllerTest {

	@Test
	public void testAcceptHeader(){
		EntityController entityController = new EntityController();
		
		/* Check to see if vitro would send RDF/XML to tabulator */
		String tabulatorsAcceptHeader =
			"text/xml,application/xml,application/xhtml+xml,text/html;q=0.5,text/plain;q=0.5," +
			"image/png,*/*;q=0.1," +
			"application/rdf+xml;q=1.0,text/n3;q=0.4";
		ContentType result = entityController.checkForLinkedDataRequest("http://notUsedInThisTestCase.com/bogus",tabulatorsAcceptHeader); 
		Assert.assertTrue( result != null );	
		Assert.assertTrue( "application/rdf+xml".equals( result.toString()) );
	}
}
