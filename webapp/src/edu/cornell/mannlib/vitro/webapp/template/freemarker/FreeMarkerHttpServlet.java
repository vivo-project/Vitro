/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.template.freemarker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

public class FreeMarkerHttpServlet extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(FreeMarkerHttpServlet.class.getName());
    private static final int FILTER_SECURITY_LEVEL = LoginFormBean.EDITOR;
    public static Configuration config = null;
    
	protected VitroRequest vreq;
	protected Portal portal;
	protected Map root = new HashMap();
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {
    	try {
	        super.doGet(request,response);
	        
	        PrintWriter out = response.getWriter();
	        vreq = new VitroRequest(request);
	        portal = vreq.getPortal();       

	        setLoginInfo();
	        
	        int portalId = portal.getPortalId();
	        root.put("portalId", portalId);
	        
	        // Make title available to all templates
	        try {
	        	config.setSharedVariable("title", getTitle());
	        } catch (TemplateModelException e) {
	        	// RY change to a logging statement
	        	System.out.println("Can't set shared variable 'title'.");
	        }

	        // FreeMarker does this wrapping automatically - no need to create the
	        // SimpleSequence directly.
	        //SimpleSequence menu = getTabMenu(portalId);
	        List<TabMenuItem> menu = getTabMenu(portalId);
	        root.put("tabMenu", menu);

	        ApplicationBean appBean = vreq.getAppBean();
	        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
	        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao());
	        
	        // We'll need to separate theme-general and theme-specific stylesheet
	        // dirs, so we need either two attributes or a list.
	        String themeDir = portal.getThemeDir();
	        String stylesheetDir = getUrl(themeDir + "css/");
            // Make stylesheetDir available to all templates
            try {
                config.setSharedVariable("stylesheetDir", stylesheetDir);
            } catch (TemplateModelException e) {
                // RY change to a logging statement
                System.out.println("Can't set shared variable 'stylesheetDir'.");
            } 
            
	        root.put("siteName", portal.getAppName());

	        String homeURL = (portal.getRootBreadCrumbURL()!=null && portal.getRootBreadCrumbURL().length()>0) ?
	        		portal.getRootBreadCrumbURL() : vreq.getContextPath()+"/";
	        root.put("homeUrl", homeURL);
	        root.put("tagline", portal.getShortHand());
	         
	        String bannerImage = portal.getBannerImage();
	        if ( ! StringUtils.isBlank(bannerImage)) {
	        	root.put("bannerImageUrl", getUrl(themeDir + "site_icons/" + bannerImage));
	        }
	        
	        root.put("aboutUrl", getUrl(Controllers.ABOUT + "?home=" + portalId));
	        root.put("aboutStUrl", getUrl(Controllers.ABOUT + "-stringtemplate?home=" + portalId));
	        //root.put("aboutStgfUrl", getUrl(Controllers.ABOUT + "-stringtemplategroupfile?home=" + portalId));        
	        root.put("aboutVUrl", getUrl(Controllers.ABOUT + "-velocity?home=" + portalId));
	        root.put("aboutFMUrl", getUrl(Controllers.ABOUT + "-freemarker?home=" + portalId));
	        // RY Change constants in Controllers from *_JSP to *_URL
	        root.put("contactUrl", getUrl(Controllers.CONTACT_JSP));
	        
	        root.put("searchUrl", getUrl(Controllers.SEARCH_URL));
	        
	        String copyrightText = portal.getCopyrightAnchor();
	        if ( ! StringUtils.isBlank(copyrightText) ) {
	        	root.put("copyrightText", copyrightText);
	        	root.put("copyrightYear", Calendar.getInstance().get(Calendar.YEAR));
	        	root.put("copyrightUrl", portal.getCopyrightURL());
	        }
	        
	        root.put("termsOfUseUrl", getUrl("/termsOfUse?home=" + portalId));
	         
	        HttpSession session = vreq.getSession();
	        try {
	            config.setSharedVariable("mySession", session);
	        } catch (TemplateModelException e) {
	            // RY change to a logging statement
	            System.out.println("Can't set shared variable 'mySession'.");
	        } 
            // This value is changed by the template when session is put in the template context
            LoginFormBean loginHandler = (LoginFormBean)session.getAttribute("loginHandler");
            if (loginHandler != null) {
                System.out.println("FreeMarker SESSION LOGINNAME = " + loginHandler.getLoginName());
            } else {
                System.out.println("FreeMarker SESSION: not logged in");
            }
	        
	        // Get page-specific body content
	        //root.put("body", getBody());
	        String body = getBody();
	        // extract link tags, add to a list, put the list in the root
	        body = extractLinkTagsFromBody(body);
	        root.put("body", body);            
	        String templateName = "page.ftl";
	        StringWriter sw = mergeToTemplate(templateName, root);   
	        
	        out.print(sw);
       
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
	
    protected String getTitle() { 
    	return null; 
    }
    
    protected String getBody() {
    	return null;
    }
    
    protected StringWriter mergeToTemplate(String templateName, Map map) {
    	
        Template template = null;
        try {
        	template = config.getTemplate(templateName);
        } catch (IOException e) {
        	// RY Change to a logging statement
        	System.out.println("Cannot get template " + templateName);
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

    protected String mergeBodyToTemplate(String templateName, Map map) {
    	return mergeToTemplate(templateName, map).toString();
    }
    
	//  public void setStylesheets() {
	//	//stylesheets.add
	//}
	//
	//public void setScripts() {
	//	//scripts.add
	//}
 
    // This is the only way to do this in FreeMarker. We cannot: (1) put a sequence of stylesheets in the template
    // context which the template can add to, because the template cannot call methods on the container. The template
    // can create a container but not add to one.
    // (2) create a sequence of stylesheets or a scalar to hold the name of a stylesheet in the template, because
    // it does not get passed back to the controller. The template can create only local variables.
    private String extractLinkTagsFromBody(String body) {
        List<String> links = new ArrayList<String>();
        
        String re = "<link[^>]*>";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            links.add(matcher.group());
        }

        root.put("stylesheets", links);
        
        body = matcher.replaceAll("");
        return body;
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
	    if (loginName == null) {
	    	root.put("loginUrl", getUrl(Controllers.LOGIN));
	    }
	    else {
	    	root.put("loginName", loginName);
	    	root.put("logoutUrl", getUrl(Controllers.LOGOUT));
	    	root.put("siteAdminUrl", getUrl(Controllers.SITE_ADMIN));
	    	securityLevel = Integer.parseInt(loginBean.getLoginRole());
	    	if (securityLevel >= FILTER_SECURITY_LEVEL) {
	    		ApplicationBean appBean = vreq.getAppBean();
	    		if (appBean.isFlag1Active()) {
	    			root.put("showFlag1SearchField", true);
	    		}
	    	}	    	
	    }       	
	}   
	
//	protected String getUrl(String path, HashMap<String, String> params) {
//		String url = getUrl(path);
//		
//		if (params.size() > 0) {
//	    	Iterator i = params.keySet().iterator();
//	    	String key;
//	    	String glue;
//	    	int counter = 1;
//	    	do {       		
//	    		key = (String) i.next();
//	    		glue = counter > 1 ? "&" : "?";
//	    		url += glue + key + "=" + params.get(key);
//	    		counter++;
//	    	} while (i.hasNext());
//		}
//	    return url;       
//	}
	
	protected String getUrl(String path) {
		String contextPath = vreq.getContextPath();
		String url = path;
		if ( ! url.startsWith("/") ) {
			url = "/" + url;
		}
		return contextPath + url;
	}
	
	/* RY Experimental approach to moving link tags generated in templates to the head element. Another approach
	 * would be to not generate the head from within the main page template, but separately, and after the body 
	 * gets generated. Then move the link tags to an array, to get passed to the template that generates the 
	 * link tags. The page components are assembled by the controller rather than in the page template. Either
	 * way, it's pretty ugly.
	 */
	private String moveLinkTagsToHead(String body) {
	    
	    return null;
	}
	
	private List<TabMenuItem> getTabMenu(int portalId) {
		List<TabMenuItem> tabMenu = new ArrayList<TabMenuItem>();
	
		//Tabs stored in database
		List primaryTabs = vreq.getWebappDaoFactory().getTabDao().getPrimaryTabs(portalId);    	
	    int tabId = TabWebUtil.getTabIdFromRequest(vreq); 
	    int rootId = TabWebUtil.getRootTabId(vreq); 
	    List tabLevels = vreq.getWebappDaoFactory().getTabDao().getTabHierarcy(tabId,rootId);
	    vreq.setAttribute("tabLevels", tabLevels); 
	    Iterator<Tab> primaryTabIterator = primaryTabs.iterator();
	    Iterator tabLevelIterator = tabLevels.iterator();
		Tab tab;
	    while (primaryTabIterator.hasNext()) {
	    	tab = (Tab) primaryTabIterator.next();
	    	tabMenu.add(new TabMenuItem(tab.getTitle(), "index.jsp?primary=" + tab.getTabId()));
	    	// RY Also need to loop through nested tab levels, but not doing that now.
	    }
	    
	    // Hard-coded tabs
		tabMenu.add(new TabMenuItem("Index", "browsecontroller"));
		tabMenu.add(new TabMenuItem("Index - JSP", "browsecontroller-jsp"));
		tabMenu.add(new TabMenuItem("Index - ST", "browsecontroller-stringtemplate"));
		// tabMenu.add(new TabMenuItem("Index - ST GF", "browsecontroller-stringtemplategroupfile"));
		tabMenu.add(new TabMenuItem("Index - Velocity", "browsecontroller-velocity"));
		tabMenu.add(new TabMenuItem("Index - FM", "browsecontroller-freemarker"));   	
		// stabMenu.add(new TabMenuItem("Index - Wicket", "browsecontroller-wicket"));
	
		return tabMenu;
	}
	
	//RY INTERESTING: Velocity cannot access TabMenuItem methods unless the class is public.
	//StringTemplate can. Which behavior is correct?
	public class TabMenuItem {
		String linkText;
		String url;
		boolean active = false;
		
		public TabMenuItem(String linkText, String path) {
			FreeMarkerHttpServlet fs = FreeMarkerHttpServlet.this;
			this.linkText = linkText;
			url = fs.getUrl(path);
			
			String requestUrl = vreq.getServletPath();
			active = requestUrl.equals("/" + path);
		}
		
		public String getLinkText() {
			return linkText; 
		}
		
		public String getUrl() {
			return url;
		}
		
		public boolean isActive() {
			return active;
		}
	}
}

