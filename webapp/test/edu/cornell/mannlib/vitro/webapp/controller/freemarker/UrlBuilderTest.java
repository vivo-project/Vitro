/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;

public class UrlBuilderTest extends AbstractTestClass {
    
    @Test
    public void testGetUrl() {
        UrlBuilder.contextPath = "/vivo";
        
        String path1 = "/individual";
        Assert.assertEquals("/vivo/individual", UrlBuilder.getUrl(path1));
        
        int portalId = 1;
        String path2 = "/individual?home=" + portalId;
        Assert.assertEquals("/vivo/individual?home=1", UrlBuilder.getUrl(path2));
    }
    
    @Test
    public void testGetUrlWithEmptyContext() {
        UrlBuilder.contextPath = "";
        String path = "/individual";
        Assert.assertEquals(path, UrlBuilder.getUrl(path));
    }
    
    @Test
    public void testGetUrlWithParams() {
        UrlBuilder.contextPath = "/vivo";
        String path = "/individual";
        ParamMap params = new ParamMap();
        int portalId = 1;
        params.put("home", "" + portalId);
        params.put("name", "Tom");
        Assert.assertEquals("/vivo/individual?home=1&name=Tom", UrlBuilder.getUrl(path, params));
    }

    @Test
    public void testEncodeUrl() {
        UrlBuilder.contextPath = "/vivo";
        String path = "/individuallist";
        ParamMap params = new ParamMap();
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember";
        params.put("vclassId", vClassUri);
        Assert.assertEquals("/vivo/individuallist?vclassId=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember", UrlBuilder.getUrl(path, params));    
    }
    
    @Test
    public void testDecodeUrl() {
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember";
        String vClassUriEncoded = "http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember";
        Assert.assertEquals(vClassUri, UrlBuilder.urlDecode(vClassUriEncoded));          
    }
    
}
