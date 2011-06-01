/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.RevisionInfoController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.SiteAdminController;

public class User extends BaseTemplateModel {
    private final VitroRequest vreq;

    // TODO JB Modify this to use UserAccount instead of User.
    private final edu.cornell.mannlib.vitro.webapp.beans.User currentUser;
    
    public User(VitroRequest vreq) {
        this.vreq = vreq;
        this.currentUser = LoginStatusBean.getCurrentUser(vreq);
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public String getEmailAddress() {
		return (currentUser == null) ? "" : currentUser.getUsername();
    }
    
    public String getLoginName() {
		return (currentUser == null) ? ""
				: (currentUser.getFirstName() + " " + currentUser.getLastName());
    }
    
    public boolean getHasSiteAdminAccess() {
    	return PolicyHelper.isAuthorizedForActions(vreq, SiteAdminController.REQUIRED_ACTIONS);
    }
    
    public boolean getHasRevisionInfoAccess() {
    	return PolicyHelper.isAuthorizedForActions(vreq, RevisionInfoController.REQUIRED_ACTIONS);
    }
    
}
