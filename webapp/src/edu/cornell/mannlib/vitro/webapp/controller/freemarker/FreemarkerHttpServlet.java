/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ForwardResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RdfResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.BreadCrumbsUtil;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.User;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Scripts;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Stylesheets;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MainMenu;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.TabMenu;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FreemarkerHttpServlet extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreemarkerHttpServlet.class);

    
    public static final String PAGE_TEMPLATE_TYPE = "page";
    public static final String BODY_TEMPLATE_TYPE = "body";

    protected enum Template {
        STANDARD_ERROR("error-standard.ftl"),
        ERROR_MESSAGE("error-message.ftl"),
        TITLED_ERROR_MESSAGE("error-titled.ftl"),
        MESSAGE("message.ftl"),
        TITLED_MESSAGE("message-titled.ftl"),
        PAGE_DEFAULT("page.ftl"),
        SETUP("setup.ftl");
        
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
	        
	        // This method does a redirect if the required login level is not met, so just return.
	        if (requiredLoginLevelNotFound(request, response)) {
	            return; 
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

    private boolean requiredLoginLevelNotFound(HttpServletRequest request, HttpServletResponse response) {
        int requiredLoginLevel = requiredLoginLevel();
        // checkLoginStatus() does a redirect if the user is not logged in.
        if (requiredLoginLevel > LoginStatusBean.ANYBODY && !checkLoginStatus(request, response, requiredLoginLevel)) {
            return true;
        }
        return false;
    }
    
    protected int requiredLoginLevel() {
        // By default, user does not need to be logged in to view pages.
        // Subclasses that require login to process their page will override to return the required login level.
        // NB This method can't be static, because then the superclass method gets called rather than
        // the subclass method. For the same reason, it can't refer to a static or instance field
        // REQUIRES_LOGIN_LEVEL which is overridden in the subclass.
        return LoginStatusBean.ANYBODY;
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
        
        Map<String, Object> templateDataModel = new HashMap<String, Object>();        
        templateDataModel.putAll(getPageTemplateValues(vreq));

        // Add the values that we got from the subcontroller processRequest() method, and merge to the template.
        // Subcontroller values (for example, for page title) will override what's already been set at the page
        // level.
        templateDataModel.putAll(values.getMap());
        
        // If a body template is specified, merge it with the template data model.
        String bodyString;
        String bodyTemplate = values.getTemplateName();        
        if (bodyTemplate != null) {
            // Tell the template and any directives it uses that we're processing a body template.
            templateDataModel.put("templateType", BODY_TEMPLATE_TYPE);
            bodyString = processTemplateToString(bodyTemplate, templateDataModel, config, vreq); 
        } else {
            // The subcontroller has not defined a body template. All markup for the page 
            // is specified in the main page template.
            bodyString = "";
        }
        templateDataModel.put("body", bodyString);
        
        // Tell the template and any directives it uses that we're processing a page template.
        templateDataModel.put("templateType", PAGE_TEMPLATE_TYPE);        
        writePage(templateDataModel, config, vreq, response);       
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
        TemplateResponseValues trv = TemplateResponseValues.getTemplateResponseValuesFromException((ExceptionResponseValues)values);
        doTemplate(vreq, response, trv);
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
        
        // Templates use this to construct urls.
        urls.put("base", urlBuilder.contextPath);

        urls.put("about", urlBuilder.getPortalUrl(Route.ABOUT));
        if (ContactMailServlet.getSmtpHostFromProperties() != null) {
            urls.put("contact", urlBuilder.getPortalUrl(Route.CONTACT));
        }
        urls.put("search", urlBuilder.getPortalUrl(Route.SEARCH));  
        urls.put("termsOfUse", urlBuilder.getPortalUrl(Route.TERMS_OF_USE));  
        urls.put("login", urlBuilder.getLoginUrl());          
        urls.put("logout", urlBuilder.getLogoutUrl());       
        urls.put("siteAdmin", urlBuilder.getPortalUrl(Route.SITE_ADMIN));  
        urls.put("siteIcons", urlBuilder.getPortalUrl(themeDir + "/site_icons")); // deprecated
        urls.put("themeImages", urlBuilder.getPortalUrl(themeDir + "/images"));
        urls.put("images", urlBuilder.getUrl("/images"));
        urls.put("theme", urlBuilder.getUrl(themeDir));
        urls.put("index", urlBuilder.getUrl("/browse"));                
        
        return urls;
    }
    
    protected BeansWrapper getNonDefaultBeansWrapper(int exposureLevel) {
        BeansWrapper wrapper = new DefaultObjectWrapper();
        // Too bad exposure levels are ints instead of enum values; what happens if 
        // we send an int that's not a defined exposure level?
        wrapper.setExposureLevel(exposureLevel);
        return wrapper;
    }
    
    private TemplateModel getStylesheetList(String themeDir) {
        
        // For script and stylesheet lists, use an object wrapper that exposes write methods, 
        // instead of the configuration's object wrapper, which doesn't. The templates can
        // add stylesheets and scripts to the lists by calling their add() methods.
        BeansWrapper wrapper = getNonDefaultBeansWrapper(BeansWrapper.EXPOSE_SAFE);
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
        BeansWrapper wrapper = getNonDefaultBeansWrapper(BeansWrapper.EXPOSE_SAFE);
        try {
            return wrapper.wrap(new Scripts(themeDir));       
        } catch (TemplateModelException e) {
            log.error("Error creating script TemplateModel");
            return null;
        }        
    }
    
    /**
     *  Add any Java directives the templates should have access to.
     *  This is public and static so that these may be used by other classes during
     *  the transition from JSP to Freemarker.
     * @return
     */    
    public static Map<String, Object> getDirectives() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("describe", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DescribeDirective());
        map.put("dump", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DumpDirective());
        map.put("dumpAll", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DumpAllDirective());  
        map.put("help", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.HelpDirective()); 
        //map.put("url", new edu.cornell.mannlib.vitro.webapp.web.directives.UrlDirective()); 
        map.put("widget", new edu.cornell.mannlib.vitro.webapp.web.directives.WidgetDirective());
        return map;
    }
    
    protected Map<String, Object> getMethods() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("profileUrl", new edu.cornell.mannlib.vitro.webapp.web.functions.IndividualProfileUrlMethod());
        return map;
    }
    
    // Add variables that are needed to generate the page template (they will also be accessible
    // to the body template).
    // RY This is protected instead of private so FreeMarkerComponentGenerator can access.
    // Once we don't need that (i.e., jsps have been eliminated) we can make it private.
    protected Map<String, Object> getPageTemplateValues(VitroRequest vreq) {
        
        Map<String, Object> map = new HashMap<String, Object>();

        Portal portal = vreq.getPortal();
        // Ideally, templates wouldn't need portal id. Currently used as a hidden input value
        // in the site search box, so needed for now.
        map.put("portalId", portal.getPortalId());
        
        String siteName = portal.getAppName();
        map.put("siteName", siteName);
        
        // This may be overridden by the body data model received from the subcontroller.
        map.put("title", getTitle(siteName, vreq));

        String themeDir = getThemeDir(portal);
        UrlBuilder urlBuilder = new UrlBuilder(portal);
        
        map.put("urls", getUrls(themeDir, urlBuilder)); 

        map.put("themeDir", themeDir);
        map.put("stylesheets", getStylesheetList(themeDir));
        map.put("scripts", getScriptList(themeDir));
        map.put("headScripts", getScriptList(themeDir));
  
        map.put("currentPage", vreq.getServletPath().replaceFirst("/", ""));
        
        map.putAll(getDirectives());
        map.putAll(getMethods());
        
        map.put("tabMenu", getTabMenu(vreq));
        map.put("menu", getDisplayModelMenu(vreq));
        
        ApplicationBean appBean = vreq.getAppBean();
        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao()); 
        
        map.put("user", new User(vreq));
        
        map.put("version", getRevisionInfo(urlBuilder));
        
        map.put("copyright", getCopyrightInfo(portal));    
        map.put("siteTagline", portal.getShortHand());
        map.put("breadcrumbs", BreadCrumbsUtil.getBreadCrumbsDiv(vreq));

        // This value is used only in stylesheets.ftl and already contains the context path.
        map.put("stylesheetPath", UrlBuilder.getUrl(themeDir + "/css"));  

        String bannerImage = portal.getBannerImage();  
        if ( ! StringUtils.isEmpty(bannerImage)) {
            map.put("bannerImage", UrlBuilder.getUrl(themeDir + "site_icons/" + bannerImage));
        }

        String flashMessage = DisplayMessage.getMessageAndClear(vreq);
        if (! flashMessage.isEmpty()) {
            map.put("flash", flashMessage);
        }

        // Let the page template know which page it's processing. 
        map.put("currentPage", vreq.getServletPath().replaceFirst("/", ""));
        
        // Allow template to send domain name to JavaScript (needed for AJAX calls)
        map.put("requestedPage", vreq.getRequestURL().toString());
        
        return map;        
    }   

    private TabMenu getTabMenu(VitroRequest vreq) {
        int portalId = vreq.getPortal().getPortalId();
        return new TabMenu(vreq, portalId);
    }
    
    protected MainMenu getDisplayModelMenu(VitroRequest vreq){
        String url = vreq.getRequestURI().substring(vreq.getContextPath().length());
        return vreq.getWebappDaoFactory().getMenuDao().getMainMenu(url);
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
    protected String getTitle(String siteName, VitroRequest vreq) {        
        return siteName;
    }

    protected StringWriter processTemplate(String templateName, Map<String, Object> map, Configuration config, 
            HttpServletRequest request) {    
        TemplateProcessingHelper helper = new TemplateProcessingHelper(config, request, getServletContext());
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
}