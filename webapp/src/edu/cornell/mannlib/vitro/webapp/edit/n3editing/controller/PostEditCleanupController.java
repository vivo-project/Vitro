/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoFrontEndEditing;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.DirectRedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.N3EditUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController.Utilities;


public class PostEditCleanupController extends FreemarkerHttpServlet{
	
    private static Log log = LogFactory.getLog(PostEditCleanupController.class);    	    
	
    @Override
	protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new DoFrontEndEditing());
	}

    @Override 
	protected ResponseValues processRequest(VitroRequest vreq) {	
		doPostEditCleanup( vreq );
		return doPostEditRedirect( vreq, null);	
	}

	/**
	 * Returns a redirect after an edit.
	 * @param vreq - should have an edit configuration in attributes or session
	 * @param entityToReturnTo - may be null
	 * @return
	 */
    protected static ResponseValues doPostEditRedirect( VitroRequest vreq , String entityToReturnTo){
        EditConfigurationVTwo editConfig = EditConfigurationVTwo.getConfigFromSession(vreq.getSession(), vreq);      
        if(editConfig == null){
            log.error("No edit configuration found in doPostEditRedirect()");        
            return new RedirectResponseValues( UrlBuilder.getHomeUrl() );
        }
        
        // If there is a urlToReturnTo that takes precedence 
        if( editConfig.getUrlToReturnTo() != null && ! editConfig.getUrlToReturnTo().trim().isEmpty()){
            //this does not get value substitution or the predicate anchor
            return new DirectRedirectResponseValues( editConfig.getUrlToReturnTo() );
        }
        
        //The submission for getting the entity to return to is not retrieved from the session but needs
        //to be created - as it is in processRdfForm3.jsp
        //TODO: this will not work if there entityToReturnTo has a new resource URI
        MultiValueEditSubmission submission = new MultiValueEditSubmission(vreq.getParameterMap(), editConfig);
        if( entityToReturnTo == null )
            entityToReturnTo = N3EditUtils.processEntityToReturnTo(editConfig, submission, vreq);                        
      
        //Get url pattern
        String urlPattern = Utilities.getPostEditUrlPattern(vreq, editConfig);                
        
        //Redirect appropriately        
        if( entityToReturnTo != null ){

            //Try to redirect to the entityToReturnTo
            ParamMap paramMap = new ParamMap();
            paramMap.put("uri", entityToReturnTo);
            paramMap.put("extra","true"); //for ie6       
            //If url already contains an ? then need to add extra params
            String path = UrlBuilder.addParams(urlPattern, paramMap);
            path += getPredicateAnchor( vreq, editConfig );
            return new RedirectResponseValues( path );
            
        } else if ( !urlPattern.endsWith("individual") && !urlPattern.endsWith("entity") ){
            
            //Try to redirect to just the EditConfig.UrlPattern
            //since it doesn't seem to be for the profile page
            return new RedirectResponseValues( urlPattern );
            
        }else if( editConfig.getSubjectUri() != null ){ 
            
            //Try to redirect to the EditConf.subjectUri profile page
            //since things seem a little odd.
            ParamMap paramMap = new ParamMap();
            paramMap.put("uri", editConfig.getSubjectUri() );
            paramMap.put("extra","true"); //for ie6           
            String path = UrlBuilder.getPath( UrlBuilder.Route.INDIVIDUAL, paramMap);            
            path += getPredicateAnchor( vreq, editConfig );
            return new RedirectResponseValues( path );
            
        }else{
            //Not sure where to go
            return new RedirectResponseValues( Route.LOGIN );
        }
    }
    
    public static void doPostEditCleanup( VitroRequest vreq ) {
        EditConfigurationVTwo configuration = EditConfigurationVTwo.getConfigFromSession(vreq.getSession(), vreq);      
        if(configuration == null)
            return;        
        
        HttpSession session = vreq.getSession(false);
        if( session != null ) {
            EditConfigurationVTwo editConfig = EditConfigurationVTwo.getConfigFromSession(session,vreq);
            //In order to support back button resubmissions, don't remove the editConfig from session.
            //EditConfiguration.clearEditConfigurationInSession(session, editConfig);            
            //Here, edit submission is retrieved so it can be cleared out in case it exists
            MultiValueEditSubmission editSub = EditSubmissionUtils.getEditSubmissionFromSession(session,editConfig);        
            EditSubmissionUtils.clearEditSubmissionInSession(session, editSub);                               
        }               
    }    
	
    
    /**
     * Adds a attribute to the request to indicate which predicate was edited.
     * This attribute is used by some controllers to send the browser to the
     * place on the page relevant to the predicate.
     * 
     * Never returns null, it will return an empty string if there is nothing. 
     */
    public static String getPredicateAnchor( HttpServletRequest req, EditConfigurationVTwo config){
        if( req == null || config == null )
            return "";  
        
        //Get prop local name if it exists
        String predicateLocalName = Utilities.getPredicateLocalName(config);
        
        if( predicateLocalName != null && predicateLocalName.length() > 0){
            String predicateAnchor = "#" + predicateLocalName;
            req.setAttribute("predicateAnchor", predicateAnchor);
            return predicateAnchor;
        }else{
            return "";
        }
    }    
}
