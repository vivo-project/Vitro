/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.RevisionInfoController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.SiteAdminController;

public class User extends BaseTemplateModel {
   
    private static final Log log = LogFactory.getLog(User.class);
    
    private enum Access {
        FILTER_SECURITY(LoginStatusBean.EDITOR);
        
        private final int requiredLoginLevel;
        
        Access(int requiredLoginLevel) {
            this.requiredLoginLevel = requiredLoginLevel;
        }
        
        int requiredLoginLevel() {
            return this.requiredLoginLevel;
        }
    }
    
    private LoginStatusBean loginBean = null;
    private VitroRequest vreq = null;
    
    public User(VitroRequest vreq) {
        this.vreq = vreq;
        loginBean = LoginStatusBean.getBean(vreq);
    }
    
    public boolean isLoggedIn() {
        return loginBean.isLoggedIn();
    }
    
    public String getLoginName() {
        return loginBean.getUsername();
    }
    
    public boolean getHasSiteAdminAccess() {
    	return PolicyHelper.isAuthorizedForActions(vreq, SiteAdminController.REQUIRED_ACTIONS);
    }
    
    public boolean getHasRevisionInfoAccess() {
    	return PolicyHelper.isAuthorizedForActions(vreq, RevisionInfoController.REQUIRED_ACTIONS);
    }
    
}
