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
import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class Classes2ClassesOperationController extends BaseEditController {

    private static final Log log = LogFactory.getLog(Classes2ClassesOperationController.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
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
            log.debug("null epo");
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
            return;
        }

        Classes2ClassesDao dao = request.getFullWebappDaoFactory().getClasses2ClassesDao();
        VClassDao vcDao = request.getFullWebappDaoFactory().getVClassDao();
        
        String modeStr = request.getParameter("opMode");
        modeStr = (modeStr == null) ? "" : modeStr;
        
        if (request.getParameter("_cancel") == null) {
        
	        try {
	            if (request.getParameter("operation").equals("remove")) {
	                String[] subclassURIstrs = request.getParameterValues("SubclassURI");
	                if ((subclassURIstrs != null) && (subclassURIstrs.length > 1)) {
	                    String superclassURIstr = request.getParameter("SuperclassURI");
	                    if (superclassURIstr != null) {
	                        for (int i=0; i<subclassURIstrs.length; i++) {
	                        	if (modeStr.equals("disjointWith")) {
	                        		vcDao.removeDisjointWithClass(superclassURIstr, subclassURIstrs[i]);
	                        	} else if (modeStr.equals("equivalentClass")) {
	                        		vcDao.removeEquivalentClass(superclassURIstr, subclassURIstrs[i]);
	                        	} else {
		                            Classes2Classes c2c = new Classes2Classes();
		                            c2c.setSubclassURI(subclassURIstrs[i]);
		                            c2c.setSuperclassURI(superclassURIstr);
		                            dao.deleteClasses2Classes(c2c);
	                        	}
	                        }
	                    }
	                } else {
	                    String subclassURIstr = subclassURIstrs[0];
	                    String[] superclassURIstrs = request.getParameterValues("SuperclassURI");
	                    if (superclassURIstrs != null) {
	                        for (int i=0; i<superclassURIstrs.length; i++) {
	                        	if (modeStr.equals("disjointWith")) {
	                        		vcDao.removeDisjointWithClass(superclassURIstrs[i],subclassURIstr);
		                        } else if (modeStr.equals("equivalentClass")) {
		                        	vcDao.removeEquivalentClass(subclassURIstr,superclassURIstrs[i]);
		                        } else {
		                            Classes2Classes c2c = new Classes2Classes();
		                            c2c.setSuperclassURI(superclassURIstrs[i]);
		                            c2c.setSubclassURI(subclassURIstr);
		                            dao.deleteClasses2Classes(c2c);
	                        	}
	                        }
	                    }
	                }
	            } else if (request.getParameter("operation").equals("add")) {
	            	if (modeStr.equals("disjointWith")) {
	            		vcDao.addDisjointWithClass(request.getParameter("SuperclassURI"), request.getParameter("SubclassURI"));
	            	} else if (modeStr.equals("equivalentClass")) {
	            		vcDao.addEquivalentClass(request.getParameter("SuperclassURI"), request.getParameter("SubclassURI"));
	            	} else {
		            	Classes2Classes c2c = new Classes2Classes();
		                c2c.setSuperclassURI(request.getParameter("SuperclassURI"));
		                c2c.setSubclassURI(request.getParameter("SubclassURI"));
		                dao.insertNewClasses2Classes(c2c);
	            	}
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
