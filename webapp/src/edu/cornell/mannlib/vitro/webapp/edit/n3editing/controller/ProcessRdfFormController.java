/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoFrontEndEditing;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.NewURIMakerVitro;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.N3EditUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;

/**
 * This servlet will convert a request to an EditSubmission, 
 * find the EditConfiguration associated with the request, 
 * use ProcessRdfForm to process these to a set of RDF additions and retractions,
 * the apply these to the models. 
 */
public class ProcessRdfFormController extends FreemarkerHttpServlet{
	
    private Log log = LogFactory.getLog(ProcessRdfFormController.class);
    	
    @Override
	protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new DoFrontEndEditing());
	}

	@Override 
	protected ResponseValues processRequest(VitroRequest vreq) {			
		//get the EditConfiguration 
		EditConfigurationVTwo configuration = EditConfigurationUtils.getEditConfiguration(vreq);
        if(configuration == null)
            throw new Error("No edit configuration found.");        

        //get the EditSubmission
        MultiValueEditSubmission submission = new MultiValueEditSubmission(vreq.getParameterMap(), configuration);        	
        EditSubmissionUtils.putEditSubmissionInSession(vreq.getSession(), submission);
       
        //if errors, return error response
		ResponseValues errorResponse = doValidationErrors(vreq, configuration, submission);
		if( errorResponse != null )
		    return errorResponse;

        // get the models to work with in case the write model and query model are not the defaults 
		OntModel queryModel = configuration.getQueryModelSelector().getModel(vreq, getServletContext());		
	    OntModel writeModel = configuration.getWriteModelSelector().getModel(vreq,getServletContext());  
	    
	    //If data property check for back button confusion
	    boolean isBackButton = checkForBackButtonConfusion(configuration, vreq, queryModel);
	    if(isBackButton) {
	    	//Process back button issues and do a return here
	    	return doProcessBackButton(configuration, submission, vreq);
	    }
	    
	    //Otherwise, process as usual
	    
	    AdditionsAndRetractions changes;
        try {

            ProcessRdfForm prf = 
                new ProcessRdfForm(configuration, new NewURIMakerVitro(vreq.getWebappDaoFactory()));        
            changes = prf.process(configuration, submission);  
            
        } catch (Exception e) {
            throw new Error(e);
        }	    
		
		if( configuration.isUseDependentResourceDelete() )
		    changes = ProcessRdfForm.addDependentDeletes(changes, queryModel);		
		
		N3EditUtils.preprocessModels(changes, configuration, vreq);		
		
		ProcessRdfForm.applyChangesToWriteModel(changes, queryModel, writeModel, EditN3Utils.getEditorUri(vreq) );
		
		//Here we are trying to get the entity to return to URL,  
		//More involved processing for data property apparently
		String entityToReturnTo = N3EditUtils.processEntityToReturnTo(configuration, submission, vreq);
		
        //For data property processing, need to update edit configuration for back button 
		N3EditUtils.updateEditConfigurationForBackButton(configuration, submission, vreq, writeModel);
        PostEditCleanupController.doPostEditCleanup(vreq);
        return PostEditCleanupController.doPostEditRedirect(vreq, entityToReturnTo);
	}

	//In case of back button confusion
	//Currently returning an error message: 
	//Later TODO: Per Brian Caruso's instructions, replicate
	//the logic in the original datapropertyBackButtonProblems.jsp
	private ResponseValues doProcessBackButton(EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, VitroRequest vreq) {
		  
		//The bulk of the processing should probably/already sits in ProcessRdfForm so that should remain there
		//The issue is what then to do with the actual redirect? What do we redirect to?
		HashMap<String,Object> map = new HashMap<String,Object>();
   	 	map.put("errorMessage", "Back button confusion has occurred");
		ResponseValues values = new TemplateResponseValues("error-message.ftl", map);        
		return values;
	}

	//Check for "back button" confusion specifically for data property editing although need to check if this applies to object property editing?
	//TODO: Check if only applicable to data property editing
	private boolean checkForBackButtonConfusion(EditConfigurationVTwo editConfig, VitroRequest vreq, Model model) {
		//back button confusion limited to data property
		if(EditConfigurationUtils.isObjectProperty(editConfig.getPredicateUri(), vreq)) {
			return false;
		}
		
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		 if ( ! editConfig.isDataPropertyUpdate())
	            return false;
	        
        Integer dpropHash = editConfig.getDatapropKey();
        DataPropertyStatement dps = 
            RdfLiteralHash.getPropertyStmtByHash(editConfig.getSubjectUri(), 
                    editConfig.getPredicateUri(), dpropHash, model);
        if (dps != null)
            return false;
        
        DataProperty dp = wdf.getDataPropertyDao().getDataPropertyByURI(
                editConfig.getPredicateUri());
        if (dp != null) {
            if (dp.getDisplayLimit() == 1 /* || dp.isFunctional() */)
                return false;
            else
                return true;
        }
        return false;

	}
	
	private ResponseValues doValidationErrors(VitroRequest vreq,
			EditConfigurationVTwo editConfiguration, MultiValueEditSubmission submission) {
		
		Map<String, String> errors = submission.getValidationErrors();
		
		if(errors != null && !errors.isEmpty()){
			String form = editConfiguration.getFormUrl();
			vreq.setAttribute("formUrl", form);
			vreq.setAttribute("view", vreq.getParameter("view"));	
			//Need to ensure that edit key is set so that the correct
			//edit configuration and edit submission are retrieved
			//This can also be set as a parameter instead
			String formUrl = editConfiguration.getFormUrl();
			formUrl += "&editKey=" + editConfiguration.getEditKey();
	        return new RedirectResponseValues(formUrl);
		}
		return null; //no errors		
	}
	
	//Move to EditN3Utils but keep make new uris here
	public static class Utilities {
		
		private static Log log = LogFactory.getLog(ProcessRdfFormController.class);
	    
		public static String assertionsType = "assertions";
		public static String retractionsType = "retractions";
		
		public static boolean isDataProperty(EditConfigurationVTwo configuration, VitroRequest vreq) {
			return EditConfigurationUtils.isDataProperty(configuration.getPredicateUri(), vreq);
		}
		
		public static boolean isObjectProperty(EditConfigurationVTwo configuration, VitroRequest vreq) {
			
			return EditConfigurationUtils.isObjectProperty(configuration.getPredicateUri(), vreq);
		}
		
	    public static List<String> makeListCopy(List<String> list) {
	    	List<String> copyOfN3 = new ArrayList<String>();
            for( String str : list){
                copyOfN3.add(str);
            }
            return copyOfN3;
	    }
	     
	     //TODO: Check if this would be correct with multiple values and uris being passed back
	     //First, need to order by uris in original and new values probably and 
	     //for literals, order by? Alphabetical or numeric value? Hard to say
	     public static boolean hasFieldChanged(String fieldName,
	             EditConfigurationVTwo editConfig, MultiValueEditSubmission submission) {
	         List<String> orgValue = editConfig.getUrisInScope().get(fieldName);
	         List<String> newValue = submission.getUrisFromForm().get(fieldName);
	         //Sort both just in case
	         if(orgValue != null) {
	        	 Collections.sort(orgValue);
	         }
	         if(newValue != null) {
	        	 Collections.sort(newValue);
	         }
	         if (orgValue != null && newValue != null) {
	             if (orgValue.equals(newValue))
	                 return false;
	             else
	                 return true;
	         }

	         List<Literal> orgLit = editConfig.getLiteralsInScope().get(fieldName);
	         List<Literal> newLit = submission.getLiteralsFromForm().get(fieldName);
	         //TODO: Sort ? Need custom comparator
	         //Collections.sort(orgLit);
	         //Collections.sort(newLit);
	         //for(Literal l: orgLit)
	         //boolean fieldChanged = !EditLiteral.equalLiterals(orgLit, newLit);
	         //TODO:Check if below acts as expected
	         boolean fieldChanged = !orgLit.equals(newLit);
	         if(!fieldChanged) {
	        	 int orgLen = orgLit.size();
	        	 int newLen = newLit.size();
	        	 if(orgLen != newLen) {
	        		 fieldChanged = true;
	        	 } else {
	        		 int i;
	        		 for(i = 0; i < orgLen; i++) {
	        			 if(!EditLiteral.equalLiterals(orgLit.get(i), newLit.get(i))) {
	        				 fieldChanged = true;
	        				 break;
	        			 }
	        		 }
	        	 }
	         }
	         log.debug("field " + fieldName + " "
	                 + (fieldChanged ? "did Change" : "did NOT change"));
	         return fieldChanged;
	     }
	     		
		//Get predicate local anchor
		public static String getPredicateLocalName(EditConfigurationVTwo editConfig) {
			String predicateLocalName = null;
	        if( editConfig != null ){
                String predicateUri = editConfig.getPredicateUri();            
                if( predicateUri != null ){
                    try{
                        Property prop = ResourceFactory.createProperty(predicateUri);
                        predicateLocalName = prop.getLocalName();
                    
                    }catch (com.hp.hpl.jena.shared.InvalidPropertyURIException e){                  
                        log.debug("could not convert predicateUri into a valid URI",e);
                    }                               
                }
	        }
	        return predicateLocalName;
		}
		//Get URL pattern for return
		public static String getPostEditUrlPattern(VitroRequest vreq, EditConfigurationVTwo editConfig) {
			String cancel = vreq.getParameter("cancel");
	        String urlPattern = null;

            String urlPatternToReturnTo = null;
            String urlPatternToCancelTo = null;
            if (editConfig != null) {
                urlPatternToReturnTo = editConfig.getUrlPatternToReturnTo();
                urlPatternToCancelTo = vreq.getParameter("url");
            }
            // If a different cancel return path has been designated, use it. Otherwise, use the regular return path.
            if (cancel != null && cancel.equals("true") && !StringUtils.isEmpty(urlPatternToCancelTo)) {
                urlPattern = urlPatternToCancelTo;
            } else if (!StringUtils.isEmpty(urlPatternToReturnTo)) {
                urlPattern = urlPatternToReturnTo;       
            } else {
                urlPattern = "/individual";         
            }
            return urlPattern;
		}
		
		//Get resource to redirect to
		public static String getResourceToRedirect(VitroRequest vreq, EditConfigurationVTwo editConfig, String entityToReturnTo ) {
			String resourceToRedirectTo = null;
			if( editConfig != null ){

                if( editConfig.getEntityToReturnTo() != null && editConfig.getEntityToReturnTo().startsWith("?") ){             
                    resourceToRedirectTo = entityToReturnTo;            
                }else{            
                    resourceToRedirectTo = editConfig.getEntityToReturnTo();
                }
                
                //if there is no entity to return to it is likely a cancel
                if( resourceToRedirectTo == null || resourceToRedirectTo.length() == 0 )
                    resourceToRedirectTo = editConfig.getSubjectUri();                
            }
			return resourceToRedirectTo;
		}
		

			
	}
}
