
/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import org.junit.Assert;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ApplicationDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.javax.servlet.http.HttpServletRequestStub;

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


    @Test
    public void testUtf8Encode(){
        UrlBuilder.contextPath = "/vivo";
        String path = "/individual";
        ParamMap params = new ParamMap();
        params.put("name", "\u2605Tom\u2605"); // \u2605 is Unicode for a five-pointed star.
        Assert.assertEquals("/vivo/individual?name=%E2%98%85Tom%E2%98%85", UrlBuilder.getUrl(path, params));
    }


    @Test
    public void testDecodeUtf8Url() {
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember\u2605"; // \u2605 is Unicode for a five-pointed star.
        String vClassUriEncoded = "http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember%E2%98%85";
        Assert.assertEquals(vClassUri, UrlBuilder.urlDecode(vClassUriEncoded));
    }


    @Test
    public void testGetIndividualProfileURI(){
        VitroRequest vreq = makeMockVitroRequest( "http://example.com/individual/");
        UrlBuilder.contextPath = "http://example.com";

        String uri = "http://example.com/individual/n2343";
        String url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
        Assert.assertEquals("http://example.com/display/n2343", url);

        uri = "http://example.com/individual/bob";
        url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
        Assert.assertEquals("http://example.com/display/bob",url);

        uri = "http://nondefaultNS.com/individual/n2343";
        url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
        Assert.assertEquals("http://example.com/individual?uri=" + URLEncoder.encode(uri), url);

        uri = "http://example.com/individual#n2343";
        url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
        Assert.assertEquals("http://example.com/individual?uri=" + URLEncoder.encode(uri), url);

        uri = "http://example.com/individual/5LNCannotStartWithNumber";
        url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
        Assert.assertEquals("http://example.com/individual?uri=" + URLEncoder.encode(uri), url);
    }

    protected VitroRequest makeMockVitroRequest( final String defaultNS){
        HttpServletRequestStub req = new HttpServletRequestStub();
        return new VitroRequest(req){

            @Override
            public String getParameter(String key){ return null; }

            @Override
            public WebappDaoFactory getWebappDaoFactory(){
                return makeMockWDF(defaultNS);
            }
        };
    }
    protected WebappDaoFactoryStub makeMockWDF( String defaultNS){
        WebappDaoFactoryStub wdf = new WebappDaoFactoryStub();
        wdf.setDefaultNamespace("http://example.com/individual/");
        ApplicationDaoStub aDao = new ApplicationDaoStub(){
            @Override
            public boolean isExternallyLinkedNamespace(String ns){
                return false;
            }
        };
        wdf.setApplicationDao( aDao );
        return wdf;
    }

    @Test
    public void testIsUriInDefaultNamespace(){
        String[][] examples = {
                { "http://example.com/individual/n3234", "http://example.com/individual/"},
                { "http://example.com/individual#n3234", "http://example.com/individual#"},
                { "http://example.com:8080/individual/n3234", "http://example.com:8080/individual/"},
                { "http://example.com:8080/individual#n3234", "http://example.com:8080/individual#"}
        };

        for( String[] example : examples ){
            Assert.assertTrue("expected '"+ example[0] + "' to be in the default NS of '"+example[1]+"'",
                    UrlBuilder.isUriInDefaultNamespace(example[0], example[1]));
        }

        String[][] counterExamples = {
                { "http://example.com/individual/5LNCannotStartWithNumber", "http://example.com/individual/" }
        };
        for( String[] example : counterExamples ){
            Assert.assertFalse("expected '"+ example[0] + "' to NOT be in the default NS of '"+example[1]+"'",
                    UrlBuilder.isUriInDefaultNamespace(example[0], example[1]));
        }
    }

}
