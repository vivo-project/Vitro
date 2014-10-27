/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;

/**
 * TODO
 */
public class FauxPropertyRetryController extends BaseEditController {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse response) {
		if (!isAuthorizedToDisplayPage(req, response,
				SimplePermission.EDIT_ONTOLOGY.ACTION)) {
			return;
		}

		// TODO
	}
	
    @Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }


}
