/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;

public class NamespacePrefixRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(NamespacePrefixRetryController.class.getName());
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION)) {
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
				
	       request.setAttribute("editAction","namespacePrefixOp");
	        request.setAttribute("scripts","/templates/edit/formBasic.js");
        	request.setAttribute("formJsp","/templates/edit/specific/namespacePrefix_retry.jsp");
        	request.setAttribute("title","Edit Namespace Prefix Mapping");
	        setRequestAttributes(request,epo);

	        try {
				JSPPageHandler.renderBasicPage(request, response, "/templates/edit/formBasic.jsp");
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
