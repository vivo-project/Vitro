/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.template.stringtemplate;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.antlr.stringtemplate.*;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
//import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
//import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;

public class Page {
	
    /* Template library */
    protected static StringTemplateGroup templates =
        new StringTemplateGroup("stGroup", "vitro-core/webapp/web/templates/stringtemplates");

    static {
        templates.setRefreshInterval(0); // don't cache templates
    }
    
	protected Portal portal;
//	protected List<String> stylesheets = new ArrayList<String>();
//	protected List<String> scripts = new ArrayList<String>();

	ServletContext servletContext;
    VitroRequest request;
    HttpServletResponse response;
    PrintWriter out;
       
    public Page(ServletContext servletContext, Portal portal) {
    	this.servletContext = servletContext;
    	this.portal = portal;
    }

    public void generate() throws IOException {
        out = response.getWriter();
  
        StringTemplate pageST = templates.getInstanceOf("page");

        String loginName = getLoginName(request); 
        if (loginName == null) {
        	pageST.setAttribute("loginUrl", getUrl(Controllers.LOGIN));
        }
        else {
        	pageST.setAttribute("loginName", loginName);
        	pageST.setAttribute("logoutUrl", getUrl(Controllers.LOGOUT));
        	pageST.setAttribute("siteAdminUrl", getUrl(Controllers.SITE_ADMIN));
        }
 
        int portalId = portal.getPortalId();
        pageST.setAttribute("portalId", portalId);

        pageST.setAttribute("title", getTitle());
        
        pageST.setAttribute("tabMenu", getTabMenu(request));
        
        // We'll need to separate theme-general and theme-specific stylesheet
        // dirs, so we need either two attributes or a list.
        String themeDir = portal.getThemeDir();
        String stylesheetDir = getUrl(themeDir + "css/");
        pageST.setAttribute("stylesheetDir", stylesheetDir);

//        setStylesheets();
//        pageST.setAttribute("stylesheets", stylesheets);
//        
//        setScripts();
//        pageST.setAttribute("scripts", scripts);
        
        pageST.setAttribute("siteName", portal.getAppName());
        
        pageST.setAttribute("homeUrl", portal.getRootBreadCrumbURL());
        pageST.setAttribute("tagline", portal.getShortHand());
         
        String bannerImage = portal.getBannerImage();
        if ( ! StringUtils.isBlank(bannerImage)) {
        	pageST.setAttribute("bannerImageUrl", getUrl(themeDir + "site_icons/" + bannerImage));
        }
        
        pageST.setAttribute("aboutUrl", getUrl(Controllers.ABOUT + "?home=" + portalId));
        pageST.setAttribute("aboutStUrl", getUrl(Controllers.ABOUT + "-stringtemplate?home=" + portalId));
    	// RY Change constants in Controllers from *_JSP to *_URL
        pageST.setAttribute("contactUrl", getUrl(Controllers.CONTACT_JSP));
        
        String copyrightText = portal.getCopyrightAnchor();
        if ( ! StringUtils.isBlank(copyrightText) ) {
        	pageST.setAttribute("copyrightText", copyrightText);
        	pageST.setAttribute("copyrightYear", Calendar.getInstance().get(Calendar.YEAR));
        	pageST.setAttribute("copyrightUrl", portal.getCopyrightURL());
        }
        
        pageST.setAttribute("termsOfUseUrl", getUrl("/termsOfUse?home=" + portalId));

        StringTemplate bodyST = body();
        pageST.setAttribute("body", bodyST);
        
        // Render the page
        String page = pageST.toString(); 
        out.print(page);
    }

    public StringTemplate body() { 
    	return null; 
    }

    public String getTitle() { 
    	return null; 
    }
    
//    public void setStylesheets() {
//    	//stylesheets.add
//    }
//
//    public void setScripts() {
//    	//scripts.add
//    }
    
    public void setRequest(VitroRequest request) {
    	this.request = request;
    }
    
    public void setResponse(HttpServletResponse response) {
    	this.response = response;
    }
    
    private final String getLoginName(HttpServletRequest request) {
    	
        String loginName = null;
        HttpSession session = request.getSession();
        LoginFormBean loginBean = (LoginFormBean) session.getAttribute("loginHandler");
        if (loginBean != null && loginBean.testSessionLevel(request) > -1) {
            loginName = loginBean.getLoginName();
        }   
        return loginName;

    }   
    
//    protected String getUrl(String path, HashMap<String, String> params) {
//    	String url = getUrl(path);
//    	
//    	if (params.size() > 0) {
//        	Iterator i = params.keySet().iterator();
//        	String key;
//        	String glue;
//        	int counter = 1;
//        	do {       		
//        		key = (String) i.next();
//        		glue = counter > 1 ? "&" : "?";
//        		url += glue + key + "=" + params.get(key);
//        		counter++;
//        	} while (i.hasNext());
//    	}
//        return url;       
//    }
    
    protected String getUrl(String path) {
    	String contextPath = servletContext.getContextPath();
    	String url = path;
    	if ( ! url.startsWith("/") ) {
    		url = "/" + url;
    	}
    	return contextPath + url;
    }

    private List<TabMenuItem> getTabMenu(HttpServletRequest request) {
    	List<TabMenuItem> tabMenu = new ArrayList<TabMenuItem>();
    	
// NB Tabs are not generated dynamically in the current code, they are simply hard-coded in menu.jsp.
// Needs to be fixed later.
//    	List primaryTabs = request.getWebappDaoFactory().getTabDao().getPrimaryTabs(portalId);
//    	
//        int tabId = TabWebUtil.getTabIdFromRequest(request); 
//        int rootId = TabWebUtil.getRootTabId(request); 
//        List tabLevels = request.getWebappDaoFactory().getTabDao().getTabHierarcy(tabId,rootId);
//        request.setAttribute("tabLevels", tabLevels);
//        
//        String uri = (String)request.getAttribute("javax.servlet.forward.request_uri");
    	
    	tabMenu.add(new TabMenuItem("Home", "index.jsp?primary=1"));
    	tabMenu.add(new TabMenuItem("Index", "browsecontroller"));
    	tabMenu.add(new TabMenuItem("Index - JSP", "browsecontroller-jsp"));
    	tabMenu.add(new TabMenuItem("Index - ST", "browsecontroller-stringtemplate"));
    	tabMenu.add(new TabMenuItem("Index - FM", "browsecontroller-freemarker"));
    	tabMenu.add(new TabMenuItem("Index - Velocity", "browsecontroller-velocity"));   	
    	tabMenu.add(new TabMenuItem("Index - Wicket", "browsecontroller-wicket"));

    	return tabMenu;
    }
    
    private class TabMenuItem {
    	String linkText;
    	String url;
    	boolean active = false;
    	
    	public TabMenuItem(String linkText, String path) {
    		Page page = Page.this;
    		this.linkText = linkText;
    		url = page.getUrl(path);
    		
    		HttpServletRequest request = page.request;
    		String requestUrl = request.getServletPath();
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
