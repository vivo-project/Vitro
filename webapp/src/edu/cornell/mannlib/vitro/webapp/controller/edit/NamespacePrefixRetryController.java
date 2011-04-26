/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;

public class NamespacePrefixRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(NamespacePrefixRetryController.class.getName());
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new UseMiscellaneousAdminPages()))) {
        	return;
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        
        if (request.getParameter("prefix") != null) {
        	epo.setAction("update");
        	request.setAttribute("_action","update");
        } else {
        	epo.setAction("insert");
        	request.setAttribute("_action","insert");
        }
				
	       RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
	       request.setAttribute("editAction","namespacePrefixOp");
	        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
	        request.setAttribute("scripts","/templates/edit/formBasic.js");
        	request.setAttribute("formJsp","/templates/edit/specific/namespacePrefix_retry.jsp");
        	request.setAttribute("title","Edit Namespace Prefix Mapping");
	        setRequestAttributes(request,epo);

	        try {
	            rd.forward(request, response);
	        } catch (Exception e) {
	            log.error(this.getClass().getName()+" could not forward to view.");
	            log.error(e.getMessage());
	            log.error(e.getStackTrace());
	        }

	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		// shouldn't be posting to this controller
	}

}
