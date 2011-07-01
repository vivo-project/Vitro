/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper.TemplateProcessingException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;

/**
 * This servlet will process EditConfigurations with query parameters
 * to perform an edit.
 * 
 * TODO: rename this class ProcessN3Edit  
 */
public class ProcessRdfFormController extends FreemarkerHttpServlet{
	
    private Log log = LogFactory.getLog(ProcessRdfFormController.class);
    	
    
	//bdc34: this is likely to become a servlet instead of a jsp.
	// You can get a reference to the servlet from the context.
	// this will need to be converted from a jsp to something else
	public static final String POST_EDIT_CLEANUP_JSP = "postEditCleanUp.jsp";	   	    
    	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
    ServletException, IOException{
	    doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws
									ServletException, IOException{		
		VitroRequest vreq = new VitroRequest(request);
		
		//get the EditConfiguration 
		EditConfigurationVTwo configuration = getEditConfiguration(request);
        if(configuration == null){
            doEditConfigNotFound( vreq, response);            
            return;
        }

        //get the EditSubmission
        MultiValueEditSubmission submission = new MultiValueEditSubmission(vreq.getParameterMap(), configuration);        	
        EditSubmissionUtils.putEditSubmissionInSession(request.getSession(), submission);

		boolean hasErrors = doValidationErrors(vreq, configuration, submission, response);
		if( hasErrors)
		    return; //processValidationErrors() already forwarded to redisplay the form with validation messages 		

        // get the models to work with in case the write model and query model are not the defaults 
		OntModel queryModel = configuration.getQueryModelSelector().getModel(request, getServletContext());		
	    OntModel writeModel = configuration.getWriteModelSelector().getModel(request,getServletContext());  
	    
	    AdditionsAndRetractions changes;
		if(configuration.isUpdate()){
		    changes = ProcessRdfForm.editExistingResource(configuration, submission);
		}else{
		    changes = ProcessRdfForm.createNewResource(configuration, submission);
		}		
		
		if( configuration.isUseDependentResourceDelete() )
		    changes = ProcessRdfForm.addDependentDeletes(changes, queryModel);		
		
		ProcessRdfForm.preprocessModels(changes, configuration, vreq);
		
		ProcessRdfForm.applyChangesToWriteModel(changes, queryModel, writeModel, EditN3Utils.getEditorUri(vreq) );
		
		//Here we are trying to get the entity to return to URL, 
		//Maybe this should be in POST_EDIT_CLEANUP? 
        if( configuration.getEntityToReturnTo() != null ){      
            request.setAttribute("entityToReturnTo", ProcessRdfForm.substitueForURL( configuration, submission));                   
        }      
        
        doPostEdit(vreq,response);		
	}
	private EditConfigurationVTwo getEditConfiguration(HttpServletRequest request) {
		HttpSession session = request.getSession();
		EditConfigurationVTwo editConfiguration = EditConfigurationVTwo.getConfigFromSession(session, request);		
		return editConfiguration;
	}
	    private void doEditConfigNotFound(VitroRequest request, HttpServletResponse response) {
        HashMap<String,Object>map = new HashMap<String,Object>();
        map.put("message", "No editing configuration found, cannot process edit.");
        ResponseValues values = new TemplateResponseValues("message.ftl", map);        
        try {
            doResponse(request,response,values);
        } catch (TemplateProcessingException e) {
            log.error("Could not process template for doEditConfigNotFound()",e);
        }        
    }

	private boolean doValidationErrors(VitroRequest vreq,
			EditConfigurationVTwo editConfiguration, MultiValueEditSubmission submission,
			HttpServletResponse response) throws ServletException, IOException {
		
		Map<String, String> errors = submission.getValidationErrors();
		
		if(errors != null && !errors.isEmpty()){
			String form = editConfiguration.getFormUrl();
			vreq.setAttribute("formUrl", form);
			vreq.setAttribute("view", vreq.getParameter("view"));			
						
	        RequestDispatcher requestDispatcher = vreq.getRequestDispatcher(editConfiguration.getFormUrl());
	        requestDispatcher.forward(vreq, response);
	        return true;    
		}
		return false;		
	}

    private void doPostEdit(VitroRequest vreq, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = vreq.getRequestDispatcher(POST_EDIT_CLEANUP_JSP);
        requestDispatcher.forward(vreq, response);
    }    
	
	//Move to EditN3Utils but keep make new uris here
	public static class Utilities {
		
		private static Log log = LogFactory.getLog(ProcessRdfFormController.class);
	    static Random random = new Random();
		
	    public static Map<String,List<String>> fieldsToAssertionMap( Map<String,FieldVTwo> fields){
	        Map<String,List<String>> out = new HashMap<String,List<String>>();
	        for( String fieldName : fields.keySet()){
	            FieldVTwo field = fields.get(fieldName);

	            List<String> copyOfN3 = new ArrayList<String>();
	            for( String str : field.getAssertions()){
	                copyOfN3.add(str);
	            }
	            out.put( fieldName, copyOfN3 );
	        }
	        return out;
	    }

	     public static Map<String,List<String>> fieldsToRetractionMap( Map<String,FieldVTwo> fields){
	        Map<String,List<String>> out = new HashMap<String,List<String>>();
	        for( String fieldName : fields.keySet()){
	            FieldVTwo field = fields.get(fieldName);

	            List<String> copyOfN3 = new ArrayList<String>();
	            for( String str : field.getRetractions()){
	                copyOfN3.add(str);
	            }
	            out.put( fieldName, copyOfN3 );
	        }
	        return out;
	    }	
	     
	     public static Map<String,String> newToUriMap(Map<String,String> newResources, WebappDaoFactory wdf){
	         HashMap<String,String> newVarsToUris = new HashMap<String,String>();
	         HashSet<String> newUris = new HashSet<String>();
	         for( String key : newResources.keySet()){        	
	             String prefix = newResources.get(key);
	         	String uri = makeNewUri(prefix, wdf);
	         	while( newUris.contains(uri) ){
	         		uri = makeNewUri(prefix,wdf);
	         	}
	         	newVarsToUris.put(key,uri);
	         	newUris.add(uri);
	         }
	          return newVarsToUris;
	     }

	     
	     public static String makeNewUri(String prefix, WebappDaoFactory wdf){
	         if( prefix == null || prefix.length() == 0 ){
	         	String uri = null;       
	         	try{
	         		uri = wdf.getIndividualDao().getUnusedURI(null);
	             }catch(InsertException ex){
	             	log.error("could not create uri");
	             }        
	 			return uri;
	         }
	         
	         String goodURI = null;
	         int attempts = 0;
	         while( goodURI == null && attempts < 30 ){            
	             Individual ind = new IndividualImpl();
	             ind.setURI( prefix + random.nextInt() );
	             try{
	         		goodURI = wdf.getIndividualDao().getUnusedURI(ind);
	             }catch(InsertException ex){
	             	log.debug("could not create uri");
	             }
	             attempts++;
	         }        
	         if( goodURI == null )
	         	log.error("could not create uri for prefix " + prefix);
	         return goodURI;
	     
	     }
	  
	     	      	     
	     
	}					
}
