/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManagePortals;
import edu.cornell.mannlib.vitro.webapp.beans.TabIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.TabIndividualRelationDao;

public class TabIndividualRelationOperationController extends
        BaseEditController {
	
	private static final Log log = LogFactory.getLog(TabIndividualRelationOperationController.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new ManagePortals()))) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);
    	String defaultLandingPage = getDefaultLandingPage(request);
    	
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

        TabIndividualRelationDao dao = request.getFullWebappDaoFactory().getTabs2EntsDao();

        try {
            if (request.getParameter("operation").equals("remove")) {
                String[] tirURIstrs = request.getParameterValues("TirURI");
                if ((tirURIstrs != null) && (tirURIstrs.length > 0)) {
                        for (int i=0; i<tirURIstrs.length; i++) {
                            TabIndividualRelation tir = dao.getTabIndividualRelationByURI(tirURIstrs[i]);
                            dao.deleteTabIndividualRelation(tir);
                        }
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
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
