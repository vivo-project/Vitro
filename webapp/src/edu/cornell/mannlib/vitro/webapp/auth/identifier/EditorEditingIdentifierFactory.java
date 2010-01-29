package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy.AuthRole;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class EditorEditingIdentifierFactory implements IdentifierBundleFactory{
    
    public IdentifierBundle getIdentifierBundle(ServletRequest request,
            HttpSession session, ServletContext context) {
        IdentifierBundle ib = new ArrayIdentifierBundle();
        ib.add( RoleBasedPolicy.AuthRole.ANYBODY);
        if( session != null ){
            LoginFormBean f = (LoginFormBean) session.getAttribute( "loginHandler" );
            try{
                if( f != null && Integer.parseInt( f.getLoginRole() ) >=  LoginFormBean.EDITOR){
                    ib.add(new EditorEditingId(f.getLoginRole(), f.getUserURI()));
                    ib.add(AuthRole.EDITOR);
                }
            }catch(NumberFormatException th){ }
        }
        return ib;        
    }

    public static class EditorEditingId implements Identifier {
        final String role;
        final String uri;
        
        public EditorEditingId( String role, String uri) {
            this.role = role;
            this.uri = uri;
        }
        public String getUri(){ return uri; }
        
        public String getRole() {
            return role;
        }
        
        public String toString(){
            return "Editor role of " + getRole();
        }
    }
}
