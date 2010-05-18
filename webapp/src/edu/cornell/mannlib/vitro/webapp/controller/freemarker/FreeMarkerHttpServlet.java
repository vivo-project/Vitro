/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
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
    
    public static Configuration config = null;
    public static String contextPath = null; 
    public static ServletContext context = null;
    
	protected VitroRequest vreq;
	protected HttpServletResponse response;
	protected Portal portal;
	protected String appName;
	protected Map<String, Object> root = new HashMap<String, Object>();
    
	// Some servlets have their own doGet() method, in which case they need to call 
	// doSetup(), setTitle(), setBody(), and write() themselves. Other servlets define only
	// a getBody() and getTitle() method and use the parent doGet() method.
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {
        
    	try {
    	    callSuperGet(request, response);  // ??
	        doSetup(request, response);
	        setTitle();	        
	        setBody();	        
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
	
	protected void setBody() {
	    root.put("body", getBody());
	}
	
	protected void setSharedVariable(String key, String value) {
        try {
            config.setSharedVariable(key, value);
        } catch (TemplateModelException e) {
            log.error("Can't set shared variable '" + key + "'.");
        }	    
	}
	
	protected void setTitle() {
	    setSharedVariable("title", getTitle());	    
	}

    protected String getTitle() { 
    	return null; 
    }
    
    protected String getBody() {
    	return null;
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
    	String body = mergeToTemplate(templateName, map).toString();
    	extractLinkTagsFromBody(body);
    	return body;
    }
 
    // This is the only way to do this in FreeMarker. We cannot: (1) put a sequence of stylesheets in the template
    // context which the template can add to, because the template cannot call methods on the container. The template
    // can create a container but not add to one.
    // (2) create a sequence of stylesheets or a scalar to hold the name of a stylesheet in the template, because
    // it does not get passed back to the controller. The template can create only local variables.
    
    // *** RY But we can create a view object with an add method, that the templates could use to add to the
    // list. ***
    private String extractLinkTagsFromBody(String body) {
        List<String> links = new ArrayList<String>();
        
        String re = "<link[^>]*>";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            links.add(matcher.group());
        }

        root.put("stylesheets", links);  // SIDE-EFFECT
        
        body = matcher.replaceAll("");
        return body;
    }
    
    protected void write(HttpServletResponse response) {

        String templateName = "page/default.ftl";
        StringWriter sw = mergeToTemplate(templateName, root);          
        try {
            PrintWriter out = response.getWriter();
            out.print(sw);     
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }                    
    }
    
    protected void callSuperGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            super.doGet(request,response);   
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }       
    }
    
    // RY This needs to be broken out as is for FreeMarkerComponentGenerator, which should not
    // include callSuperGet(). So it's only temporary.
    protected void doSetup(HttpServletRequest request, HttpServletResponse response) {
 
        vreq = new VitroRequest(request);
        this.response = response;
        portal = vreq.getPortal(); 

        // RY Can this be removed? Do templates need it? Ideally, they should not.
        // Only needed for some weird stuff in search box that I think is only used in old default theme.
        int portalId = portal.getPortalId();
        try {
            config.setSharedVariable("portalId", portalId);
        } catch (TemplateModelException e) {
            log.error("Can't set shared variable 'portalId'.");
        } 
        
        appName = portal.getAppName();
        try {
            config.setSharedVariable("appName", appName);
        } catch (TemplateModelException e) {
            log.error("Can't set shared variable 'appName'.");
        } 

        setTemplateLoader();
        
        TabMenu menu = getTabMenu(portalId);
        root.put("tabMenu", menu);

        ApplicationBean appBean = vreq.getAppBean();
        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao());
        
        // We'll need to separate theme-general and theme-specific stylesheet
        // dirs, so we need either two attributes or a list.
        String themeDir = portal.getThemeDir();
        String stylesheetDir = getUrl(themeDir + "css/");
        try {
            config.setSharedVariable("stylesheetDir", stylesheetDir);
        } catch (TemplateModelException e) {
            log.error("Can't set shared variable 'stylesheetDir'.");
        } 
        
        root.put("siteName", portal.getAppName());
        root.put("tagline", portal.getShortHand());
        root.put("breadcrumbs", BreadCrumbsUtil.getBreadCrumbsDiv(request));
        
        setUrls(portalId, themeDir);
        setLoginInfo();      
        setCopyrightInfo();
    }
    
    // Define the URLs that are accessible to the templates. Note that we do not create menus here,
    // because we want the templates to be free to define the link text and where the links are displayed.
    private final void setUrls(int portalId, String themeDir) {
        // The urls that are accessible to the templates. 
        // NB We are not using our menu object mechanism to build menus here, because we want the 
        // view to control which links go where, and the link text and title.
        Map<String, String> urls = new HashMap<String, String>();
        
        String homeUrl = (portal.getRootBreadCrumbURL()!=null && portal.getRootBreadCrumbURL().length()>0) ?
                portal.getRootBreadCrumbURL() : vreq.getContextPath()+"/";
        urls.put("home", homeUrl);

        String bannerImage = portal.getBannerImage();
        if ( ! StringUtils.isEmpty(bannerImage)) {
            root.put("bannerImage", getUrl(themeDir + "site_icons/" + bannerImage));
        }
        
        Map<String, String> portalParam = new HashMap<String, String>();
        portalParam.put("home", "" + portalId);

        urls.put("about", getUrl(Router.ABOUT, portalParam));
        if (ContactMailServlet.getSmtpHostFromProperties() != null) {
            urls.put("contact", getUrl(Router.CONTACT, portalParam));
        }
        urls.put("search", getUrl(Router.SEARCH));
        urls.put("termsOfUse", getUrl(Router.TERMS_OF_USE, portalParam));        
        urls.put("login", getUrl(Router.LOGIN));
        urls.put("logout", getUrl(Router.LOGOUT));
        urls.put("siteAdmin", getUrl(Router.SITE_ADMIN));     
        
        root.put("urls", urls);
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

    // Define template locations. Template loader will look first in the theme-specific
    // location, then in the vitro location.
    // RY We cannot do this in FreeMarkerSetup because (a) the theme depends on the portal,
    // and we have multi-portal installations, and (b) we need to support theme-switching on the fly.
    // To make more efficient, we could do this once, and then have a listener that does it again 
    // when theme is switched.BUT this doesn't support (a), only (b), so  we have to do it on every request.
	protected final void setTemplateLoader() {
	    
	    String themeTemplateDir = context.getRealPath(portal.getThemeDir()) + "/ftl";
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
    
	private TabMenu getTabMenu(int portalId) {
	    return new TabMenu(vreq, portalId);
	}

    // TEMPORARY for transition from JSP to FreeMarker. Once transition
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
    
    
    /* ******************** Utilities ******************* */

    public static String getUrl(String path) {
        if ( ! path.startsWith("/") ) {
            path = "/" + path;
        }
        return contextPath + path;
    }
    
    public static String getUrl(String path, Map<String, String> params) {
        String url = getUrl(path);
        
        Iterator<String> i = params.keySet().iterator();
        String key, value;
        String glue = "?";
        while (i.hasNext()) {
            key = i.next();
            value = params.get(key);
            url += glue + key + "=" + urlEncode(value);
            glue = "&";
        }
        
        return url;
    }

    public static String urlEncode(String url) {
        String encoding = "ISO-8859-1";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(url, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding url " + url + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return encodedUrl;
    }

    
}