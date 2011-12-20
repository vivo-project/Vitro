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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

/**
 * @author bdc34
 *
 */
public class ThumbnailImageURLTest extends AbstractTestClass{
    OntModel testModel;
    String personsURI = "http://vivo.cornell.edu/individual/individual8803";
    
    static VitroSearchTermNames term = new VitroSearchTermNames();
    String fieldForThumbnailURL = term.THUMBNAIL_URL;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

        Model model = ModelFactory.createDefaultModel();        
        InputStream in = ThumbnailImageURLTest.class.getResourceAsStream("testPerson.n3");
        model.read(in,"","N3");        
        testModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,model);
    }

    /**
     * Test method for {@link edu.cornell.mannlib.vitro.webapp.search.solr.ThumbnailImageURL#modifyDocument(edu.cornell.mannlib.vitro.webapp.beans.Individual, org.apache.solr.common.SolrInputDocument, java.lang.StringBuffer)}.
     */
    @Test
    public void testModifyDocument() {
        SolrInputDocument doc = new SolrInputDocument();
        ThumbnailImageURL testMe = new ThumbnailImageURL( testModel );
        Individual ind = new IndividualImpl();
        ind.setURI(personsURI);
        try {
            testMe.modifyDocument(ind, doc, null);
        } catch (SkipIndividualException e) {
                Assert.fail("person was skipped: " + e.getMessage());
        }
                
        SolrInputField thumbnailField = doc.getField(fieldForThumbnailURL);
        Assert.assertNotNull(thumbnailField);

        Assert.assertNotNull( thumbnailField.getValues() );
        Assert.assertEquals(1, thumbnailField.getValueCount());
        
        Assert.assertEquals("http://vivo.cornell.edu/individual/n54945", thumbnailField.getFirstValue());
    }

}
