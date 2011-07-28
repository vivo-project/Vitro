/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletResponse;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;


public class URLRewritingHttpServletResponseTest {

    /*
     * Style A is for sites that are running at a URL like http://localhost:8080/vivo
     * with no portals.
     */
    protected void urlEncodingStyleA(String urlToEncode, String expectedUrlResult ){
        URLRewritingHttpServletResponse urhsr = new URLRewritingHttpServletResponse(new stubs.javax.servlet.http.HttpServletResponseStub());
        
        List<String>externalNamespaces = new ArrayList();
        externalNamespaces.add("http://vivo.med.cornell.edu/individual/");        
                
        String actual = urhsr.encodeForVitro(urlToEncode, "UTF-8",  
                true, 1, 
                getMockNamespaceMapper(), 
                "http://vivo.cornell.edu/individual/", 
                externalNamespaces);
        Assert.assertEquals(expectedUrlResult, actual);
    }      
    
    /*
     * Style A is for sites that are running behind apache httpd at a 
     * URL like http://caruso.mannlib.cornell.edu/ with no portals.
     */
    protected void urlEncodingStyleB(String urlToEncode, String expectedUrlResult){
        URLRewritingHttpServletResponse urhsr = new URLRewritingHttpServletResponse(new stubs.javax.servlet.http.HttpServletResponseStub());
        
        List<String>externalNamespaces = new ArrayList();
        externalNamespaces.add("http://vivo.med.cornell.edu/individual/");        
                
        String actual = urhsr.encodeForVitro(urlToEncode, "UTF-8", 
                true, 0, 
                getMockNamespaceMapper(), 
                "http://vivo.cornell.edu/individual/", 
                externalNamespaces);
        Assert.assertEquals(expectedUrlResult, actual);
    }
    
    
    @Test
    public void test40984(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }

    @Test
    public void test40988(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/getURLParam.js",
    "/vivo/js/jquery_plugins/getURLParam.js"); }
    @Test
    public void test40994(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/colorAnimations.js",
    "/vivo/js/jquery_plugins/colorAnimations.js"); }
    @Test
    public void test40995(){ urlEncodingStyleA(
    "/vivo/js/propertyGroupSwitcher.js",
    "/vivo/js/propertyGroupSwitcher.js"); }
    @Test
    public void test40996(){ urlEncodingStyleA( "/vivo/js/controls.js",
    "/vivo/js/controls.js"); }
    @Test
    public void test40999(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.form.js",
    "/vivo/js/jquery_plugins/jquery.form.js"); }
    @Test
    public void test41004(){ urlEncodingStyleA(
    "/vivo/js/tiny_mce/tiny_mce.js", "/vivo/js/tiny_mce/tiny_mce.js"); }
    @Test
    public void test41133(){ urlEncodingStyleA(
    "/vivo/entityEdit?uri=http%3a%2f%2fbogus.com%2findividual%2fn3671",
    "/vivo/entityEdit?uri=http%3A%2F%2Fbogus.com%2Findividual%2Fn3671"); }
    @Test
    public void test41464(){ urlEncodingStyleA(
    "/vivo/themes/vivo-basic/site_icons/visualization/ajax-loader.gif",
    "/vivo/themes/vivo-basic/site_icons/visualization/ajax-loader.gif"); }
    @Test
    public void test41465(){ urlEncodingStyleA(
    "/vivo/visualization?render_mode=dynamic&container=vis_container&vis=person_pub_count&vis_mode=short&uri=http%3a%2f%2fbogus.com%2findividual%2fn3671",
    "/vivo/visualization?render_mode=dynamic&container=vis_container&vis=person_pub_count&vis_mode=short&uri=http%3A%2F%2Fbogus.com%2Findividual%2Fn3671");
    }
    @Test
    public void test42110(){ urlEncodingStyleA(
    "/vivo/js/imageUpload/imageUploadUtils.js",
    "/vivo/js/imageUpload/imageUploadUtils.js"); }
    @Test
    public void test57982(){ urlEncodingStyleA(
    "entityEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fAgent",
    "entityEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FAgent");
    }
    @Test
    public void test57983(){ urlEncodingStyleA(
    "/vivo/vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fAgent",
    "/vivo/vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FAgent"); }
    @Test
    public void test57986(){ urlEncodingStyleA(
    "entityEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "entityEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson");
    }
    @Test
    public void test57987(){ urlEncodingStyleA(
    "/vivo/vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "/vivo/vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson");
    }
    @Test
    public void test57988(){ urlEncodingStyleA(
    "entityEdit?uri=http%3a%2f%2fwww.w3.org%2f2002%2f07%2fowl%23Thing",
    "entityEdit?uri=http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23Thing");
    }
    @Test
    public void test57989(){ urlEncodingStyleA(
    "/vivo/vclassEdit?uri=http%3a%2f%2fwww.w3.org%2f2002%2f07%2fowl%23Thing",
    "/vivo/vclassEdit?uri=http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23Thing");
    }
    @Test
    public void test42083(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23Address",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23Address");
    }
    @Test
    public void test42084(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23DateTimeInterval",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23DateTimeInterval");
    }
    @Test
    public void test42085(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23URLLink",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23URLLink");
    }
    @Test
    public void test42086(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23AcademicDegree",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23AcademicDegree");
    }
    @Test
    public void test42087(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fpurl.org%2fontology%2fbibo%2fDocumentStatus",
    "vclassEdit?uri=http%3A%2F%2Fpurl.org%2Fontology%2Fbibo%2FDocumentStatus");
    }
    @Test
    public void test42088(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23DateTimeValuePrecision",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23DateTimeValuePrecision");
    }
    @Test
    public void test42089(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23DateTimeValue",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23DateTimeValue");
    }
    @Test
    public void test42090(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23Award",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23Award");
    }
    @Test
    public void test42091(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23Authorship",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23Authorship");
    }
    @Test
    public void test48256(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson");
    }
    @Test
    public void test11309(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7df6",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7df6");
    }
    @Test
    public void test11310(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7e02",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7e02");
    }
    @Test
    public void test11311(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7e09",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7e09");
    }
    @Test
    public void test11312(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7df5",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7df5");
    }
    @Test
    public void test11313(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7df4",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7df4");
    }
    @Test
    public void test11314(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7df9",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7df9");
    }
    @Test
    public void test11315(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7df8",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7df8");
    }
    @Test
    public void test11317(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-43882498%3a12c1825c819%3a-7df7",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-43882498%3A12c1825c819%3A-7df7");
    }
    @Test
    public void test11318(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fAgent",
    "vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FAgent");
    }
    @Test
    public void test11319(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23Librarian",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23Librarian");
    }
    @Test
    public void test11320(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23Student",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23Student");
    }
    @Test
    public void test11321(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23NonAcademic",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23NonAcademic");
    }
    @Test
    public void test11322(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23NonFacultyAcademic",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23NonFacultyAcademic");
    }
    @Test
    public void test113222(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23FacultyMember",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember");
    }
    @Test
    public void test11323(){ urlEncodingStyleA(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23EmeritusProfessor",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23EmeritusProfessor");
    }
    @Test
    public void test53543(){ urlEncodingStyleA(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-2",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-2");
    }
    @Test
    public void test53549(){ urlEncodingStyleA(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-inf",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-inf");
    }
    @Test
    public void test53555(){ urlEncodingStyleA(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-userAccounts",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-userAccounts");
    }
    @Test
    public void test53557(){ urlEncodingStyleA(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-displayMetadata",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-displayMetadata");
    }
    @Test
    public void test391499(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customForm.css",
    "/vivo/edit/forms/css/customForm.css"); }
    @Test
    public void test39149(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test39153(){ urlEncodingStyleA(
    "/vivo/edit/processRdfForm2.jsp", "/vivo/edit/processRdfForm2.jsp"); }
    @Test
    public void test39472(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test394730(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test39473(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test39474(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test39475(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }
    @Test
    public void test14958(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test14968(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/getURLParam.js",
    "/vivo/js/jquery_plugins/getURLParam.js"); }
    @Test
    public void test14972(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/colorAnimations.js",
    "/vivo/js/jquery_plugins/colorAnimations.js"); }
    @Test
    public void test14979(){ urlEncodingStyleA(
    "/vivo/js/propertyGroupSwitcher.js",
    "/vivo/js/propertyGroupSwitcher.js"); }
    @Test
    public void test14980(){ urlEncodingStyleA( "/vivo/js/controls.js",
    "/vivo/js/controls.js"); }
    @Test
    public void test14982(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.form.js",
    "/vivo/js/jquery_plugins/jquery.form.js"); }
    @Test
    public void test14986(){ urlEncodingStyleA(
    "/vivo/js/tiny_mce/tiny_mce.js", "/vivo/js/tiny_mce/tiny_mce.js"); }
    @Test
    public void test14999(){ urlEncodingStyleA(
    "/vivo/entityEdit?uri=http%3a%2f%2fbogus.com%2findividual%2fn3671",
    "/vivo/entityEdit?uri=http%3A%2F%2Fbogus.com%2Findividual%2Fn3671"); }
    @Test
    public void test15011(){ urlEncodingStyleA(
    "/vivo/themes/vivo-basic/site_icons/visualization/ajax-loader.gif",
    "/vivo/themes/vivo-basic/site_icons/visualization/ajax-loader.gif"); }
    @Test
    public void test15014(){ urlEncodingStyleA(
    "/vivo/visualization?render_mode=dynamic&container=vis_container&vis=person_pub_count&vis_mode=short&uri=http%3a%2f%2fbogus.com%2findividual%2fn3671",
    "/vivo/visualization?render_mode=dynamic&container=vis_container&vis=person_pub_count&vis_mode=short&uri=http%3A%2F%2Fbogus.com%2Findividual%2Fn3671");
    }
    @Test
    public void test15143(){ urlEncodingStyleA(
    "/vivo/js/imageUpload/imageUploadUtils.js",
    "/vivo/js/imageUpload/imageUploadUtils.js"); }
    @Test
    public void test184670(){ urlEncodingStyleA(
    "/vivo/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css",
    "/vivo/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css"); }
    @Test
    public void test18467(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customForm.css",
    "/vivo/edit/forms/css/customForm.css"); }
    @Test
    public void test184680(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customFormWithAutocomplete.css",
    "/vivo/edit/forms/css/customFormWithAutocomplete.css"); }
    @Test
    public void test18468(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test18472(){ urlEncodingStyleA(
    "/vivo/edit/processRdfForm2.jsp", "/vivo/edit/processRdfForm2.jsp"); }
    @Test
    public void test18506(){ urlEncodingStyleA( "/vivo/individual?uri=",
    "/vivo/individual?uri="); }
    @Test
    public void test18512(){ urlEncodingStyleA(
    "/vivo/autocomplete?tokenize=true&stem=true",
    "/vivo/autocomplete?tokenize=true&stem=true"); }
    @Test
    public void test18516(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test18543(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test185440(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test18544(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test18545(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }
    @Test
    public void test18546(){ urlEncodingStyleA(
    "/vivo/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js",
    "/vivo/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"); }
    @Test
    public void test185470(){ urlEncodingStyleA(
    "/vivo/js/customFormUtils.js", "/vivo/js/customFormUtils.js"); }
    @Test
    public void test18547(){ urlEncodingStyleA(
    "/vivo/edit/forms/js/customFormWithAutocomplete.js",
    "/vivo/edit/forms/js/customFormWithAutocomplete.js"); }
    @Test
    public void test27127(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test27130(){ urlEncodingStyleA(
    "/vivo/edit/processDatapropRdfForm.jsp",
    "/vivo/edit/processDatapropRdfForm.jsp"); }
    @Test
    public void test271590(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test27159(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test27160(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test27161(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test27166(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }
    @Test
    public void test14842(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test14846(){ urlEncodingStyleA(
    "/vivo/edit/processDatapropRdfForm.jsp",
    "/vivo/edit/processDatapropRdfForm.jsp"); }
    @Test
    public void test148510(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test14851(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test14852(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test148530(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test14853(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }
    @Test
    public void test43748(){ urlEncodingStyleA(
    "/vivo/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css",
    "/vivo/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css"); }
    @Test
    public void test43749(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customForm.css",
    "/vivo/edit/forms/css/customForm.css"); }
    @Test
    public void test437500(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customFormWithAutocomplete.css",
    "/vivo/edit/forms/css/customFormWithAutocomplete.css"); }
    @Test
    public void test43750(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test437540(){ urlEncodingStyleA(
    "/vivo/edit/processRdfForm2.jsp", "/vivo/edit/processRdfForm2.jsp"); }
    @Test
    public void test43754(){ urlEncodingStyleA( "/vivo/individual?uri=",
    "/vivo/individual?uri="); }
    @Test
    public void test43757(){ urlEncodingStyleA(
    "/vivo/autocomplete?tokenize=true&stem=true",
    "/vivo/autocomplete?tokenize=true&stem=true"); }
    @Test
    public void test43760(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test437610(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test43761(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test43762(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test437630(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }
    @Test
    public void test43763(){ urlEncodingStyleA(
    "/vivo/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js",
    "/vivo/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"); }
    @Test
    public void test437640(){ urlEncodingStyleA(
    "/vivo/js/customFormUtils.js", "/vivo/js/customFormUtils.js"); }
    @Test
    public void test43764(){ urlEncodingStyleA(
    "/vivo/edit/forms/js/customFormWithAutocomplete.js",
    "/vivo/edit/forms/js/customFormWithAutocomplete.js"); }
    @Test
    public void test14550(){ urlEncodingStyleA(
    "/vivo/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css",
    "/vivo/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css"); }
    @Test
    public void test14551(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customForm.css",
    "/vivo/edit/forms/css/customForm.css"); }
    @Test
    public void test1455200(){ urlEncodingStyleA(
    "/vivo/edit/forms/css/customFormWithAutocomplete.css",
    "/vivo/edit/forms/css/customFormWithAutocomplete.css"); }
    @Test
    public void test14552(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test14556(){ urlEncodingStyleA(
    "/vivo/edit/processRdfForm2.jsp", "/vivo/edit/processRdfForm2.jsp"); }
    @Test
    public void test14557(){ urlEncodingStyleA( "/vivo/individual?uri=",
    "/vivo/individual?uri="); }
    @Test
    public void test145610(){ urlEncodingStyleA(
    "/vivo/autocomplete?tokenize=true&stem=true",
    "/vivo/autocomplete?tokenize=true&stem=true"); }
    @Test
    public void test14561(){ urlEncodingStyleA( "/vivo/admin/sparqlquery",
    "/vivo/admin/sparqlquery"); }
    @Test
    public void test14565(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test145650(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test145660(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test14566(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test14567(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }
    @Test
    public void test145680(){ urlEncodingStyleA(
    "/vivo/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js",
    "/vivo/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"); }
    @Test
    public void test14568(){ urlEncodingStyleA(
    "/vivo/js/customFormUtils.js", "/vivo/js/customFormUtils.js"); }
    @Test
    public void test145690(){ urlEncodingStyleA(
    "/vivo/js/browserUtils.js", "/vivo/js/browserUtils.js"); }
    @Test
    public void test14569(){ urlEncodingStyleA(
    "/vivo/edit/forms/js/customFormWithAutocomplete.js",
    "/vivo/edit/forms/js/customFormWithAutocomplete.js"); }
    @Test
    public void test29078(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox.css",
    "/vivo/js/jquery_plugins/thickbox/thickbox.css"); }
    @Test
    public void test29081(){ urlEncodingStyleA(
    "/vivo/edit/processDatapropRdfForm.jsp",
    "/vivo/edit/processDatapropRdfForm.jsp"); }
    @Test
    public void test29084(){ urlEncodingStyleA(
    "/vivo/js/extensions/String.js", "/vivo/js/extensions/String.js"); }
    @Test
    public void test29085(){ urlEncodingStyleA( "/vivo/js/jquery.js",
    "/vivo/js/jquery.js"); }
    @Test
    public void test290860(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js",
    "/vivo/js/jquery_plugins/jquery.bgiframe.pack.js"); }
    @Test
    public void test29086(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js",
    "/vivo/js/jquery_plugins/thickbox/thickbox-compressed.js"); }
    @Test
    public void test29087(){ urlEncodingStyleA(
    "/vivo/js/jquery_plugins/ui.datepicker.js",
    "/vivo/js/jquery_plugins/ui.datepicker.js"); }

    
    

    @Test
    public void test35560(){ urlEncodingStyleB( "/js/jquery.js",
    "/js/jquery.js"); }
    @Test
    public void test35562(){ urlEncodingStyleB(
    "/js/jquery_plugins/getURLParam.js",
    "/js/jquery_plugins/getURLParam.js"); }
    @Test
    public void test35564(){ urlEncodingStyleB(
    "/js/jquery_plugins/colorAnimations.js",
    "/js/jquery_plugins/colorAnimations.js"); }
    @Test
    public void test35568(){ urlEncodingStyleB(
    "/js/propertyGroupSwitcher.js", "/js/propertyGroupSwitcher.js"); }
    @Test
    public void test35617(){ urlEncodingStyleB( "/js/controls.js",
    "/js/controls.js"); }
    @Test
    public void test35618(){ urlEncodingStyleB(
    "/js/jquery_plugins/jquery.form.js",
    "/js/jquery_plugins/jquery.form.js"); }
    @Test
    public void test356180(){ urlEncodingStyleB(
    "/js/tiny_mce/tiny_mce.js", "/js/tiny_mce/tiny_mce.js"); }
    @Test
    public void test37150(){ urlEncodingStyleB(
    "/entityEdit?uri=http%3a%2f%2fbogus.com%2findividual%2fn3671",
    "/entityEdit?uri=http%3A%2F%2Fbogus.com%2Findividual%2Fn3671"); }
    @Test
    public void test37402(){ urlEncodingStyleB(
    "/themes/vivo-basic/site_icons/visualization/ajax-loader.gif",
    "/themes/vivo-basic/site_icons/visualization/ajax-loader.gif"); }
    @Test
    public void test37403(){ urlEncodingStyleB(
    "/visualization?render_mode=dynamic&container=vis_container&vis=person_pub_count&vis_mode=short&uri=http%3a%2f%2fbogus.com%2findividual%2fn3671",
    "/visualization?render_mode=dynamic&container=vis_container&vis=person_pub_count&vis_mode=short&uri=http%3A%2F%2Fbogus.com%2Findividual%2Fn3671");
    }
    @Test
    public void test38667(){ urlEncodingStyleB(
    "/js/imageUpload/imageUploadUtils.js",
    "/js/imageUpload/imageUploadUtils.js"); }
    @Test
    public void test47087(){ urlEncodingStyleB(
    "entityEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fAgent",
    "entityEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FAgent");
    }
    @Test
    public void test47088(){ urlEncodingStyleB(
    "/vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fAgent",
    "/vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FAgent"); }
    @Test
    public void test470910(){ urlEncodingStyleB(
    "entityEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "entityEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson");
    }
    @Test
    public void test47091(){ urlEncodingStyleB(
    "/vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "/vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson"); }
    @Test
    public void test470930(){ urlEncodingStyleB(
    "entityEdit?uri=http%3a%2f%2fwww.w3.org%2f2002%2f07%2fowl%23Thing",
    "entityEdit?uri=http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23Thing");
    }
    @Test
    public void test47093(){ urlEncodingStyleB(
    "/vclassEdit?uri=http%3a%2f%2fwww.w3.org%2f2002%2f07%2fowl%23Thing",
    "/vclassEdit?uri=http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23Thing");
    }
    @Test
    public void test04993(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7d2e",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7d2e");
    }
    @Test
    public void test04994(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7dad",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7dad");
    }
    @Test
    public void test04995(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7d31",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7d31");
    }
    @Test
    public void test04996(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7db7",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7db7");
    }
    @Test
    public void test04997(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7df2",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7df2");
    }
    @Test
    public void test04999(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fOrganization",
    "vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FOrganization");
    }
    @Test
    public void test05000(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson");
    }
    @Test
    public void test13898(){ urlEncodingStyleB(
    "entityEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fvitro%2fpublic%23File",
    "entityEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fvitro%2Fpublic%23File");
    }
    @Test
    public void test13899(){ urlEncodingStyleB(
    "/vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fvitro%2fpublic%23File",
    "/vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fvitro%2Fpublic%23File");
    }
    @Test
    public void test28454(){ urlEncodingStyleB(
    "entityEdit?uri=http%3a%2f%2fwww.w3.org%2f2002%2f07%2fowl%23Thing",
    "entityEdit?uri=http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23Thing");
    }
    @Test
    public void test28458(){ urlEncodingStyleB(
    "/vclassEdit?uri=http%3a%2f%2fwww.w3.org%2f2002%2f07%2fowl%23Thing",
    "/vclassEdit?uri=http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23Thing");
    }
    @Test
    public void test38687(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7d75",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7d75");
    }
    @Test
    public void test38693(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7d76",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7d76");
    }
    @Test
    public void test38694(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23AbstractInformation",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23AbstractInformation");
    }
    @Test
    public void test38695(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvitro.mannlib.cornell.edu%2fns%2fbnode%23-20981c46%3a12c18866689%3a-7d77",
    "vclassEdit?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fns%2Fbnode%23-20981c46%3A12c18866689%3A-7d77");
    }
    @Test
    public void test38696(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fpurl.org%2fontology%2fbibo%2fThesisDegree",
    "vclassEdit?uri=http%3A%2F%2Fpurl.org%2Fontology%2Fbibo%2FThesisDegree");
    }
    @Test
    public void test43123(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fxmlns.com%2ffoaf%2f0.1%2fPerson",
    "vclassEdit?uri=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson");
    }
    @Test
    public void test43124(){ urlEncodingStyleB(
    "vclassEdit?uri=http%3a%2f%2fvivoweb.org%2fontology%2fcore%23Postdoc",
    "vclassEdit?uri=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23Postdoc");
    }
    @Test
    public void test59983(){ urlEncodingStyleB(
    "propertyEdit?uri=http%3a%2f%2fpurl.org%2fdc%2fterms%2fcontributor",
    "propertyEdit?uri=http%3A%2F%2Fpurl.org%2Fdc%2Fterms%2Fcontributor");
    }
    @Test
    public void test17004(){ urlEncodingStyleB(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-2",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-2");
    }
    @Test
    public void test17017(){ urlEncodingStyleB(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-inf",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-inf");
    }
    @Test
    public void test17021(){ urlEncodingStyleB(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-userAccounts",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-userAccounts");
    }
    @Test
    public void test17033(){ urlEncodingStyleB(
    "ingest?action=outputModel&modelName=http%3a%2f%2fvitro.mannlib.cornell.edu%2fdefault%2fvitro-kb-displayMetadata",
    "ingest?action=outputModel&modelName=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fdefault%2Fvitro-kb-displayMetadata");
    }

    public NamespaceMapper getMockNamespaceMapper(){
        return new NamespaceMapper() {
            
            @Override
            public void removedStatements(Model arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void removedStatements(StmtIterator arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void removedStatements(List<Statement> arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void removedStatements(Statement[] arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void removedStatement(Statement arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void notifyEvent(Model arg0, Object arg1) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addedStatements(Model arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addedStatements(StmtIterator arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addedStatements(List<Statement> arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addedStatements(Statement[] arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addedStatement(Statement arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public List<String> getPrefixesForNamespace(String namespace) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String getPrefixForNamespace(String namespace) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String getNamespaceForPrefix(String prefix) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}
