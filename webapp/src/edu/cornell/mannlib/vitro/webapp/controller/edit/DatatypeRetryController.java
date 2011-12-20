/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;


public class DatatypeRetryController extends BaseEditController {

	private static final Log log = LogFactory.getLog(DatatypeRetryController.class.getName());
	
    public void doPost (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        DatatypeDao dDao = request.getFullWebappDaoFactory().getDatatypeDao();
        epo.setDataAccessObject(dDao);
        Datatype objectForEditing = null;
        String action = "";

        if (request.getParameter("id") != null) {
            int id = Integer.parseInt(request.getParameter("id"));

            if (id > 0) {
                try {
                    log.debug("Trying to retrieve datatype "+id);
                    objectForEditing = dDao.getDatatypeById(id);
                    action = "update";
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            }
        } else {
            action = "insert";
            objectForEditing = new Datatype();
        }

        epo.setOriginalBean(objectForEditing);

        //put this in the parent class?
        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=Integer.valueOf(objectForEditing.getId());
        epo.getSimpleMask().add(simpleMaskPair);

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = int.class;
            epo.setGetMethod(dDao.getClass().getDeclaredMethod("getDatatypeById",args));
        } catch (NoSuchMethodException e) {
            log.error("EntityRetryController could not find the entityById method in the facade");
        }

        epo.setPostInsertPageForwarder(new DatatypeInsertPageForwarder());
        epo.setPostDeletePageForwarder(new DatatypeDeletePageForwarder());

        FormObject foo = new FormObject();
        epo.setFormObject(foo);
        FormUtils.populateFormFromBean(objectForEditing,action,foo);
        //for now, this is also making the value hash - need to separate this out


        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("formJsp","/templates/edit/specific/datatype_retry.jsp");
        request.setAttribute("title","Datatype Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Datatype");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("VclassRetryContro" +
                    "ller could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class DatatypeInsertPageForwarder implements PageForwarder {
        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newDtpUrl = "fetch?queryspec=private_datatypev&postGenLimit=-1&linkwhere=datatypes.id=";
            Datatype dtp = (Datatype) epo.getNewBean();
            newDtpUrl += dtp.getId();
            try {
                response.sendRedirect(newDtpUrl);
            } catch (IOException ioe) {
                log.error("DatatypeInsertPageForwarder could not send redirect.");
            }
        }
    }

    class DatatypeDeletePageForwarder implements PageForwarder {
        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newDtpUrl = "fetch?queryspec=private_datatypes";
            Datatype dtp = (Datatype) epo.getNewBean();
            try {
                response.sendRedirect(newDtpUrl);
            } catch (IOException ioe) {
                log.error("DatatypeInsertPageForwarder could not send redirect.");
            }
        }
    }

}
