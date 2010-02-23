/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.template.velocity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;

public class VelocityHttpServlet extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(VelocityHttpServlet.class.getName());
    private static final int FILTER_SECURITY_LEVEL = LoginFormBean.EDITOR;
    
	protected VitroRequest vreq;
	protected PrintWriter out;
	protected Portal portal;
	protected VelocityContext context;
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {
    	try {
	        super.doGet(request,response);
	        
	        out = response.getWriter();
	        vreq = new VitroRequest(request);
	        portal = vreq.getPortal();
	        context = new VelocityContext();   	        
	        String templateName = "page.vm";
	        	               
	        setLoginInfo();
	        
	        int portalId = portal.getPortalId();
	        context.put("portalId", portalId);
	        
	        context.put("title", getTitle());
	        
	        context.put("tabMenu", getTabMenu(portalId));

	        ApplicationBean appBean = vreq.getAppBean();
	        PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
	        PortalWebUtil.populateNavigationChoices(portal, vreq, appBean, vreq.getWebappDaoFactory().getPortalDao());
	        
	        // We'll need to separate theme-general and theme-specific stylesheet
	        // dirs, so we need either two attributes or a list.
	        String themeDir = portal.getThemeDir();
	        String stylesheetDir = getUrl(themeDir + "css/");
	        context.put("stylesheetDir", stylesheetDir);

//	        setStylesheets();
//	        context.put("stylesheets", stylesheets);
//	        
//	        setScripts();
//	        context.put("scripts", scripts);
	        
	        context.put("siteName", portal.getAppName());
	        
	        context.put("homeUrl", portal.getRootBreadCrumbURL());
	        context.put("tagline", portal.getShortHand());
	         
	        String bannerImage = portal.getBannerImage();
	        if ( ! StringUtils.isBlank(bannerImage)) {
	        	context.put("bannerImageUrl", getUrl(themeDir + "site_icons/" + bannerImage));
	        }
	        
	        context.put("aboutUrl", getUrl(Controllers.ABOUT + "?home=" + portalId));
	        context.put("aboutStUrl", getUrl(Controllers.ABOUT + "-stringtemplate?home=" + portalId));
	        //context.put("aboutStgfUrl", getUrl(Controllers.ABOUT + "-stringtemplategroupfile?home=" + portalId));        
	        context.put("aboutVUrl", getUrl(Controllers.ABOUT + "-velocity?home=" + portalId));
	        // RY Change constants in Controllers from *_JSP to *_URL
	        context.put("contactUrl", getUrl(Controllers.CONTACT_JSP));
	        
	        context.put("searchUrl", getUrl(Controllers.SEARCH_URL));
	        
	        String copyrightText = portal.getCopyrightAnchor();
	        if ( ! StringUtils.isBlank(copyrightText) ) {
	        	context.put("copyrightText", copyrightText);
	        	context.put("copyrightYear", Calendar.getInstance().get(Calendar.YEAR));
	        	context.put("copyrightUrl", portal.getCopyrightURL());
	        }
	        
	        context.put("termsOfUseUrl", getUrl("/termsOfUse?home=" + portalId));
	        
	        // Get page-specific body content
	        context.put("body", getBody());
	
	        StringWriter sw = mergeTemplateToContext(templateName, context);
	        out.print(sw);
	        
	    } catch (Throwable e) {
	        log.error("VelocityHttpServlet could not forward to view.");
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
    
    protected StringWriter mergeTemplateToContext(String templateName, VelocityContext context) {
    	
        Template template = null;
        try {
        	template = Velocity.getTemplate(templateName);
        }
        // RY Change the System.out.println statements to logging statements
        catch (ResourceNotFoundException e) {
        	System.out.println("Can't find template " + templateName);
        }
        catch (ParseErrorException e) {
        	System.out.println("Problem parsing template " + templateName);
        }           	
        catch (Exception e) {
        	System.out.println("Error merging template " + templateName + " to context");
        }

        StringWriter sw = new StringWriter();
        if (template != null) {
        	try {
        		template.merge(context, sw);
        	}
        	catch (IOException e) {
        		System.out.println("Error merging template " + templateName + " to context");
        	}
        }
        return sw;
    }
    
    protected String mergeBodyTemplateToContext(String templateName, VelocityContext context) {
    	return mergeTemplateToContext(templateName, context).toString();
    }

	//  public void setStylesheets() {
	//	//stylesheets.add
	//}
	//
	//public void setScripts() {
	//	//scripts.add
	//}

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
	    	context.put("loginUrl", getUrl(Controllers.LOGIN));
	    }
	    else {
	    	context.put("loginName", loginName);
	    	context.put("logoutUrl", getUrl(Controllers.LOGOUT));
	    	context.put("siteAdminUrl", getUrl(Controllers.SITE_ADMIN));
	    	securityLevel = Integer.parseInt(loginBean.getLoginRole());
	    	if (securityLevel >= FILTER_SECURITY_LEVEL) {
	    		ApplicationBean appBean = vreq.getAppBean();
	    		if (appBean.isFlag1Active()) {
	    			context.put("showFlag1SearchField", true);
	    		}
	    	}
	    	
	    }       
	
	}   
	
	//protected String getUrl(String path, HashMap<String, String> params) {
	//	String url = getUrl(path);
	//	
	//	if (params.size() > 0) {
	//    	Iterator i = params.keySet().iterator();
	//    	String key;
	//    	String glue;
	//    	int counter = 1;
	//    	do {       		
	//    		key = (String) i.next();
	//    		glue = counter > 1 ? "&" : "?";
	//    		url += glue + key + "=" + params.get(key);
	//    		counter++;
	//    	} while (i.hasNext());
	//	}
	//    return url;       
	//}
	
	protected String getUrl(String path) {
		String contextPath = vreq.getContextPath();
		String url = path;
		if ( ! url.startsWith("/") ) {
			url = "/" + url;
		}
		return contextPath + url;
	}
	
	private List<TabMenuItem> getTabMenu(int portalId) {
		List<TabMenuItem> tabMenu = new ArrayList<TabMenuItem>();
		
		// Tabs stored in database
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
		//tabMenu.add(new TabMenuItem("Index - ST GF", "browsecontroller-stringtemplategroupfile"));
		tabMenu.add(new TabMenuItem("Index - Velocity", "browsecontroller-velocity"));
		tabMenu.add(new TabMenuItem("Index - FM", "browsecontroller-freemarker"));   	
		tabMenu.add(new TabMenuItem("Index - Wicket", "browsecontroller-wicket"));
	
		return tabMenu;
	}
	
	// RY INTERESTING: Velocity cannot access TabMenuItem methods unless the class is public.
	// StringTemplate can. Which behavior is correct?
	public class TabMenuItem {
		String linkText;
		String url;
		boolean active = false;
		
		public TabMenuItem(String linkText, String path) {
			VelocityHttpServlet pc = VelocityHttpServlet.this;
			this.linkText = linkText;
			url = pc.getUrl(path);
			
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

