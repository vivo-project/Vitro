/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController.Utilities;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;

import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
/**
 * This servlet will process EditConfigurations with query parameters
 * to perform an edit.
 * 
 * TODO: rename this class ProcessN3Edit  
 */
public class PostEditCleanupController extends FreemarkerHttpServlet{
	
    private Log log = LogFactory.getLog(PostEditCleanupController.class);
    	
    
	//bdc34: this is likely to become a servlet instead of a jsp.
	// You can get a reference to the servlet from the context.
	// this will need to be converted from a jsp to something else
	
	@Override 
	protected ResponseValues processRequest(VitroRequest vreq) {	
		EditConfigurationVTwo configuration = EditConfigurationVTwo.getConfigFromSession(vreq.getSession(), vreq);		
        if(configuration == null)
            throw new Error("No edit configuration found.");        

        //The submission for getting the entity to return to is not retrieved from the session but needs
        //to be created - as it is in processRdfForm3.jsp
        MultiValueEditSubmission submission = new MultiValueEditSubmission(vreq.getParameterMap(), configuration);  
        String entityToReturnTo = ProcessRdfForm.processEntityToReturnTo(configuration, submission, vreq);
		return doPostEdit(vreq, entityToReturnTo);		
	}


    public static RedirectResponseValues doPostEdit(VitroRequest vreq, String resourceToRedirectTo ) {
        String urlPattern = null;
        String predicateAnchor = "";
        HttpSession session = vreq.getSession(false);
        if( session != null ) {
            EditConfigurationVTwo editConfig = EditConfigurationVTwo.getConfigFromSession(session,vreq);
            //In order to support back button resubmissions, don't remove the editConfig from session.
            //EditConfiguration.clearEditConfigurationInSession(session, editConfig);            
            //Here, edit submission is retrieved so it can be cleared out in case it exists
            MultiValueEditSubmission editSub = EditSubmissionUtils.getEditSubmissionFromSession(session,editConfig);        
            EditSubmissionUtils.clearEditSubmissionInSession(session, editSub);
            
            //Get prop local name if it exists
            String predicateLocalName = Utilities.getPredicateLocalName(editConfig);
          
            //Get url pattern
            urlPattern = Utilities.getPostEditUrlPattern(vreq, editConfig);
            predicateAnchor = Utilities.getPredicateAnchorPostEdit(urlPattern, predicateLocalName);
            if(predicateAnchor != null && !predicateAnchor.isEmpty()) {
                vreq.setAttribute("predicateAnchor", predicateAnchor);

            }
            
        }
        
        //Redirect appropriately
        if( resourceToRedirectTo != null ){
            ParamMap paramMap = new ParamMap();
            paramMap.put("uri", resourceToRedirectTo);
            paramMap.put("extra","true"); //for ie6            
            return new RedirectResponseValues( UrlBuilder.getPath(urlPattern,paramMap) + predicateAnchor );
        } else if ( !urlPattern.endsWith("individual") && !urlPattern.endsWith("entity") ){
            return new RedirectResponseValues( urlPattern );
        }
        return new RedirectResponseValues( Route.LOGIN );
    }    
	
}
