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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;
import edu.cornell.mannlib.vitro.webapp.beans.Namespace;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.NamespaceDao;

public class NamespaceRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(NamespaceRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new UseMiscellaneousAdminPages()))) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        /*for testing*/
        Namespace testMask = new Namespace();
        epo.setBeanClass(Namespace.class);
        epo.setBeanMask(testMask);

        String action = "insert";
        if (request.getFullWebappDaoFactory() == null)
            log.error("null CoreDaoFactory");
        NamespaceDao namespaceDao = request.getFullWebappDaoFactory().getNamespaceDao();
        //VitroFacade facade = getFacade();
        //epo.setFacade(facade);

        Namespace namespaceForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("id") != null) {
                int id = Integer.parseInt(request.getParameter("id"));
                if (id > 0) {
                    try {
                        namespaceForEditing = namespaceDao.getNamespaceById(id);
                        action = "update";
                    } catch (NullPointerException e) {
                        log.error("Need to implement 'record not found' error message.");
                    }
                }
            } else {
                namespaceForEditing = new Namespace();
            }
            epo.setOriginalBean(namespaceForEditing);
        } else {
            namespaceForEditing = (Namespace) epo.getNewBean();
            action = "update";
            log.error("using newBean");
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=Integer.valueOf(namespaceForEditing.getId());
        epo.getSimpleMask().add(simpleMaskPair);

        //set up any listeners

        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new NamespaceInsertPageForwarder());
        //make a postdelete pageforwarder that will send us to the list of classes

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = int.class;
            epo.setGetMethod(namespaceDao.getClass().getDeclaredMethod("getNamespaceById",args));
        } catch (NoSuchMethodException e) {
            log.error("NamespaceRetryController could not find the namespaceById method in the facade");
        }


        FormObject foo = new FormObject();

        foo.setErrorMap(epo.getErrMsgMap());

        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(namespaceForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/namespace_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Namespace Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Namespace");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("NamespaceRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class NamespaceInsertPageForwarder implements PageForwarder {

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newNamespaceUrl = "fetch?queryspec=private_namespacev&postGenLimit=-1&linkwhere=namespaces.id=";
            Namespace ns = (Namespace) epo.getNewBean();
            newNamespaceUrl += ns.getId();
            try {
                response.sendRedirect(newNamespaceUrl);
            } catch (IOException ioe) {
                log.error("NamespaceInsertPageForwarder could not send redirect.");
            }
        }
    }

}
