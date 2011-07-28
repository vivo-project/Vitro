/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualController;


public class ContentTypeTest {

    @Test
    public void typeAndQTest1(){
        Map<String,Float> map = ContentType.getTypesAndQ(
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8,application/rdf+xml;q=0.93,text/rdf+n3;q=0.5");
        Assert.assertEquals(1.0f, map.get("text/html"), 0.01f);
        Assert.assertEquals(1.0f, map.get("application/xhtml+xml"), 0.01f);
        Assert.assertEquals(0.9f, map.get("application/xml"), 0.01f);
        Assert.assertEquals(0.93f,map.get("application/rdf+xml"), 0.01f);
        Assert.assertEquals(0.5f, map.get("text/rdf+n3"), 0.01f);
        Assert.assertEquals(0.8f,map.get("*/*"), 0.01f);
    }
    
    @Test
    public void typeAndQTest2(){
        Map<String,Float> map = ContentType.getTypesAndQ(
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        Assert.assertEquals(1.0f, map.get("text/html"), 0.01f);
        Assert.assertEquals(1.0f, map.get("application/xhtml+xml"), 0.01f);
        Assert.assertEquals(0.9f, map.get("application/xml"), 0.01f);
        Assert.assertEquals(0.8f,map.get("*/*"), 0.01f);               
    }
    
    @Test
    public void testWeightedBestContentTypeForTabulator(){
        //accept header from tabulator
        Map<String,Float> clientAccepts = ContentType.getTypesAndQ(
        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8,application/rdf+xml;q=0.93,text/rdf+n3;q=0.5");
        
        Map<String,Float> serverTypes = IndividualController.getAcceptedContentTypes();
        
        Assert.assertEquals("application/rdf+xml", ContentType.getBestContentType(clientAccepts, serverTypes)); 
    }
    
    @Test
    public void testWeightedBestContentTypeForFirefox(){
        //accept header from normal firefox
        Map<String,Float> clientAccepts = ContentType.getTypesAndQ(
        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        
        Map<String,Float> serverTypes = IndividualController.getAcceptedContentTypes();
        
        Assert.assertEquals("application/xhtml+xml", ContentType.getBestContentType(clientAccepts, serverTypes)); 
    }
}
