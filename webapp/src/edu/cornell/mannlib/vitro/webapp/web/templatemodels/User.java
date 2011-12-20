/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.Collection;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProfile;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.RevisionInfoController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.SiteAdminController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexController;

public class User extends BaseTemplateModel {
    private final VitroRequest vreq;
    private final UserAccount currentUser;
    private final String profileUrl;
    
    public User(VitroRequest vreq) {
        this.vreq = vreq;
        this.currentUser = LoginStatusBean.getCurrentUser(vreq);
        this.profileUrl = figureAssociatedProfileUrl();
    }
    
	private String figureAssociatedProfileUrl() {
        IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(vreq);
		Collection<String> uris = HasProfile.getProfileUris(ids);
        if (uris.isEmpty()) {
        	return "";
        }
        
        String uri = uris.iterator().next();
        String url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
        if (url == null) {
        	return "";
        }
        
        return url;
	}
	
	/* Template properties */

	public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public String getEmailAddress() {
		return (currentUser == null) ? "" : currentUser.getEmailAddress();
    }
    
    public String getLoginName() {
    	if (currentUser == null) {
    		return "";
    	} 

    	if (currentUser.getFirstName().isEmpty()) {
    		return currentUser.getEmailAddress();
    	}
    	
    	return currentUser.getFirstName();
    }
    
    public String getFirstName() {
        return currentUser == null ? "" : currentUser.getFirstName();
    }
    
    public String getLastName() {
        return currentUser == null ? "" : currentUser.getLastName();
    }
    
    public boolean getHasSiteAdminAccess() {
    	return PolicyHelper.isAuthorizedForActions(vreq, SiteAdminController.REQUIRED_ACTIONS);
    }
    
    public boolean getHasRevisionInfoAccess() {
    	return PolicyHelper.isAuthorizedForActions(vreq, RevisionInfoController.REQUIRED_ACTIONS);
    }
    
    public boolean isAuthorizedToRebuildSearchIndex() {
        return PolicyHelper.isAuthorizedForActions(vreq, IndexController.REQUIRED_ACTIONS);
    }
    
    public boolean getHasProfile() {
    	return !profileUrl.isEmpty();
    }
    
    public String getProfileUrl() {
    	return profileUrl;
    }
}
