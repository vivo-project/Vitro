/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.http.HttpServletRequestStub;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class PageDataGetterUtilsTest extends AbstractTestClass{
    OntModel displayModel;
    WebappDaoFactory wdf;
    
    String pageURI = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#pageX";
    String pageURI_2 = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#SPARQLPage";
    
    @Before
    public void setUp() throws Exception {
        // Suppress error logging.
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM);        
        InputStream in = PageDataGetterUtilsTest.class.getResourceAsStream("resources/pageDataGetter.n3");
        model.read(in,"","N3");        
        displayModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,model);
        
        SimpleOntModelSelector sos = new SimpleOntModelSelector( ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM));
        sos.setDisplayModel(displayModel);
        
        wdf = new WebappDaoFactoryJena(sos);
    }

    @Test
    public void testGetPageDataGetterObjects() throws Exception{
        VitroRequest vreq = new VitroRequest( new HttpServletRequestStub() );
        ModelAccess.on(vreq).setWebappDaoFactory(wdf);
        
        List<PageDataGetter> pdgList = PageDataGetterUtils.getPageDataGetterObjects(vreq, pageURI);
        Assert.assertNotNull(pdgList);
        Assert.assertTrue("should have one PageDataGetter", pdgList.size() == 1);
    }

    @Test
    public void testGetNonPageDataGetterObjects() throws Exception{
        VitroRequest vreq = new VitroRequest( new HttpServletRequestStub() );
        ModelAccess.on(vreq).setWebappDaoFactory(wdf);
        
        List<PageDataGetter> pdgList = PageDataGetterUtils.getPageDataGetterObjects(vreq, pageURI_2);
        Assert.assertNotNull(pdgList);
        Assert.assertTrue("should have no PageDataGetters", pdgList.size() == 0);
    }
}
