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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao;

public class Properties2PropertiesOperationController extends
		BaseEditController {
	
	private static final Log log = LogFactory.getLog(Properties2PropertiesOperationController.class.getName());

	private static final boolean ADD = false;
	private static final boolean REMOVE = true;
	
    public void doPost(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
        	return;
        }
    	
    	String defaultLandingPage = getDefaultLandingPage(req);
    	
    	try {
			VitroRequest request = new VitroRequest(req);
		    
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
		        log.debug("null epo");
		        try {
		            response.sendRedirect(defaultLandingPage);
		        } catch (IOException e) {
		            log.error(e, e);
		        }
		        return;
		    }
		    
		    // get parameters from request
		   	        
		    String modeStr = request.getParameter("opMode");
		    modeStr = (modeStr == null) ? "" : modeStr;
		    String operationStr = request.getParameter("operation");
		    boolean operation = false;
		    if ("add".equals(operationStr)) {
		    	operation = ADD;
		    } else if ("remove".equals(operationStr)) {
		    	operation = REMOVE;
		    } else {
		    	throw new IllegalArgumentException(
		    	    "request parameter 'operation' must have value 'add' or 'remove'");
		    }
		    	    
		    if (request.getParameter("_cancel") == null) {
		    	doEdit(modeStr, operation, request);
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

	    } catch (RuntimeException e) {
	        log.error("Unable to perform edit operation: ", e);
	        throw e;
	    } catch (Error err) {
	    	log.error("Unable to perform edit operation: ", err);
	    	throw err;
	    }
        
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	
    	String defaultLandingPage = getDefaultLandingPage(request);
    	
    	try {
            response.sendRedirect(defaultLandingPage);
        } catch (IOException e) {
            log.error("Unable to redirect to default landing page", e);
        }
    }
    
    private void doEdit(String modeStr, boolean operation, VitroRequest request) {
    	PropertyDao opDao = request.getFullWebappDaoFactory().getObjectPropertyDao();
   	 
        if (operation == REMOVE) {
            String[] subpropertyURIstrs = request.getParameterValues("SubpropertyURI");
            if ((subpropertyURIstrs != null) && (subpropertyURIstrs.length > 1)) {
                String superpropertyURIstr = request.getParameter("SuperpropertyURI");
                if (superpropertyURIstr != null) {
                    for (int i=0; i<subpropertyURIstrs.length; i++) {
                    	if (modeStr.equals("equivalentProperty")) {
                    		opDao.removeEquivalentProperty(superpropertyURIstr, subpropertyURIstrs[i]);
                    	} else {
                    		opDao.removeSuperproperty(subpropertyURIstrs[i], superpropertyURIstr);
                    	}
                    }
                }
            } else {
                String subpropertyURIstr = subpropertyURIstrs[0];
                String[] superpropertyURIstrs = request.getParameterValues("SuperpropertyURI");
                if (superpropertyURIstrs != null) {
                    for (int i=0; i<superpropertyURIstrs.length; i++) {
                    	if (modeStr.equals("equivalentProperty")) {
                        	opDao.removeEquivalentProperty(subpropertyURIstr,superpropertyURIstrs[i]);
                        } else {
                        	opDao.removeSuperproperty(subpropertyURIstr,superpropertyURIstrs[i]);
                    	}
                    }
                }
            }
        } else if (operation == ADD) {
        	if (modeStr.equals("equivalentProperty")) {
        		opDao.addEquivalentProperty(request.getParameter("SuperpropertyURI"), request.getParameter("SubpropertyURI"));
        	} else {
        		opDao.addSuperproperty(request.getParameter("SubpropertyURI"), request.getParameter("SuperpropertyURI"));
        	}
        }
    
    }
	
}
