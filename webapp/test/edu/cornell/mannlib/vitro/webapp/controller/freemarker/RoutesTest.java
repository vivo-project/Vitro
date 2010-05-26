/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import freemarker.template.Configuration;

public class RoutesTest extends AbstractTestClass {
    
    @Test
    public void testGetUrl() {
        Routes.contextPath = "/vivo";
        
        String path1 = "/individual";
        Assert.assertEquals("/vivo/individual", Routes.getUrl(path1));
        
        int portalId = 1;
        String path2 = "/individual?home=" + portalId;
        Assert.assertEquals("/vivo/individual?home=1", Routes.getUrl(path2));
    }
    
    @Test
    public void testGetUrlWithEmptyContext() {
        Routes.contextPath = "";
        String path = "/individual";
        Assert.assertEquals(path, Routes.getUrl(path));
    }
    
    @Test
    public void testGetUrlWithParams() {
        Routes.contextPath = "/vivo";
        String path = "/individual";
        Map<String, String> params = new HashMap<String, String>();
        int portalId = 1;
        params.put("home", "" + portalId);
        params.put("name", "Tom");
        Assert.assertEquals("/vivo/individual?home=1&name=Tom", Routes.getUrl(path, params));
    }

    @Test
    public void testEncodeUrl() {
        Routes.contextPath = "/vivo";
        String path = "/individuallist";
        Map<String, String> params = new HashMap<String, String>();
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember";
        params.put("vclassId", vClassUri);
        Assert.assertEquals("/vivo/individuallist?vclassId=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember", Routes.getUrl(path, params));    
    }
    
    @Test
    public void testDecodeUrl() {
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember";
        String vClassUriEncoded = "http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember";
        Assert.assertEquals(vClassUri, Routes.urlDecode(vClassUriEncoded));          
    }
    
}
