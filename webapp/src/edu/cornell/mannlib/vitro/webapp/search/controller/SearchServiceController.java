/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BasicAuthenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.MultipartHttpServletRequest;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * Accepts requests to update a set of URIs in the search index. 
 */
@SuppressWarnings("serial")
public class SearchServiceController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory.getLog(SearchServiceController.class);

    /**
     * Attempt to check if there is a email and password in the request parameters.
     * If there is not, fall back on the normal usage pattern of requestedAction().
     * If there is, then try to authenticate and authorize the 
     * userAccount associated with the email.
     */
	@Override
	protected Actions requiredActions(VitroRequest vreq) {
        try{
			// Works by side effect: parse the multi-part request and stash FileItems in request			
			FileUploadServletRequest.parseRequest(vreq, 0);

            //first figure out if the password and email login to an account with a password
            String pw = vreq.getParameter("password");
            String email = vreq.getParameter("email");

            log.debug(String.format("email: '%s' password: '%s' ",email,pw));

            if( pw == null || email == null || pw.isEmpty() || email.isEmpty()){
                return SimplePermission.MANAGE_SEARCH_INDEX.ACTIONS;
            }

            Authenticator basicAuth = new BasicAuthenticator(vreq);            
            UserAccount user = basicAuth.getAccountForInternalAuth( email );
            log.debug("userAccount is " + user==null?"null":user.getUri() );
                
            if( ! basicAuth.isCurrentPassword( user, pw ) ){
                log.debug(String.format("UNAUTHORIZED, password not accepted for %s, account URI: %s",
                                        user.getEmailAddress(), user.getUri()));
                return Actions.UNAUTHORIZED;
            }else{
                log.debug(String.format("password accepted for %s, account URI: %s",
                                        user.getEmailAddress(), user.getUri() ));
            }
                
            //then figure out if that account can manage the search index.
            IdentifierBundle ids = 
                ActiveIdentifierBundleFactories.getUserIdentifierBundle(vreq,user);
            PolicyIface policy = ServletPolicyList.getPolicies(vreq);
            boolean canManageSearchIndex = 
                PolicyHelper.isAuthorizedForActions( ids, policy, 
                                                     SimplePermission.MANAGE_SEARCH_INDEX.ACTIONS );
            if( canManageSearchIndex ){
                return Actions.AUTHORIZED;
            }else{
                log.debug(String.format("userAccount is unauthorized to" +
                                        " manage the search index.",user.getUri()));
                return Actions.UNAUTHORIZED;
            }

        }catch(Exception ex){
            log.error("Error while attempting to log in " + 
                      "to SearchServiceController: " + ex.getMessage());
            return Actions.UNAUTHORIZED;
        }
	}	

    
	/**
	 * Handle the different actions. If not specified, the default action is to
	 * show the help page.
	 */  
	@Override
	protected ResponseValues processRequest(VitroRequest req) {
		try {
			
            //figure out what action to perform
            String pathInfo = req.getPathInfo();
                        
            if( pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim()) ){
                return doHelpForm(req);
            }
            
            pathInfo = pathInfo.substring(1);  //get rid of leading slash
            
			if (VERBS.UPDATE_URIS_IN_SEARCH.verb.equals( pathInfo )) {
				return doUpdateUrisInSearch(req);
			} else {			
				return doHelpForm(req);
			}
		} catch (Exception e) {
			return new ExceptionResponseValues(e);
		}
	}


    /**
     * Process requests to the web service to update a list of URIs in the search index. */
    public ResponseValues doUpdateUrisInSearch(HttpServletRequest req )
        throws IOException, ServletException {
    
       IndexBuilder builder = IndexBuilder.getBuilder(getServletContext());
       if( builder == null )
           throw new ServletException( "Could not get search index builder from context. Check smoke test");

        new UpdateUrisInIndex().doUpdateUris( req, builder);

        TemplateResponseValues trv = new TemplateResponseValues( "" );
        return trv;
    }

    
    public ResponseValues doHelpForm(HttpServletRequest req){
        return  new TemplateResponseValues( "searchService-help.ftl");
    }
    
    public enum VERBS{
        UPDATE_URIS_IN_SEARCH("updateUrisInSearch");
        
        public final String verb;
        VERBS(String verb){
            this.verb = verb;
        }
    }    
    
}
