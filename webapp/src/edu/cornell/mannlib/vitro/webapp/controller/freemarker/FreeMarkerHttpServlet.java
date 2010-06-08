/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.view.fileList.ScriptList;
import edu.cornell.mannlib.vitro.webapp.view.fileList.StylesheetList;
import edu.cornell.mannlib.vitro.webapp.view.menu.TabMenu;
import edu.cornell.mannlib.vitro.webapp.web.BreadCrumbsUtil;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

public class FreeMarkerHttpServlet extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreeMarkerHttpServlet.class.getName());
    private static final int FILTER_SECURITY_LEVEL = LoginFormBean.EDITOR;
    
    protected static Configuration config = null;
    protected static ServletContext context = null;
    
	protected VitroRequest vreq;
	protected HttpServletResponse response;
	protected Portal portal;
	protected int portalId;
	protected String appName;
	protected UrlBuilder urlBuilder;

	private Map<String, Object> root = new HashMap<String, Object>();
    
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {

    	try {
	        doSetup(request, response);
	        setTitleAndBody();
	        write(response);
       
	    } catch (Throwable e) {
	        log.error("FreeMarkerHttpServlet could not forward to view.");
	        log.error(e.getMessage());
	        log.error(e.getStackTrace());
	    }
	}
    
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		doGet(request, response);
	}

	// Basic setup needed by all controllers
    protected void doSetup(HttpServletRequest request, HttpServletResponse response) {
        
        if ( !(this instanceof FreeMarkerComponentGenerator) ) {
            try {
                super.doGet(request,response);   
            } catch (ServletException e) {
                log.error("ServletException calling VitroHttpRequest.doGet()");
                e.printStackTrace();
            } catch (IOException e) {
                log.error("IOException calling VitroHttpRequest.doGet()");
                e.printStackTrace();
            }                
        }
        
        vreq = new VitroRequest(request);
        this.response = response;
        portal = vreq.getPortal(); 
        urlBuilder = new UrlBuilder(portal);

        // RY Can this be removed? Do templates need it? Ideally, they should not.
        // Only needed for some weird stuff in search box that I think is only used in old default theme.
        // Some forms need it also, in which case it should get set in the getBody() method and passed to that
        // body template.
        portalId = portal.getPortalId();
        root.put("portalId", portalId);
        
        appName = portal.getAppName();
        setSharedVariable("siteName", appName);

        setTemplateLoader();
        
        TabMenu menu = getTabMenu();
        root.put("tabMenu", menu);

        ApplicationBean appBean = vreq.getAppBean();
        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao());
        
        root.put("tagline", portal.getShortHand());
        root.put("breadcrumbs", BreadCrumbsUtil.getBreadCrumbsDiv(vreq));

        String themeDir = getThemeDir();
        
        setUrls(themeDir);
        setLoginInfo();      
        setCopyrightInfo();
        setThemeInfo(themeDir);
        
        // Here themeDir SHOULD NOT have the context path already added to it.
        setSharedVariable("stylesheets", new StylesheetList(themeDir)); 
        setSharedVariable("scripts", new ScriptList()); 
    }

    // Define template locations. Template loader will look first in the theme-specific
    // location, then in the vitro location.
    // RY We cannot do this in FreeMarkerSetup because (a) the theme depends on the portal,
    // and we have multi-portal installations, and (b) we need to support theme-switching on the fly.
    // To make more efficient, we could do this once, and then have a listener that does it again 
    // when theme is switched. BUT this doesn't support (a), only (b), so  we have to do it on every request.
    protected final void setTemplateLoader() {
        
        String themeTemplateDir = context.getRealPath(getThemeDir()) + "/templates";
        String vitroTemplateDir = context.getRealPath("/templates/freemarker");

        try {
            FileTemplateLoader themeFtl = new FileTemplateLoader(new File(themeTemplateDir));
            FileTemplateLoader vitroFtl = new FileTemplateLoader(new File(vitroTemplateDir));
            ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "");
            TemplateLoader[] loaders = new TemplateLoader[] { themeFtl, vitroFtl, ctl };
            MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
            config.setTemplateLoader(mtl);
        } catch (IOException e) {
            log.error("Error loading templates");
        }
        
    }

    private TabMenu getTabMenu() {
        return new TabMenu(vreq, portalId);
    }
    
    public String getThemeDir() {
        return portal.getThemeDir().replaceAll("/$", "");
    }
    
    // Define the URLs that are accessible to the templates. Note that we do not create menus here,
    // because we want the templates to be free to define the link text and where the links are displayed.
    private final void setUrls(String themeDir) {
        // The urls that are accessible to the templates. 
        // NB We are not using our menu object mechanism to build menus here, because we want the 
        // view to control which links go where, and the link text and title.
        Map<String, String> urls = new HashMap<String, String>();
        
        urls.put("home", urlBuilder.getHomeUrl());

        String bannerImage = portal.getBannerImage();
        if ( ! StringUtils.isEmpty(bannerImage)) {
            root.put("bannerImage", UrlBuilder.getUrl(themeDir + "site_icons/" + bannerImage));
        }

        urls.put("about", urlBuilder.getPortalUrl(Route.ABOUT));
        if (ContactMailServlet.getSmtpHostFromProperties() != null) {
            urls.put("contact", urlBuilder.getPortalUrl(Route.CONTACT));
        }
        urls.put("search", urlBuilder.getPortalUrl(Route.SEARCH));  
        urls.put("termsOfUse", urlBuilder.getPortalUrl(Route.TERMS_OF_USE));  
        urls.put("login", urlBuilder.getPortalUrl(Route.LOGIN));          
        urls.put("logout", urlBuilder.getLogoutUrl());       
        urls.put("siteAdmin", urlBuilder.getPortalUrl(Route.LOGIN));  
        
        setSharedVariable("urls", urls); 
    }

    private final void setLoginInfo() {
        
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
    
    private final void setCopyrightInfo() {

        String copyrightText = portal.getCopyrightAnchor();
        if ( ! StringUtils.isEmpty(copyrightText) ) {
            Map<String, Object> copyright =  new HashMap<String, Object>();
            copyright.put("text", copyrightText);
            int thisYear = Calendar.getInstance().get(Calendar.YEAR);  // use ${copyrightYear?c} in template
            //String thisYear = ((Integer)Calendar.getInstance().get(Calendar.YEAR)).toString(); // use ${copyrightYear} in template
            //SimpleDate thisYear = new SimpleDate(Calendar.getInstance().getTime(), TemplateDateModel.DATE); // use ${copyrightYear?string("yyyy")} in template
            copyright.put("year", thisYear);
            copyright.put("url", portal.getCopyrightURL());
            root.put("copyright", copyright);
        } 
    }
    
    private final void setThemeInfo(String themeDir) {

        // This value will be available to any template as a path for adding a new stylesheet.
        // It does not contain the context path, because the methods to generate the href
        // attribute from the string passed in by the template automatically add the context path.
        setSharedVariable("stylesheetDir", themeDir + "/css");
        
        String themeDirWithContext = UrlBuilder.getUrl(themeDir);
        
        // This value is used only in stylesheets.ftl and already contains the context path.
        root.put("stylesheetPath", themeDirWithContext + "/css");
        
        setSharedVariable("siteIconPath", themeDirWithContext + "/site_icons");

    }
    
    // Default case is to set title first, because it's used in the body. However, in some cases
    // the title is based on values computed during compilation of the body (e.g., IndividualListController). 
    // Individual controllers can override this method to set title and body together. End result must be:
    // body is added to root with key "body" 
    // title is set as a shared variable with key "title" 
    // This can be achieved by making sure setBody() and setTitle() are called.
    protected void setTitleAndBody() {
        setTitle();
        setBody();
    }

	protected void setBody() {
	    root.put("body", getBody());
	}

    protected String getBody() {
        return ""; // body should never be null
    }
    
	protected void setTitle() {
	    String title = getTitle();
	    // If the individual controller fails to assign a non-null, non-empty title
	    if (StringUtils.isEmpty(title)) {
	        title = appName;
	    }
	    // Title is a shared variable because it's used in both body and head elements.
	    setSharedVariable("title", title); 
	}
	
    protected String getTitle() { 
    	return "";
    }

    protected void setSharedVariable(String key, Object value) {
        try {
            config.setSharedVariable(key, value);
        } catch (TemplateModelException e) {
            log.error("Can't set shared variable '" + key + "'.");
        }       
    }
    
    protected StringWriter mergeToTemplate(String templateName, Map<String, Object> map) {
    	
        Template template = null;
        try {
        	template = config.getTemplate(templateName);
        } catch (IOException e) {
        	log.error("Cannot get template " + templateName);
        }
        StringWriter sw = new StringWriter();
        if (template != null) {	        
            try {
				template.process(map, sw);
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        	
        }
        return sw;
    }

    protected String mergeBodyToTemplate(String templateName, Map<String, Object> map) {
        templateName = "body/" + templateName;
    	String body = mergeToTemplate(templateName, map).toString();
    	return body;
    }
    
    protected void write(HttpServletResponse response) {

        String templateName = "page/" + getPageTemplateName();
        
        StringWriter sw = mergeToTemplate(templateName, root);          
        try {
            PrintWriter out = response.getWriter();
            out.print(sw);     
        } catch (IOException e) {
            log.error("FreeMarkerHttpServlet cannot write output");
            e.printStackTrace();
        }                    
    }
    
    // Can be overridden by individual controllers to use a different basic page layout.
    protected String getPageTemplateName() {
        return "default.ftl";
    }


    public static boolean isConfigured() {
        return config != null;
    }
    
    // TEMPORARY methods for transition from JSP to FreeMarker. Once transition
    // is complete and no more pages are generated in JSP, this can be removed.
    // Do this if FreeMarker is configured (i.e., not Datastar) and if we are not in
    // a FreeMarkerHttpServlet, which will generate identity, menu, and footer from the page template.
    // It's a static method because it needs to be called from JSPs that don't go through a servlet.
    public static void getFreeMarkerComponentsForJsp(HttpServletRequest request, HttpServletResponse response) {
        FreeMarkerComponentGenerator fcg = new FreeMarkerComponentGenerator(request, response);
        request.setAttribute("ftl_identity", fcg.getIdentity());
        request.setAttribute("ftl_menu", fcg.getMenu());
        request.setAttribute("ftl_search", fcg.getSearch());
        request.setAttribute("ftl_footer", fcg.getFooter());       
    }
    // This method is called by FreeMarkerComponentGenerator, since root is private.
    // Don't want to make root protected because other controllers shouldn't add to it.
    protected String mergeTemplateToRoot(String template) {
        return mergeToTemplate(template, root).toString();
    }

}