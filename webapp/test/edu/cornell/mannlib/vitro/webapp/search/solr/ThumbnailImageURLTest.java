/* $This file is distributed under the terms of the license in /doc/license.txt$ */
/**
 * 
 */
package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.io.InputStream;

import org.apache.log4j.Level;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.SkipIndividualException;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ThumbnailImageURL;

public class ThumbnailImageURLTest extends AbstractTestClass{
    RDFServiceFactory testRDF;
    String personsURI = "http://vivo.cornell.edu/individual/individual8803";
        
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

        Model model = ModelFactory.createDefaultModel();        
        InputStream in = ThumbnailImageURLTest.class.getResourceAsStream("testPerson.n3");
        model.read(in,"","N3");        
        testRDF = new RDFServiceFactorySingle( new RDFServiceModel( model ) );            
    }

    /**
     * Test to see if ThumbnailImageURL gets the date it is suppose to gete
     * from a set of RDF.
     * 
     * Test method for {@link edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ThumbnailImageURL#modifyDocument(edu.cornell.mannlib.vitro.webapp.beans.Individual, org.apache.solr.common.SolrInputDocument, java.lang.StringBuffer)}.
     */
    @Test
    public void testThumbnailFieldCreatedInSolrDoc() {
        SolrInputDocument doc = new SolrInputDocument();
        ThumbnailImageURL testMe = new ThumbnailImageURL( testRDF );
        Individual ind = new IndividualImpl();
        ind.setURI(personsURI);
        
        //make sure that the person is in the RDF
        try {
            testMe.modifyDocument(ind, doc, null);
        } catch (SkipIndividualException e) {
                Assert.fail("Test individual was skipped by classes that build the search document: " + e.getMessage());
        }

        //make sure that a Solr document field got created for the thumbnail image
        
        SolrInputField thumbnailField = doc.getField( VitroSearchTermNames.THUMBNAIL_URL );
        Assert.assertNotNull(thumbnailField);

        Assert.assertNotNull( thumbnailField.getValues() );
        Assert.assertEquals(1, thumbnailField.getValueCount());
        
        Assert.assertEquals("http://vivo.cornell.edu/individual/n54945", thumbnailField.getFirstValue());
    }

}
