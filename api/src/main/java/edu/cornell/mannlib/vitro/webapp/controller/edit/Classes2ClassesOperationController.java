/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

@WebServlet(name = "Classes2ClassesOperationController", urlPatterns = {"/classes2ClassesOp"} )
public class Classes2ClassesOperationController extends BaseEditController {

    private static final Log log = LogFactory.getLog(Classes2ClassesOperationController.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
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

        VClassDao vcDao = request.getLanguageNeutralWebappDaoFactory().getVClassDao();

        String modeStr = request.getParameter("opMode");
        modeStr = (modeStr == null) ? "" : modeStr;

        if (request.getParameter("_cancel") == null) {

	        try {
	            if (request.getParameter("operation").equals("remove")) {
	                String[] subclassURIstrs = request.getParameterValues("SubclassURI");
	                if ((subclassURIstrs != null) && (subclassURIstrs.length > 1)) {
	                    String superclassURIstr = request.getParameter("SuperclassURI");
	                    if (superclassURIstr != null) {
                            for (String subclassURIstr : subclassURIstrs) {
                                switch (modeStr) {
                                    case "disjointWith":
                                        vcDao.removeDisjointWithClass(superclassURIstr, subclassURIstr);
                                        break;
                                    case "equivalentClass":
                                        vcDao.removeEquivalentClass(superclassURIstr, subclassURIstr);
                                        break;
                                    default:
                                        Classes2Classes c2c = new Classes2Classes();
                                        c2c.setSubclassURI(subclassURIstr);
                                        c2c.setSuperclassURI(superclassURIstr);
                                        vcDao.deleteClasses2Classes(c2c);
                                        break;
                                }
                            }
	                    }
	                } else {
	                    String subclassURIstr = subclassURIstrs[0];
	                    String[] superclassURIstrs = request.getParameterValues("SuperclassURI");
	                    if (superclassURIstrs != null) {
                            for (String superclassURIstr : superclassURIstrs) {
                                switch (modeStr) {
                                    case "disjointWith":
                                        vcDao.removeDisjointWithClass(superclassURIstr, subclassURIstr);
                                        break;
                                    case "equivalentClass":
                                        vcDao.removeEquivalentClass(subclassURIstr, superclassURIstr);
                                        break;
                                    default:
                                        Classes2Classes c2c = new Classes2Classes();
                                        c2c.setSuperclassURI(superclassURIstr);
                                        c2c.setSubclassURI(subclassURIstr);
                                        vcDao.deleteClasses2Classes(c2c);
                                        break;
                                }
                            }
	                    }
	                }
	            } else if (request.getParameter("operation").equals("add")) {
                    switch (modeStr) {
                        case "disjointWith":
                            vcDao.addDisjointWithClass(request.getParameter("SuperclassURI"), request.getParameter("SubclassURI"));
                            break;
                        case "equivalentClass":
                            vcDao.addEquivalentClass(request.getParameter("SuperclassURI"), request.getParameter("SubclassURI"));
                            break;
                        default:
                            Classes2Classes c2c = new Classes2Classes();
                            c2c.setSuperclassURI(request.getParameter("SuperclassURI"));
                            c2c.setSubclassURI(request.getParameter("SubclassURI"));
                            vcDao.insertNewClasses2Classes(c2c);
                            break;
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
