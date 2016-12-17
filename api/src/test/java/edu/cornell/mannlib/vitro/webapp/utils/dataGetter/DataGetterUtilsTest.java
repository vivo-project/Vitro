/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.http.HttpServletRequestStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class DataGetterUtilsTest extends AbstractTestClass{
    
    OntModel displayModel;
    VitroRequest vreq;
    String testDataGetterURI_1 = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#query1data";
    String pageURI_1 = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#SPARQLPage";
    String pageX = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#pageX";
    String dataGetterX = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#pageDataGetterX";
    
    @Before
    public void setUp() throws Exception {    
        // Suppress error logging.
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);
        
        Model model = ModelFactory.createDefaultModel();        
        InputStream in = DataGetterUtilsTest.class.getResourceAsStream("resources/dataGetterTest.n3");
        model.read(in,"","N3");        
        displayModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,model);
        
        vreq = new VitroRequest(new HttpServletRequestStub());
    }

    @Test
    public void testGetJClassForDataGetterURI() throws IllegalAccessException {
        String fullJavaClassName = DataGetterUtils.getJClassForDataGetterURI(displayModel, testDataGetterURI_1);
        Assert.assertNotNull(fullJavaClassName);
        Assert.assertTrue("java class name should not be empty", ! StringUtils.isEmpty(fullJavaClassName));
        Assert.assertEquals(SparqlQueryDataGetter.class.getName(), fullJavaClassName);
    }

    @Test
    public void testDataGetterForURI() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        DataGetter dg = DataGetterUtils.dataGetterForURI(vreq, displayModel, testDataGetterURI_1);
        Assert.assertNotNull(dg);
    }
    
    @Test
    public void testGetDataGettersForPage() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        List<DataGetter> dgList = 
            DataGetterUtils.getDataGettersForPage(vreq, displayModel, pageURI_1);
        Assert.assertNotNull(dgList);
        Assert.assertTrue("List of DataGetters was empty, it should not be.", dgList.size() > 0);
    }


    

}
