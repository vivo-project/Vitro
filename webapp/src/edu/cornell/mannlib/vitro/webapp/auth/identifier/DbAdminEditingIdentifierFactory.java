/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy.AuthRole;

public class DbAdminEditingIdentifierFactory implements IdentifierBundleFactory{
    
    public IdentifierBundle getIdentifierBundle(ServletRequest request,
            HttpSession session, ServletContext context) {
        IdentifierBundle ib = new ArrayIdentifierBundle();
        ib.add( RoleBasedPolicy.AuthRole.ANYBODY);
        
		LoginStatusBean loginBean = LoginStatusBean.getBean(session);
		if (loginBean.isLoggedInAtLeast(LoginStatusBean.DBA)) {
			String loginRole = String.valueOf(loginBean.getSecurityLevel());
			ib.add(new DbAdminEditingId(loginRole, loginBean.getUserURI()));
			ib.add(AuthRole.DBA);
		}

        return ib;        
    }

    public static class DbAdminEditingId extends RoleIdentifier{
        final String role;
        final String uri;
        
        public DbAdminEditingId( String role, String uri) {
            this.role = role;
            this.uri = uri;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getUri(){ return uri; }
        
        public String toString(){
            return "DbAdminEditingId: role of " + getRole();
        }
    }
}
