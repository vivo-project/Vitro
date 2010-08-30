/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Params;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginTemplateHelper;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import freemarker.template.Configuration;

public class FreemarkerSiteAdminController extends FreemarkerHttpServlet {
	
	private static final Log log = LogFactory.getLog(FreemarkerSiteAdminController.class);

    public static final String VERBOSE = "verbosePropertyListing";
    
	public String getTitle(String siteName) {
        return siteName + " Site Administration";
	}

    public String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {

        String loginStatus = null;
        
        LoginFormBean loginHandler = (LoginFormBean)vreq.getSession().getAttribute("loginHandler");
        if (loginHandler != null) {
            loginStatus = loginHandler.getLoginStatus();
        }
        
        // NOT LOGGED IN: just show login form
        if (loginHandler == null || !"authenticated".equals(loginStatus)) {
            body.put("loginPanel", new LoginTemplateHelper(vreq).showLoginPage(vreq, body, config));
            return mergeBodyToTemplate("siteAdmin-main.ftl", body, config);           
        } 
        
        // LOGGED IN: show editing options based on user role
        int securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
        
        WebappDaoFactory wadf = vreq.getFullWebappDaoFactory();
        
        // DATA INPUT
        if (securityLevel >= LoginFormBean.EDITOR) {
            Map<String, Object> dataInputData = new HashMap<String, Object>();
            dataInputData.put("formAction", UrlBuilder.getUrl("/edit/editRequestDispatch.jsp"));
            
            // Create map for data input entry form options list
            List classGroups = wadf.getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals        
            Iterator classGroupIt = classGroups.iterator();
            LinkedHashMap<String, List> orderedClassGroups = new LinkedHashMap<String, List>(classGroups.size());
            while (classGroupIt.hasNext()) {
                VClassGroup group = (VClassGroup)classGroupIt.next();
                List classes = group.getVitroClassList();
                orderedClassGroups.put(group.getPublicName(),FormUtils.makeOptionListFromBeans(classes,"URI","PickListName",null,null,false));
            }
            dataInputData.put("classGroupOptions", orderedClassGroups);
            
            body.put("dataInput", dataInputData);
        }

        // SITE CONFIGURATION
        if (securityLevel >= loginHandler.CURATOR) {
            Map<String, Object> siteConfigData = new HashMap<String, Object>();
            
            boolean singlePortal = new Boolean(vreq.getFullWebappDaoFactory().getPortalDao().isSinglePortal());
            body.put("singlePortal", singlePortal);
            
            Params params = new Params();
            int portalId = vreq.getPortal().getPortalId();
            params.put("home", portalId);

            siteConfigData.put("tabManagementUrl", UrlBuilder.getUrl("/listTabs", params));
            
            if (securityLevel >= loginHandler.DBA) {                
                siteConfigData.put("userManagementUrl", UrlBuilder.getUrl("/listUsers", params));
            }
 
            if (!singlePortal) {
                siteConfigData.put("listPortalsUrl", UrlBuilder.getUrl("/listPortals", params));
            }
            
            params.put("controller", "Portal");
            params.put("id", portalId);        
            siteConfigData.put("siteInfoUrl", UrlBuilder.getUrl("/editForm", params));

            body.put("siteConfig", siteConfigData);
        }
        
        // ONTOLOGY EDITOR

        if (securityLevel >= LoginFormBean.CURATOR) {
            String verbose = vreq.getParameter("verbose");
            if( "true".equals(verbose)) {
                vreq.getSession().setAttribute(VERBOSE, Boolean.TRUE);
            } else if( "false".equals(verbose)) {
                vreq.getSession().setAttribute(VERBOSE, Boolean.FALSE);
            }
        }    
        
        // ADVANCED DATA TOOLS
        
        // CUSTOM REPORTS
                
    



        

        
// Not used
//        int languageProfile = wadf.getLanguageProfile();
//        String languageMode = null;
//        if ( 200 <= languageProfile && languageProfile < 300 ) {
//            languageMode = "OWL Mode";        	
//        } else if ( 100 == languageProfile ) {
//            languageMode = "RDF Schema Mode";
//        } 
//        body.put("languageModeStr",  languageMode);       
        



        return mergeBodyToTemplate("siteAdmin-main.ftl", body, config);
        
    }

}
