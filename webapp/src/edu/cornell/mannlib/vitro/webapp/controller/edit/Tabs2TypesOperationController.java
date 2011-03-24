package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.TabVClassRelation;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.TabVClassRelationDao;

public class Tabs2TypesOperationController extends BaseEditController {

    private static final Log log = LogFactory.getLog(Tabs2TypesOperationController.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse response) {
    	
    	VitroRequest request = new VitroRequest(req);
    	String defaultLandingPage = getDefaultLandingPage(request);
    	
        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" encountered exception calling super.doGet()");
        }

        HashMap epoHash = null;
        EditProcessObject epo = null;
        try {
            epoHash = (HashMap) request.getSession().getAttribute("epoHash");
            epo = (EditProcessObject) epoHash.get(request.getParameter("_epoKey"));
        } catch (NullPointerException e) {
            //session or edit process expired
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException f) {
                e.printStackTrace();
            }
            return;
        }

        if (epo == null) {
            log.error("null epo");
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        TabVClassRelationDao dao = getWebappDaoFactory().getTabs2TypesDao();

        if (request.getParameter("_cancel") == null) {
	        try {
	            if (request.getParameter("operation").equals("remove")) {
	                String[] typeURIstrs = request.getParameterValues("TypeURI");
	                if ((typeURIstrs != null) && (typeURIstrs.length > 0)) {
	                    String tabIdStr = request.getParameter("TabId");
	                    if (tabIdStr != null) {
	                        for (int i=0; i<typeURIstrs.length; i++) {
	                            TabVClassRelation t2t = new TabVClassRelation();
	                            t2t.setTabId(Integer.decode(tabIdStr));
	                            t2t.setVClassURI(typeURIstrs[i]);
	                            dao.deleteTabVClassRelation(t2t);
	                        }
	                    }
	                }
	            } else if (request.getParameter("operation").equals("add")) {
	                TabVClassRelation t2t = new TabVClassRelation();
	                t2t.setTabId(Integer.decode(request.getParameter("TabId")));
	                t2t.setVClassURI(request.getParameter("TypeURI"));
	                dao.insertTabVClassRelation(t2t);
	            }
	        } catch (Exception e) {
	            //e.printStackTrace();
	        }
        }

        //if no page forwarder was set, just go back to referring page:
        //the referer stuff all will be changed so as not to rely on the HTTP header
        String referer = request.getHeader("REFERER");
        if (referer == null) {
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                response.sendRedirect(referer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

}