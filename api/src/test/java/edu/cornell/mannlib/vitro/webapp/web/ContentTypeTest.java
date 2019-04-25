/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.Map;

import org.junit.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualController;


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

        Map<String,Float> serverTypes = IndividualController.ACCEPTED_CONTENT_TYPES;

        Assert.assertEquals("application/rdf+xml", ContentType.getBestContentType(clientAccepts, serverTypes));
    }

    /**
     * Modified this, added q-factor to text/html, because otherwise the result is indeterminate, and the
     * test fails in Java 8.
     */
    @Test
    public void testWeightedBestContentTypeForFirefox(){
        //accept header from normal firefox
        Map<String,Float> clientAccepts = ContentType.getTypesAndQ(
        "text/html;q=0.95,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        Map<String,Float> serverTypes = IndividualController.ACCEPTED_CONTENT_TYPES;

        Assert.assertEquals("application/xhtml+xml", ContentType.getBestContentType(clientAccepts, serverTypes));
    }
}
