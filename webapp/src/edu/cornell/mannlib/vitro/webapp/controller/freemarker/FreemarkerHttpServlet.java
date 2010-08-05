/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.BreadCrumbsUtil;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Scripts;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Stylesheets;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.TabMenu;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FreemarkerHttpServlet extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreemarkerHttpServlet.class);
    private static final int FILTER_SECURITY_LEVEL = LoginFormBean.EDITOR;
    
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {
  
        super.doGet(request,response);   
        
    	try {
	        VitroRequest vreq = new VitroRequest(request);
	        Configuration config = getConfig(vreq);
	        
	        // We can't use shared variables in the FreeMarker configuration to store anything 
	        // except theme-specific data, because multiple portals or apps might share the same theme. So instead
	        // just put the shared variables in both root and body.
	        Map<String, Object> sharedVariables = getSharedVariables(vreq); // start by getting the title here

	        // root is the map used to create the page shell - header, footer, menus, etc.
	        Map<String, Object> root = new HashMap<String, Object>(sharedVariables);

	        // body is the map used to create the page body
	        Map<String, Object> body = new HashMap<String, Object>(sharedVariables);
	        
	        setUpRoot(vreq, root); 	        
	        root.put("body", getBody(vreq, body, config)); // need config to get and process template
	        
	        // getBody() may have changed the title, so put the new value in the root map. (E.g., the title may
	        // include an individual's name, which is only discovered when processing the body.)
	        root.put("title", body.get("title"));
	        
	        writePage(root, config, response);
       
	    } catch (Throwable e) {
	        log.error("FreeMarkerHttpServlet could not forward to view.", e);
	    }
	}

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request, response);
    }
   
    protected Configuration getConfig(VitroRequest vreq) {
        
        String themeDir = getThemeDir(vreq.getPortal());        
        return getConfigForTheme(themeDir);
    }

    @SuppressWarnings("unchecked")
    protected Configuration getConfigForTheme(String themeDir) {
        
        // The template loader is theme-specific because it specifies the theme template directory as a location to
        // load templates from. Thus configurations are associated with themes rather than portals.
        Map<String, Configuration> themeToConfigMap = (Map<String, Configuration>) (getServletContext().getAttribute("themeToConfigMap"));
        
        if( themeToConfigMap == null )
        	log.error("The templating system is not configured correctly. Make sure that you have the FreeMarkerSetup context listener in your web.xml");
        if (themeToConfigMap.containsKey(themeDir)) {
            return themeToConfigMap.get(themeDir);
        } else {
            Configuration config = getNewConfig(themeDir);
            themeToConfigMap.put(themeDir, config);
            return config;
        }
    }
    
    private Configuration getNewConfig(String themeDir) {
        
        Configuration config = new Configuration();

        String buildEnv = ConfigurationProperties.getProperty("Environment.build");
        log.debug("Current build environment: " + buildEnv);
        if ("development".equals(buildEnv)) {
            log.debug("Disabling FreeMarker template caching in development build.");
            config.setTemplateUpdateDelay(0); // no template caching in development 
        }

        // Specify how templates will see the data-model. 
        // The default wrapper exposes set methods unless exposure level is set.
        // By default we want to block exposure of set methods. 
        // config.setObjectWrapper(new DefaultObjectWrapper());
        BeansWrapper wrapper = new DefaultObjectWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        config.setObjectWrapper(wrapper);

        // Set some formatting defaults. These can be overridden at the template
        // or environment (template-processing) level, or for an individual
        // instance by using built-ins.
        config.setLocale(java.util.Locale.US);
        
        String dateFormat = "M/d/yyyy";
        config.setDateFormat(dateFormat);
        String timeFormat = "hh:mm a";
        config.setTimeFormat(timeFormat);
        config.setDateTimeFormat(dateFormat + " " + timeFormat);
        
        //config.setNumberFormat("#,##0.##");
        
        try {
            config.setSetting("url_escaping_charset", "ISO-8859-1");
        } catch (TemplateException e) {
            log.error("Error setting value for url_escaping_charset.");
        }
        
        config.setTemplateLoader(getTemplateLoader(config, themeDir));
        
        return config;
    }

    // Define template locations. Template loader will look first in the theme-specific
    // location, then in the vitro location.
    protected final TemplateLoader getTemplateLoader(Configuration config, String themeDir) {
        
        ServletContext context = getServletContext();
        String themeTemplatePath = context.getRealPath(themeDir) + "/templates";
        String vitroTemplatePath = context.getRealPath("/templates/freemarker");
        
        try {
            TemplateLoader[] loaders;
            FlatteningTemplateLoader vitroFtl = new FlatteningTemplateLoader(new File(vitroTemplatePath));
            ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "");
            
            File themeTemplateDir = new File(themeTemplatePath);
            // Handle the case where there's no theme template directory gracefully
            if (themeTemplateDir.exists()) {
                FileTemplateLoader themeFtl = new FileTemplateLoader(themeTemplateDir);
                loaders = new TemplateLoader[] { themeFtl, vitroFtl, ctl };
            } else {
                loaders = new TemplateLoader[] { vitroFtl, ctl };
            }
            MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
            return mtl;
        } catch (IOException e) {
            log.error("Error creating template loaders");
            return null;
        }
        
    }

    // We can't use shared variables in the FreeMarker configuration to store anything 
    // except theme-specific data, because multiple portals or apps might share the same theme. So instead
    // we'll get all the shared variables here, and put them in both root and body maps.
    protected Map<String, Object> getSharedVariables(VitroRequest vreq) {
        
        Map<String, Object> map = new HashMap<String, Object>();
        
        Portal portal = vreq.getPortal();
        // Ideally, templates wouldn't need portal id. Currently used as a hidden input value
        // in the site search box, so needed for now.
        map.put("portalId", portal.getPortalId());
        
        String siteName = portal.getAppName();
        map.put("siteName", siteName);
        map.put("title", getTitle(siteName));

        String themeDir = getThemeDir(portal);
        UrlBuilder urlBuilder = new UrlBuilder(portal);
        
        map.put("urls", getUrls(themeDir, urlBuilder)); 

        // This value will be available to any template as a path for adding a new stylesheet.
        // It does not contain the context path, because the methods to generate the href
        // attribute from the string passed in by the template automatically add the context path.
        map.put("themeStylesheetDir", themeDir + "/css");

        map.put("stylesheets", getStylesheetList(themeDir));
        map.put("scripts", getScriptList(themeDir));
  
        addDirectives(map);
        
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
    private void addDirectives(Map<String, Object> map) {
        map.put("dump", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DumpDirective());
        map.put("dumpAll", new edu.cornell.mannlib.vitro.webapp.web.directives.dump.DumpAllDirective());       
    }
    
    // Add variables that should be available only to the page's root map, not to the body.
    // RY This is protected instead of private so FreeMarkerComponentGenerator can access.
    // Once we don't need that (i.e., jsps have been eliminated) we can make it private.
    protected void setUpRoot(VitroRequest vreq, Map<String, Object> root) {
        
        root.put("tabMenu", getTabMenu(vreq));

        Portal portal = vreq.getPortal();
        
        ApplicationBean appBean = vreq.getAppBean();
        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao()); 
        
        addLoginInfo(vreq, root);      
        
        root.put("copyright", getCopyrightInfo(portal));
    
        root.put("siteTagline", portal.getShortHand());
        root.put("breadcrumbs", BreadCrumbsUtil.getBreadCrumbsDiv(vreq));
    
        String themeDir = getThemeDir(portal);

        // This value is used only in stylesheets.ftl and already contains the context path.
        root.put("stylesheetPath", UrlBuilder.getUrl(themeDir + "/css"));  

        String bannerImage = portal.getBannerImage();  
        if ( ! StringUtils.isEmpty(bannerImage)) {
            root.put("bannerImage", UrlBuilder.getUrl(themeDir + "site_icons/" + bannerImage));
        }
        
    }   

    private TabMenu getTabMenu(VitroRequest vreq) {
        // RY There's a vreq.getPortalId() method, but not sure if it returns
        // same value as this.
        int portalId = vreq.getPortal().getPortalId();
        return new TabMenu(vreq, portalId);
    }

    private final void addLoginInfo(VitroRequest vreq, Map<String, Object> root) {
        
        String loginName = null;
        int securityLevel;
        
        HttpSession session = vreq.getSession();
        LoginFormBean loginBean = (LoginFormBean) session.getAttribute("loginHandler");
        if (loginBean != null && loginBean.testSessionLevel(vreq) > -1) {
            loginName = loginBean.getLoginName();
            securityLevel = Integer.parseInt(loginBean.getLoginRole());
        }   
        if (loginName != null) {
            root.put("loginName", loginName);

            securityLevel = Integer.parseInt(loginBean.getLoginRole());
            if (securityLevel >= FILTER_SECURITY_LEVEL) {
                ApplicationBean appBean = vreq.getAppBean();
                if (appBean.isFlag1Active()) {
                    root.put("showFlag1SearchField", true);
                }
            }           
        }       
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

    // Subclasses may override. This serves as a default.
    protected String getTitle(String siteName) {        
        return siteName;
    }
        
    // Most subclasses will override. Some (e.g., ajax controllers) don't need to define a page body.
    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {
        return "";
    }

    protected StringWriter mergeToTemplate(String templateName, Map<String, Object> map, Configuration config) {   	
        FreemarkerHelper helper = new FreemarkerHelper(config);
        return helper.mergeToTemplate(templateName, map);
    }

    protected String mergeBodyToTemplate(String templateName, Map<String, Object> map, Configuration config) {
    	return mergeToTemplate(templateName, map, config).toString();
    }
    
    protected void writePage(Map<String, Object> root, Configuration config, HttpServletResponse response) {
        String templateName = getPageTemplateName();     
        writeTemplate(templateName, root, config, response);                   
    }
    
    protected void writeTemplate(String templateName, Map<String, Object> map, Configuration config, HttpServletResponse response) {       
        StringWriter sw = mergeToTemplate(templateName, map, config);          
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
        return "page.ftl";
    }

    // TEMPORARY method for transition from JSP to FreeMarker. 
    // It's a static method because it needs to be called from JSPs that don't go through a servlet.
    public static void getFreemarkerComponentsForJsp(HttpServletRequest request) {
        // We need to create a FreeMarkerHttpServlet object in order to call the instance methods
        // to set up the data model.
        new FreemarkerComponentGenerator(request);
    }

}