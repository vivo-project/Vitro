/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.view.menu.MainMenuItem;
import edu.cornell.mannlib.vitro.webapp.view.menu.Menu;
import edu.cornell.mannlib.vitro.webapp.view.menu.TabMenu;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

public class FreeMarkerHttpServlet extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(FreeMarkerHttpServlet.class.getName());
    private static final int FILTER_SECURITY_LEVEL = LoginFormBean.EDITOR;
    public static Configuration config = null;
    
	protected VitroRequest vreq;
	protected HttpServletResponse response;
	protected Portal portal;
	protected Map<String, Object> root = new HashMap<String, Object>();
	
	// Some servlets have their own doGet() method, in which case they need to call 
	// doSetup(), setTitle(), setBody(), and write() themselves. Other servlets define only
	// a getBody() and getTitle() method and use the parent doGet() method.
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {
    	try {
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

        String templateName = "page.ftl";
        StringWriter sw = mergeToTemplate(templateName, root);          
        try {
            PrintWriter out = response.getWriter();
            out.print(sw);     
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }                    
    }
    
    protected void doSetup(HttpServletRequest request, HttpServletResponse response) {

        try {
            super.doGet(request,response);
        } catch (ServletException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
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
        
        // RY Remove this. Templates shouldn't use it. ViewObjects will use it.
        try {
            config.setSharedVariable("contextPath", vreq.getContextPath());
        } catch (TemplateModelException e) {
            log.error("Can't set shared variable 'contextPath'.");
        }        

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

        urls.put("about", getUrl(Controllers.ABOUT + "?home=" + portalId));
        urls.put("aboutFM", getUrl(Controllers.ABOUT + "-fm?home=" + portalId)); // TEMPORARY
        if (ContactMailServlet.getSmtpHostFromProperties() != null) {
            urls.put("contact", getUrl(Controllers.CONTACT_URL + "?home=" + portalId));
        }
        urls.put("search", getUrl(Controllers.SEARCH_URL));
        urls.put("termsOfUse", getUrl("/termsOfUse?home=" + portalId));        
        urls.put("login", getUrl(Controllers.LOGIN));
        urls.put("logout", getUrl(Controllers.LOGOUT));
        urls.put("siteAdmin", getUrl(Controllers.SITE_ADMIN));     
        
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
	
	protected String getUrl(String path) {
		String contextPath = vreq.getContextPath();
		if ( ! path.startsWith("/") ) {
			path = "/" + path;
		}
		return contextPath + path;
	}
	
	private TabMenu getTabMenu(int portalId) {
	    return new TabMenu(vreq, portalId);
	}
	
}