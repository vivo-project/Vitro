/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Check to see if the User is logged in, find Individuals that the User mayEditAs,
 * and and those Individuals as identifiers.
 *  
 * @author bdc34
 *
 */
public class UserToIndIdentifierFactory implements IdentifierBundleFactory {

    public IdentifierBundle getIdentifierBundle(
            ServletRequest request,
            HttpSession session, 
            ServletContext context) {       
    	// is the request logged in as a User?
    	LoginStatusBean loginBean = LoginStatusBean.getBean(session);
    	if (loginBean.isLoggedIn()) {
            String userURI = loginBean.getUserURI();

            WebappDaoFactory wdf = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
            
            // get Individuals that the User mayEditAs
            List<String> mayEditAsUris = 
                wdf.getUserDao().getIndividualsUserMayEditAs(userURI);

            // make self editing Identifiers for those Individuals
            IdentifierBundle idb = new ArrayIdentifierBundle();
            idb.add( new UserIdentifier(userURI,mayEditAsUris) );
            
            //Also make a self-editing identifier.
            //There is not need for SelfEditingIdentifierFactory because SelfEditing
            //identifiers are created here.              
            for( String personUri : mayEditAsUris){
                if( personUri != null ){
                    Individual person = wdf.getIndividualDao().getIndividualByURI(personUri);
                    if( person != null ){
                        idb.add( new SelfEditingIdentifierFactory.SelfEditing(person,null) );
                    }
                }
            }
            return idb;            
        }

        return null;
    }
    
    public static List<String> getIndividualsForUser(IdentifierBundle ids) {
        if( ids == null )
            return Collections.emptyList();
        
        //find the user id
        List<String> uris = new ArrayList<String>();
        for( Identifier id : ids ){
            if( id instanceof UserIdentifier){
                uris.addAll( ((UserIdentifier)id).getMayEditAsURIs() );
            }
        }
        return uris;
    }
            
    public class UserIdentifier implements Identifier {
        private final String userURI;
        private final List<String> mayEditAsURIs;
        public UserIdentifier(String userURI, List<String> mayEditAsURIs) {
            super();
            this.userURI = userURI;
            this.mayEditAsURIs = Collections.unmodifiableList(mayEditAsURIs);
        }
        public String getUserURI() {
            return userURI;
        }
        public List<String> getMayEditAsURIs() {
            return mayEditAsURIs;
        }
		@Override
		public String toString() {
			return "UserIdentifier: " + userURI;
		}        
        
    }
}
