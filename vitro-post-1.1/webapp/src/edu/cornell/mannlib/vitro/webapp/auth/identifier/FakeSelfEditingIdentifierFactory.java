/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.NetId;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/** 
 * Attempts to simulate the action of SelfEditingIdentifierFactory.java using the
 * request attribute FAKE_SELF_EDIT_NETID.
 */
public class FakeSelfEditingIdentifierFactory implements IdentifierBundleFactory{    
    
    public static final String FAKE_SELF_EDIT_NETID = "fakeSelfEditingNetid";     
    
    public IdentifierBundle getIdentifierBundle(ServletRequest request,
            HttpSession session, ServletContext context) {
        WebappDaoFactory wdf = ((WebappDaoFactory)context.getAttribute("webappDaoFactory"));
        
        IdentifierBundle ib = new ArrayIdentifierBundle();
        ib.add( RoleBasedPolicy.AuthRole.ANYBODY);
                
        String netid = null;
        if( session != null )
            netid = (String)session.getAttribute(FAKE_SELF_EDIT_NETID );
        
        if( netid != null ){
            NetId netIdToken = new NetId(netid);
            ib.add(netIdToken);
            
            String uri = wdf.getIndividualDao().getIndividualURIFromNetId( netid );
            if( uri != null ){        
                Individual ind = wdf.getIndividualDao().getIndividualByURI(uri);
                if( ind != null ){        
                    String causeOfBlacklist = SelfEditingIdentifierFactory.checkForBlacklisted(ind, context);
                    if( causeOfBlacklist == SelfEditingIdentifierFactory.NOT_BLACKLISTED )
                        ib.add( new SelfEditingIdentifierFactory.SelfEditing( ind, SelfEditingIdentifierFactory.NOT_BLACKLISTED, true ) );
                    else
                        ib.add( new SelfEditingIdentifierFactory.SelfEditing( ind, causeOfBlacklist, true) );
                }
            }
        }
        return ib;        
    }

    public static void putFakeIdInSession(String netid, HttpSession session){        
        session.setAttribute(FAKE_SELF_EDIT_NETID , netid);
    }
    
    public static void clearFakeIdInSession( HttpSession session){        
        session.removeAttribute(FAKE_SELF_EDIT_NETID);
    }
    
	public static String getFakeIdFromSession(HttpSession session) {
		Object netid = session.getAttribute(FAKE_SELF_EDIT_NETID);
		if (netid instanceof String) {
			return (String) netid;
		} else {
			return null;
		}
	}
}
