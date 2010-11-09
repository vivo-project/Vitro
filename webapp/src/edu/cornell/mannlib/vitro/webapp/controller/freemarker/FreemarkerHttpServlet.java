/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.BreadCrumbsUtil;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Scripts;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Stylesheets;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.TabMenu;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.servlet.AllHttpScopesHashModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FreemarkerHttpServlet extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreemarkerHttpServlet.class);
    private static final int FILTER_SECURITY_LEVEL = LoginStatusBean.EDITOR;

    protected enum Template {
        STANDARD_ERROR("error-standard.ftl"),
        ERROR_MESSAGE("error-message.ftl"),
        TITLED_ERROR_MESSAGE("error-titled.ftl"),
        MESSAGE("message.ftl"),
        TITLED_MESSAGE("message-titled.ftl"),
        PAGE_DEFAULT("page.ftl");
        
        private final String filename;
        
        Template(String filename) {
            this.filename = filename;
        }

        public String toString() {
            return filename;
        }
    }
    
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws IOException, ServletException {
        
        super.doGet(request,response);   
        
    	try {
	        VitroRequest vreq = new VitroRequest(request);
	        
	        Configuration config = getConfig(vreq);
	        vreq.setAttribute("freemarkerConfig", config);
	        
	        ResponseValues responseValues;
	        
	        if (requiresLogin() && !checkLoginStatus(request, response)) {
	            responseValues = new RedirectResponseValues(UrlBuilder.Route.LOGIN.path());
	        } else {
	            responseValues = processRequest(vreq);
	        }

	        doResponse(vreq, response, responseValues);	        
       
        } catch (Throwable e) {
            log.error("FreeMarkerHttpServlet could not forward to view.", e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request, response);
    }
   
    protected Configuration getConfig(VitroRequest vreq) {               
        FreemarkerConfigurationLoader loader = 
            FreemarkerConfigurationLoader.getFreemarkerConfigurationLoader(getServletContext());
        return loader.getConfig(vreq);
    }
    
    protected boolean requiresLogin() {
        // By default, user does not need to be logged in to view pages.
        // Subclasses that require login to process their page will override to return true.
        // NB This method can't be static, because then the superclass method gets called rather than
        // the subclass method. For the same reason, it can't refer to a static or instance field
        // REQUIRES_LOGIN which is overridden in the subclass.
        return false;
    }
    
    // Subclasses will override
    protected ResponseValues processRequest(VitroRequest vreq) {
        return null;
    }
       
    protected void doResponse(VitroRequest vreq, HttpServletResponse response, ResponseValues values) {
        try {
            
            int statusCode = values.getStatusCode();
            if (statusCode > 0) {
                response.setStatus(statusCode);
            }

            if (values instanceof ExceptionResponseValues) {
                doException(vreq, response, values);
            } else if (values instanceof TemplateResponseValues) {
                doTemplate(vreq, response, values);
            } else if (values instanceof RedirectResponseValues) {
                doRedirect(vreq, response, values);
            } else if (values instanceof ForwardResponseValues) {
                doForward(vreq, response, values);
            } else if (values instanceof RdfResponseValues) {
                doRdf(vreq, response, values);
            }
        } catch (ServletException e) {
            log.error("ServletException in doResponse()", e);
        } catch (IOException e) {
            log.error("IOException in doResponse()", e);
        }
     
    }

    protected void doTemplate(VitroRequest vreq, HttpServletResponse response, ResponseValues values) {
     
        Configuration config = getConfig(vreq);
        Map<String, Object> bodyMap = values.getMap();
        
        // We can't use shared variables in the Freemarker configuration to store anything 
        // except theme-specific data, because multiple portals or apps might share the same theme. So instead
        // just put the shared variables in both root and body.
        Map<String, Object> sharedVariables = getSharedVariables(vreq, bodyMap);
        
        // root is the map used to create the page shell - header, footer, menus, etc.
        Map<String, Object> root = new HashMap<String, Object>(sharedVariables);
        
        root.putAll(getPageTemplateValues(vreq));
        // Tell the template and any directives it uses that we're processing a page template.
        root.put("templateType", "page");

        // Add the values that we got, and merge to the template.
        String bodyTemplate = values.getTemplateName();
        String bodyString;
        if (bodyTemplate != null) {
            // body is the map used to create the page body
            // Adding sharedVariables before bodyMap values is important. If the body
            // processing has changed a shared value such as title, we want to get that 
            // value.
            Map<String, Object> body = new HashMap<String, Object>(sharedVariables);
            body.putAll(bodyMap);
            // Tell the template and any directives it uses that we're processing a body template.
            body.put("templateType", "body");
            bodyString = processTemplateToString(bodyTemplate, body, config, vreq); 
        } else {
            // The subcontroller has not defined a body template. All markup for the page 
            // is specified in the main page template.
            bodyString = "";
        }
        root.put("body", bodyString);
        
        writePage(root, config, vreq, response);       
    }
    
    protected void doRedirect(HttpServletRequest request, HttpServletResponse response, ResponseValues values) 
        throws ServletException, IOException { 
        String redirectUrl = values.getRedirectUrl();
        response.sendRedirect(redirectUrl);        
    }
    
    protected void doForward(HttpServletRequest request, HttpServletResponse response, ResponseValues values) 
        throws ServletException, IOException {        
        String forwardUrl = values.getForwardUrl();
        if (forwardUrl.contains("://")) {
            // It's a full URL, so redirect.
            response.sendRedirect(forwardUrl);
        } else {
            // It's a relative URL, so forward within the application.
            request.getRequestDispatcher(forwardUrl).forward(request, response);
        }
    }
    
    protected void doRdf(HttpServletRequest request, HttpServletResponse response, ResponseValues values) 
        throws IOException {
        
        String mediaType = values.getContentType().getMediaType();
        response.setContentType(mediaType);
        
        String format = ""; 
        if ( RDFXML_MIMETYPE.equals(mediaType)) {
            format = "RDF/XML";
        } else if( N3_MIMETYPE.equals(mediaType)) {
            format = "N3";
        } else if ( TTL_MIMETYPE.equals(mediaType)) {
            format ="TTL";
        }
        
        values.getModel().write( response.getOutputStream(), format );      
    }

    protected void doException(VitroRequest vreq, HttpServletResponse response, ResponseValues values) {
        // Log the error, and display an error message on the page.        
        log.error(values.getException(), values.getException());      
        TemplateResponseValues trv = new TemplateResponseValues(values.getTemplateName(), values.getMap());
        doTemplate(vreq, response, trv);
    }

    // We can't use shared variables in the Freemarker configuration to store anything 
    // except theme-specific data, because multiple portals or apps might share the same theme. So instead
    // we'll get all the shared variables here, and put them in both root and body maps.
    public Map<String, Object> getSharedVariables(VitroRequest vreq, Map<String, Object> bodyMap) {
        
        Map<String, Object> map = new HashMap<String, Object>();
        
        Portal portal = vreq.getPortal();
        // Ideally, templates wouldn't need portal id. Currently used as a hidden input value
        // in the site search box, so needed for now.
        map.put("portalId", portal.getPortalId());
        
        String siteName = portal.getAppName();
        map.put("siteName", siteName);
        
        // In some cases the title is determined during the subclass processRequest() method; e.g., 
        // for an Individual profile page, the title should be the Individual's label. In that case,
        // put that title in the sharedVariables map because it's needed by the page root map as well
        // to generate the <title> element. Otherwise, use the getTitle() method to generate the title.
        String title = (String) bodyMap.get("title");
        if (StringUtils.isEmpty(title)) {
            title = getTitle(siteName);
        }
        map.put("title", title);

        String themeDir = getThemeDir(portal);
        UrlBuilder urlBuilder = new UrlBuilder(portal);
        
        map.put("urls", getUrls(themeDir, urlBuilder)); 

        map.put("themeDir", themeDir);

        map.put("themeDir", themeDir);

        map.put("stylesheets", getStylesheetList(themeDir));
        map.put("scripts", getScriptList(themeDir));
        map.put("headScripts", getScriptList(themeDir));
  
        map.putAll(getDirectives());
        
        return  map;
    }


    public String getThemeDir(Portal portal) {
        return portal.getThemeDir().replaceAll("/$", "");
    }

    // Define the URLs that are accessible to the templates. Note that we do not create menus here,
    // because we want the templates to be free to define the link text and where the links are displayed.
    private final Map<String, String> getUrls(String themeDir, UrlBuilder urlBuilder) {
        // The urls that are accessible to the templates. 
        // NB We are not using our menu object mechanism to build menus here, because we want the 
        // view to control which links go where, and the link text and title.
        Map<String, String> urls = new HashMap<String, String>();
        
        urls.put("home", urlBuilder.getHomeUrl());

        urls.put("about", urlBuilder.getPortalUrl(Route.ABOUT));
        if (ContactMailServlet.getSmtpHostFromProperties() != null) {
            urls.put("contact", urlBuilder.getPortalUrl(Route.CONTACT));
        }
        urls.put("search", urlBuilder.getPortalUrl(Route.SEARCH));  
        urls.put("termsOfUse", urlBuilder.getPortalUrl(Route.TERMS_OF_USE));  
        urls.put("login", urlBuilder.getPortalUrl(Route.LOGIN));          
        urls.put("logout", urlBuilder.getLogoutUrl());       
        urls.put("siteAdmin", urlBuilder.getPortalUrl(Route.LOGIN));  
        urls.put("siteIcons", urlBuilder.getPortalUrl(themeDir + "/site_icons"));
        urls.put("themeImages", urlBuilder.getPortalUrl(themeDir + "/images"));
        urls.put("images", urlBuilder.getUrl("/images"));

        return urls;
    }
    
    private TemplateModel getStylesheetList(String themeDir) {
        
        // For script and stylesheet lists, use an object wrapper that exposes write methods, 
        // instead of the configuration's object wrapper, which doesn't. The templates can
        // add stylesheets and scripts to the lists by calling their add() methods.
        BeansWrapper wrapper = new DefaultObjectWrapper();
        try {
            // Here themeDir SHOULD NOT have the context path already added to it.
            return wrapper.wrap(new Stylesheets(themeDir));       
        } catch (TemplateModelException e) {
            log.error("Error creating stylesheet TemplateModel");
            return null;
        }
    }
    
    private TemplateModel getScriptList(String themeDir) {
        
        // For script and stylesheet lists, use an object wrapper that exposes write methods, 
        // instead of the configuration's object wrapper, which doesn't. The templates can
        // add stylesheets and scripts to the lists by calling their add() methods.
        BeansWrapper wrapper = new DefaultObjectWrapper();
        try {
            return wrapper.wrap(new Scripts(themeDir));       
        } catch (TemplateModelException e) {
            log.error("Error creating script TemplateModel");
            return null;
        }        
    }
    
    // Add any Java directives the templates should have access to
    private Map<String, Object> getDirectives() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("describe", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DescribeDirective());
        map.put("dump", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DumpDirective());
        map.put("dumpAll", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DumpAllDirective());  
        map.put("help", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.HelpDirective()); 
        //map.put("url", new edu.cornell.mannlib.vitro.webapp.web.directives.UrlDirective()); 
        map.put("widget", new edu.cornell.mannlib.vitro.webapp.web.directives.widgets.BaseWidgetDirective());
        return map;
    }
    
    // Add variables that should be available only to the page's root map, not to the body.
    // RY This is protected instead of private so FreeMarkerComponentGenerator can access.
    // Once we don't need that (i.e., jsps have been eliminated) we can make it private.
    protected Map<String, Object> getPageTemplateValues(VitroRequest vreq) {
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tabMenu", getTabMenu(vreq));

        Portal portal = vreq.getPortal();
        
        ApplicationBean appBean = vreq.getAppBean();
        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao()); 
        
        map.putAll(getLoginValues(vreq));  
        
        UrlBuilder urlBuilder = new UrlBuilder(portal); 
        map.put("version", getRevisionInfo(urlBuilder));
        
        map.put("copyright", getCopyrightInfo(portal));    
        map.put("siteTagline", portal.getShortHand());
        map.put("breadcrumbs", BreadCrumbsUtil.getBreadCrumbsDiv(vreq));
    
        String themeDir = getThemeDir(portal);

        // This value is used only in stylesheets.ftl and already contains the context path.
        map.put("stylesheetPath", UrlBuilder.getUrl(themeDir + "/css"));  

        String bannerImage = portal.getBannerImage();  
        if ( ! StringUtils.isEmpty(bannerImage)) {
            map.put("bannerImage", UrlBuilder.getUrl(themeDir + "site_icons/" + bannerImage));
        }
        
        return map;        
    }   


    private TabMenu getTabMenu(VitroRequest vreq) {
        int portalId = vreq.getPortal().getPortalId();
        return new TabMenu(vreq, portalId);
    }

    private final Map<String, Object> getLoginValues(VitroRequest vreq) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        LoginStatusBean loginBean = LoginStatusBean.getBean(vreq);
        if (loginBean.isLoggedIn()) {
            map.put("loginName", loginBean.getUsername());

            if (loginBean.isLoggedInAtLeast(FILTER_SECURITY_LEVEL)) {
                ApplicationBean appBean = vreq.getAppBean();
                if (appBean.isFlag1Active()) {
                    map.put("showFlag1SearchField", true);
                }
            }           
        }  
        
        return map;
    }   
    
    private final Map<String, Object> getCopyrightInfo(Portal portal) {

        Map<String, Object> copyright = null;
        String copyrightText = portal.getCopyrightAnchor();
        if ( ! StringUtils.isEmpty(copyrightText) ) {
            copyright =  new HashMap<String, Object>();
            copyright.put("text", copyrightText);
            int thisYear = Calendar.getInstance().get(Calendar.YEAR);  // use ${copyrightYear?c} in template
            //String thisYear = ((Integer)Calendar.getInstance().get(Calendar.YEAR)).toString(); // use ${copyrightYear} in template
            //SimpleDate thisYear = new SimpleDate(Calendar.getInstance().getTime(), TemplateDateModel.DATE); // use ${copyrightYear?string("yyyy")} in template
            copyright.put("year", thisYear);
            copyright.put("url", portal.getCopyrightURL());
        } 
        return copyright;
    }
    
    private final Map<String, Object> getRevisionInfo(UrlBuilder urlBuilder) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("label", RevisionInfoBean.getBean(getServletContext())
                .getReleaseLabel());
        map.put("moreInfoUrl", urlBuilder.getPortalUrl("/revisionInfo"));
        return map;
    }

    // Subclasses may override. This serves as a default.
    protected String getTitle(String siteName) {        
        return siteName;
    }

    protected StringWriter processTemplate(String templateName, Map<String, Object> map, Configuration config, 
            HttpServletRequest request) {    
        FreemarkerHelper helper = new FreemarkerHelper(config, request, getServletContext());
        return helper.processTemplate(templateName, map);
    }
    
    protected StringWriter processTemplate(ResponseValues values, Configuration config, HttpServletRequest request) {
        return processTemplate(values.getTemplateName(), values.getMap(), config, request);
    }
    
    // In fact, we can put StringWriter objects directly into the data model, so perhaps we should eliminate the processTemplateToString() methods.
    protected String processTemplateToString(String templateName, Map<String, Object> map, Configuration config, 
            HttpServletRequest request) {
        return processTemplate(templateName, map, config, request).toString();
    }
  
    protected String processTemplateToString(ResponseValues values, Configuration config, HttpServletRequest request) {
        return processTemplate(values, config, request).toString();
    }
    
    protected void writePage(Map<String, Object> root, Configuration config, HttpServletRequest request, HttpServletResponse response) {   
        writeTemplate(getPageTemplateName(), root, config, request, response);                   
    }
    
    protected void writeTemplate(String templateName, Map<String, Object> map, Configuration config, 
            HttpServletRequest request, HttpServletResponse response) {       
        StringWriter sw = processTemplate(templateName, map, config, request);          
        write(sw, response);
    }
    
    protected void write(StringWriter sw, HttpServletResponse response) {
        
        try {
            PrintWriter out = response.getWriter();
            out.print(sw);     
        } catch (IOException e) {
            log.error("FreeMarkerHttpServlet cannot write output", e);
        }            
    }
    
    // Can be overridden by individual controllers to use a different basic page layout.
    protected String getPageTemplateName() {
        return Template.PAGE_DEFAULT.toString();
    }

    // TEMPORARY method for transition from JSP to Freemarker. 
    // It's a static method because it needs to be called from JSPs that don't go through a servlet.
    public static void getFreemarkerComponentsForJsp(HttpServletRequest request) {
        // We need to create a FreeMarkerHttpServlet object in order to call the instance methods
        // to set up the data model.
        new FreemarkerComponentGenerator(request);
    }

    protected static interface ResponseValues {

        String getTemplateName();

        int getStatusCode();

        void setStatusCode(int statusCode);

        Map<String, Object> getMap();
        
        String getRedirectUrl();

        String getForwardUrl();

        Throwable getException();
        
        ContentType getContentType();
        
        Model getModel();
    }
    
    protected static abstract class BaseResponseValues implements ResponseValues {
        private int statusCode = 0;
        private ContentType contentType = null;
        
        BaseResponseValues() { }
        
        BaseResponseValues(int statusCode) {
            this.statusCode = statusCode;
        }

        BaseResponseValues(ContentType contentType) {
            this.contentType = contentType;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public ContentType getContentType() {
            return contentType;
        }

        public void setContentType(ContentType contentType) {
            this.contentType = contentType;
        }
    }

    protected static class TemplateResponseValues extends BaseResponseValues {
        private final String templateName;
        private final Map<String, Object> map;
        
        public TemplateResponseValues(String templateName) {
            this.templateName = templateName;
            this.map = new HashMap<String, Object>();
        }

        public TemplateResponseValues(String templateName, int statusCode) {
            super(statusCode);
            this.templateName = templateName;
            this.map = new HashMap<String, Object>();
        }
        
        public TemplateResponseValues(String templateName, Map<String, Object> map) {
            this.templateName = templateName;
            this.map = map;
        }

        public TemplateResponseValues(String templateName, Map<String, Object> map, int statusCode) {
            super(statusCode);
            this.templateName = templateName;
            this.map = map;
        }
        
        public TemplateResponseValues put(String key, Object value) {
            this.map.put(key, value);
            return this;
        }

        @Override
        public Map<String, Object> getMap() {
            return Collections.unmodifiableMap(this.map);
        }

        @Override
        public String getTemplateName() {
            return this.templateName;
        }

        @Override
        public String getRedirectUrl() {
            throw new UnsupportedOperationException(
                    "This is not a redirect response.");
        }
        
        @Override
        public String getForwardUrl() {
            throw new UnsupportedOperationException(
                    "This is not a forwarding response.");
        }
        
        @Override
        public Throwable getException() {
            throw new UnsupportedOperationException(
                    "This is not an exception response.");
        }

        @Override
        public Model getModel() {
            throw new UnsupportedOperationException(
                    "This is not an RDF response.");
        }
        
    }

    protected static class RedirectResponseValues extends BaseResponseValues {
        private final String redirectUrl;

        public RedirectResponseValues(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public RedirectResponseValues(String redirectUrl, int statusCode) {
            super(statusCode);
            this.redirectUrl = redirectUrl;
        }
        
        @Override
        public String getRedirectUrl() {
            return this.redirectUrl;
        }

        @Override
        public String getTemplateName() {
            throw new UnsupportedOperationException(
                    "This is not a template response.");
        }

        @Override
        public Map<String, Object> getMap() {
            throw new UnsupportedOperationException(
                    "This is not a template response.");
        }

        @Override
        public String getForwardUrl() {
            throw new UnsupportedOperationException(
                    "This is not a forwarding response.");
        }
        
        @Override
        public Throwable getException() {
            throw new UnsupportedOperationException(
                    "This is not an exception response.");
        }

        @Override
        public Model getModel() {
            throw new UnsupportedOperationException(
                    "This is not an RDF response.");
        }
    }

    protected static class ForwardResponseValues extends BaseResponseValues {
        private final String forwardUrl;

        public ForwardResponseValues(String forwardUrl) {
            this.forwardUrl = forwardUrl;
        }

        public ForwardResponseValues(String forwardUrl, int statusCode) {
            super(statusCode);
            this.forwardUrl = forwardUrl;
        }

        @Override
        public String getForwardUrl() {
            return this.forwardUrl;
        }

        @Override
        public String getTemplateName() {
            throw new UnsupportedOperationException(
                    "This is not a template response.");
        }

        @Override
        public Map<String, Object> getMap() {
            throw new UnsupportedOperationException(
                    "This is not a template response.");
        }

        @Override
        public String getRedirectUrl() {
            throw new UnsupportedOperationException(
                    "This is not a redirect response.");
        }
        
        @Override
        public Throwable getException() {
            throw new UnsupportedOperationException(
                    "This is not an exception response.");
        }

        @Override
        public Model getModel() {
            throw new UnsupportedOperationException(
                    "This is not an RDF response.");
        }
    }

    protected static class ExceptionResponseValues extends TemplateResponseValues {
        private final static String DEFAULT_TEMPLATE_NAME = "error-standard.ftl";
        private final Throwable cause;

        public ExceptionResponseValues(Throwable cause) {
            super(DEFAULT_TEMPLATE_NAME);
            this.cause = cause;
        }

        public ExceptionResponseValues(Throwable cause, int statusCode) {
            super(DEFAULT_TEMPLATE_NAME, statusCode);
            this.cause = cause;
        }
        
        public ExceptionResponseValues(String templateName, Throwable cause) {
            super(templateName);
            this.cause = cause;
        }

        public ExceptionResponseValues(String templateName, Throwable cause, int statusCode) {
            super(templateName, statusCode);
            this.cause = cause;
        }
        
        public ExceptionResponseValues(String templateName, Map<String, Object> map, Throwable cause) {
            super(templateName, map);
            this.cause = cause;
        }

        public ExceptionResponseValues(String templateName, Map<String, Object> map, Throwable cause, int statusCode) {
            super(templateName, map, statusCode);
            this.cause = cause;
        }

        @Override
        public Throwable getException() {
            return cause;
        }

        @Override
        public String getRedirectUrl() {
            throw new UnsupportedOperationException(
                    "This is not a redirect response.");
        }
        
        @Override
        public String getForwardUrl() {
            throw new UnsupportedOperationException(
                    "This is not a forwarding response.");
        }
        
        @Override
        public Model getModel() {
            throw new UnsupportedOperationException(
                    "This is not an RDF response.");
        }        
    }
    
    protected static class RdfResponseValues extends BaseResponseValues {
        private final Model model;
        
        RdfResponseValues(ContentType contentType, Model model) {
            super(contentType);
            this.model = model;
        }

        @Override
        public String getTemplateName() {
            throw new UnsupportedOperationException(
                "This is not a template response.");
        }

        @Override
        public Map<String, Object> getMap() {
            throw new UnsupportedOperationException(
                "This is not a template response.");
        }

        @Override
        public String getRedirectUrl() {
            throw new UnsupportedOperationException(
                "This is not a redirect response.");
        }

        @Override
        public String getForwardUrl() {
            throw new UnsupportedOperationException(
                "This is not a forwarding response.");
        }

        @Override
        public Throwable getException() {
            throw new UnsupportedOperationException(
                "This is not an exception response.");
        }
        
        @Override
        public Model getModel() {
           return model;
        }
    }
}