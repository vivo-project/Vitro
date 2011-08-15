/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.util.ArrayList;
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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;

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
    	
	
	@Override 
	protected ResponseValues processRequest(VitroRequest vreq) {						
		//get the EditConfiguration 
		EditConfigurationVTwo configuration = getEditConfiguration(vreq);
        if(configuration == null)
            throw new Error("No edit configuration found.");        

        //get the EditSubmission
        MultiValueEditSubmission submission = new MultiValueEditSubmission(vreq.getParameterMap(), configuration);        	
        EditSubmissionUtils.putEditSubmissionInSession(vreq.getSession(), submission);

		ResponseValues errorResponse = doValidationErrors(vreq, configuration, submission);
		if( errorResponse != null )
		    return errorResponse;

        // get the models to work with in case the write model and query model are not the defaults 
		OntModel queryModel = configuration.getQueryModelSelector().getModel(vreq, getServletContext());		
	    OntModel writeModel = configuration.getWriteModelSelector().getModel(vreq,getServletContext());  
	    
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
        if( configuration.getEntityToReturnTo() != null ){      
            vreq.setAttribute("entityToReturnTo", ProcessRdfForm.substitueForURL( configuration, submission));                   
        }      
        
        return doPostEdit(vreq);		
	}
	
	private EditConfigurationVTwo getEditConfiguration(HttpServletRequest request) {
		HttpSession session = request.getSession();
		EditConfigurationVTwo editConfiguration = EditConfigurationVTwo.getConfigFromSession(session, request);		
		return editConfiguration;
	}
	
//	private ResponseValues doEditConfigNotFound(VitroRequest request) {
//        HashMap<String,Object>map = new HashMap<String,Object>();
//        map.put("message", "No editing configuration found, cannot process edit.");
//        ResponseValues values = new TemplateResponseValues("message.ftl", map);        
//        try {
//            doResponse(request,values);
//        } catch (TemplateProcessingException e) {
//            log.error("Could not process template for doEditConfigNotFound()",e);
//        }        
//    }

	private ResponseValues doValidationErrors(VitroRequest vreq,
			EditConfigurationVTwo editConfiguration, MultiValueEditSubmission submission) {
		
		Map<String, String> errors = submission.getValidationErrors();
		
		if(errors != null && !errors.isEmpty()){
			String form = editConfiguration.getFormUrl();
			vreq.setAttribute("formUrl", form);
			vreq.setAttribute("view", vreq.getParameter("view"));			
							        
	        return new RedirectResponseValues(editConfiguration.getFormUrl());
		}
		return null; //no errors		
	}

    private RedirectResponseValues doPostEdit(VitroRequest vreq ) {
        String resourceToRedirectTo = null;   
        String urlPattern = null;
        String predicateLocalName = null;
        String predicateAnchor = "";
        HttpSession session = vreq.getSession(false);
        if( session != null ) {
            EditConfigurationVTwo editConfig = EditConfigurationVTwo.getConfigFromSession(session,vreq);
            //In order to support back button resubmissions, don't remove the editConfig from session.
            //EditConfiguration.clearEditConfigurationInSession(session, editConfig);            

            MultiValueEditSubmission editSub = EditSubmissionUtils.getEditSubmissionFromSession(session,editConfig);        
            EditSubmissionUtils.clearEditSubmissionInSession(session, editSub);
            
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
                            
                if( editConfig.getEntityToReturnTo() != null && editConfig.getEntityToReturnTo().startsWith("?") ){             
                    resourceToRedirectTo = (String)vreq.getAttribute("entityToReturnTo");            
                }else{            
                    resourceToRedirectTo = editConfig.getEntityToReturnTo();
                }
                
                //if there is no entity to return to it is likely a cancel
                if( resourceToRedirectTo == null || resourceToRedirectTo.length() == 0 )
                    resourceToRedirectTo = editConfig.getSubjectUri();                
            }
            
            //set up base URL
            String cancel = vreq.getParameter("cancel");
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
            
            //looks like a redirect to a profile page, try to add anchor for property that was just edited.
            if( urlPattern.endsWith("individual") || urlPattern.endsWith("entity") ){           
                if( predicateLocalName != null && predicateLocalName.length() > 0){
                    predicateAnchor = "#" + predicateLocalName;
                    vreq.setAttribute("predicateAnchor", predicateAnchor);
                }
            }
        }
        
        if( resourceToRedirectTo != null ){
            ParamMap paramMap = new ParamMap();
            paramMap.put("uri", resourceToRedirectTo);
            paramMap.put("extra","true"); //for ie6            
            return new RedirectResponseValues( UrlBuilder.getPath(urlPattern,paramMap) + predicateAnchor );
        } else if ( !urlPattern.endsWith("individual") && !urlPattern.endsWith("entity") ){
            return new RedirectResponseValues( urlPattern );
        }
            
    
        return new RedirectResponseValues( UrlBuilder.getUrl(Route.LOGIN) );
        
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
