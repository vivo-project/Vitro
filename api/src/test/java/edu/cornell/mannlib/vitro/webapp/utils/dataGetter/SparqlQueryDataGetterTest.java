/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.http.HttpServletRequestStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class SparqlQueryDataGetterTest extends AbstractTestClass{
    
    OntModel displayModel;
    String testDataGetterURI_1 = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#query1data";
    WebappDaoFactory wdf;
    VitroRequest vreq;
    
    @Before
    public void setUp() throws Exception {
        // Suppress error logging.
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

        OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM);        
        InputStream in = SparqlQueryDataGetterTest.class.getResourceAsStream("resources/dataGetterTest.n3");
        model.read(in,"","N3");        
        displayModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,model);
        
        SimpleOntModelSelector sos = new SimpleOntModelSelector( ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM));
        sos.setDisplayModel(displayModel);            
        wdf = new WebappDaoFactoryJena(sos);    
        
        vreq = new VitroRequest(new HttpServletRequestStub());
    }

    @Test
    public void testBasicGetData() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        DataGetter dg = DataGetterUtils.dataGetterForURI(vreq, displayModel, testDataGetterURI_1);
        Assert.assertNotNull(dg);
        Assert.assertTrue(
                "DataGetter should be of type " + SparqlQueryDataGetter.class.getName(),
                dg instanceof SparqlQueryDataGetter);
        
        SparqlQueryDataGetter sdg = (SparqlQueryDataGetter)dg;
        
        
        Model dataModel = ModelFactory.createDefaultModel();
        String bobURI = "http://example.com/p/bob";
        dataModel.add(ResourceFactory.createResource(bobURI), RDF.type, ResourceFactory.createResource("http://xmlns.com/foaf/0.1/Person"));
        
        Map<String, String> params = Collections.emptyMap();
        
        Map<String,Object> mapOut = sdg.doQueryOnModel(sdg.queryText, dataModel);
        
        Assert.assertNotNull(mapOut);
        Assert.assertTrue("should contain key people" , mapOut.containsKey("people"));
        
        Object obj = mapOut.get("people");        
        Assert.assertTrue("people should be a List, it is " + obj.getClass().getName(), obj instanceof List);        
        List people = (List)obj;
        
        Assert.assertEquals(1, people.size());
        
        Map<String,String> first = (Map<String, String>) people.get(0);
        Assert.assertEquals(bobURI, first.get("uri"));
    }

}
