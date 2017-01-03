/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class RDFUploadFormController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RDFUploadFormController.class.getName());

    @Override
	public void doPost (HttpServletRequest request, HttpServletResponse response) {

		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
    		return;
    	}

        VitroRequest vreq = new VitroRequest(request);

        request.setAttribute("title","Ingest RDF Data");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/specific/upload_rdf.jsp");
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    @Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }

}
