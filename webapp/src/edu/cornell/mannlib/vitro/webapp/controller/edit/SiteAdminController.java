/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class SiteAdminController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(SiteAdminController.class.getName());

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            super.doGet(request,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        VitroRequest vreq = new VitroRequest(request);

        EditProcessObject epo = super.createEpo(request);
        FormObject foo = new FormObject();
        HashMap optionMap = new HashMap();
        
        List classGroups = vreq.getWebappDaoFactory().getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals
        
        Iterator classGroupIt = classGroups.iterator();
        ListOrderedMap optGroupMap = new ListOrderedMap();
        while (classGroupIt.hasNext()) {
            VClassGroup group = (VClassGroup)classGroupIt.next();
            List classes = group.getVitroClassList();
            optGroupMap.put(group.getPublicName(),FormUtils.makeOptionListFromBeans(classes,"URI","PickListName",null,null,false));
        }
        optionMap.put("VClassId", optGroupMap);
        foo.setOptionLists(optionMap);
        epo.setFormObject(foo);

        if ( (200 <= getWebappDaoFactory().getLanguageProfile()) && (getWebappDaoFactory().getLanguageProfile() < 300) ) {
            request.setAttribute("languageModeStr", "OWL Mode" );        	
        } else if ( 100 == getWebappDaoFactory().getLanguageProfile() ) {
            request.setAttribute("languageModeStr", "RDF Schema Mode" );        	
        } 
        
        
        LoginFormBean loginHandler = (LoginFormBean)request.getSession().getAttribute("loginHandler");
        if( loginHandler != null ){
            String status = loginHandler.getLoginStatus();
            if ( "authenticated".equals(status) ) {
                int securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
                if(securityLevel >= loginHandler.CURATOR ){
                    String verbose = request.getParameter("verbose");
                    if( "true".equals(verbose)) {
                        request.getSession().setAttribute(VERBOSE, Boolean.TRUE);
                    } else if( "false".equals(verbose)) {
                        request.getSession().setAttribute(VERBOSE, Boolean.FALSE);
                    }
                }
            }
        }
        
        request.setAttribute("singlePortal",new Boolean(vreq.getWebappDaoFactory().getPortalDao().isSinglePortal()));
        
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);        
        request.setAttribute("bodyJsp","/siteAdmin/siteAdminMain.jsp");
        request.setAttribute("scripts","/siteAdmin/siteAdminScripts.jsp");
        request.setAttribute("title",((Portal)request.getAttribute("portalBean")).getAppName() + " Site Administration");
        request.setAttribute("epoKey",epo.getKey());
        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("SiteAdminController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request,response);
    }

    public static final String VERBOSE = "verbosePropertyListing";
}
