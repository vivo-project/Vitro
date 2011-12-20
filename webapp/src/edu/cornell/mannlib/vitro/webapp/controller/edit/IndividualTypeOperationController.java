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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoBackEndEditing;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

public class IndividualTypeOperationController extends BaseEditController {

    private static final Log log = LogFactory.getLog(IndividualTypeOperationController.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new DoBackEndEditing()))) {
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
                log.error(f, f);
                throw new RuntimeException(f);
            }
            return;
        }

        if (epo == null) {
            log.error("null epo");
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
            return;
        }

        IndividualDao dao = request.getAssertionsWebappDaoFactory().getIndividualDao();
        
        if (request.getParameter("_cancel") == null) {
	        try {
	            if (request.getParameter("operation").equals("remove")) {
	                String[] typeURIstrs = request.getParameterValues("TypeURI");
	                String individualURIstr = request.getParameter("individualURI");
	                    if (individualURIstr != null) {
	                        for (int i=0; i<typeURIstrs.length; i++) {                        	
	                            dao.removeVClass(individualURIstr, typeURIstrs[i]);
	                        }
	                    }
	            } else if (request.getParameter("operation").equals("add")) {
		                dao.addVClass(request.getParameter("individualURI"),request.getParameter("TypeURI"));
	            }
	        } catch (Exception e) {
	            log.error(e, e);
	        }
        }

        //if no page forwarder was set, just go back to referring page:
        //the referer stuff all will be changed so as not to rely on the HTTP header
        
        String referer = epo.getReferer();
        if (referer == null) {
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
        } else {
            try {
                response.sendRedirect(referer);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
        }

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }
	
}
