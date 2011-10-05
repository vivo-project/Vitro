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
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;

import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
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
	    AdditionsAndRetractions changes = getAdditionsAndRetractions(configuration, submission, vreq);
		
		if( configuration.isUseDependentResourceDelete() )
		    changes = ProcessRdfForm.addDependentDeletes(changes, queryModel);		
		
		ProcessRdfForm.preprocessModels(changes, configuration, vreq);
		
		ProcessRdfForm.applyChangesToWriteModel(changes, queryModel, writeModel, EditN3Utils.getEditorUri(vreq) );
		
		//Here we are trying to get the entity to return to URL,  
		//More involved processing for data property apparently
		//Also what do we actually DO with the vreq attribute: Answer: Use it for redirection
		//And no where else so we could technically calculate and send that here
		String entityToReturnTo = ProcessRdfForm.processEntityToReturnTo(configuration, submission, vreq);
        //For data property processing, need to update edit configuration for back button 
		ProcessRdfForm.updateEditConfigurationForBackButton(configuration, submission, vreq, writeModel);
        return doPostEdit(vreq, entityToReturnTo);		
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
		Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		 if (editConfig.getDatapropKey() == null
	                || editConfig.getDatapropKey().length() == 0)
	            return false;
	        
        int dpropHash = Integer.parseInt(editConfig.getDatapropKey());
        DataPropertyStatement dps = RdfLiteralHash.getPropertyStmtByHash(subject, editConfig.getPredicateUri(), dpropHash, model);

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
	
	
	private AdditionsAndRetractions getAdditionsAndRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, VitroRequest vreq) {
		
		AdditionsAndRetractions changes = null;
		//if editing existing resource or literal 
		if(configuration.isObjectPropertyUpdate() || configuration.isDataPropertyUpdate()) {
			changes = ProcessRdfForm.editExistingStatement(configuration, submission, vreq);
		} else {
			changes = ProcessRdfForm.createNewStatement(configuration, submission, vreq);
		}
		
		return changes;
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
	
	//TODO: Is this the equivalent of post Edit Cleanup
	//Also do we wish to continue setting attributes on the request?

    private RedirectResponseValues doPostEdit(VitroRequest vreq, String resourceToRedirectTo ) {
        String urlPattern = null;
        String predicateAnchor = "";
        HttpSession session = vreq.getSession(false);
        if( session != null ) {
            EditConfigurationVTwo editConfig = EditConfigurationVTwo.getConfigFromSession(session,vreq);
            //In order to support back button resubmissions, don't remove the editConfig from session.
            //EditConfiguration.clearEditConfigurationInSession(session, editConfig);            

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
            
    
        return new RedirectResponseValues( UrlBuilder.getUrl(Route.LOGIN) );
        
    }    
	
	//Move to EditN3Utils but keep make new uris here
	public static class Utilities {
		
		private static Log log = LogFactory.getLog(ProcessRdfFormController.class);
	    static Random random = new Random();
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
	    
	    public static List<String> getFieldStmts(FieldVTwo field, String stmtType) {
	    	if(stmtType.equals(assertionsType)) {
	    		return field.getAssertions();
            } else {
            	return field.getRetractions();
            }
	    }
	    
	    public static Map<String, List<String>> fieldsToN3Map(Map<String, FieldVTwo> fields, String stmtType) {
	    	Map<String,List<String>> out = new HashMap<String,List<String>>();
	        for( String fieldName : fields.keySet()){
	            FieldVTwo field = fields.get(fieldName);
	            List<String> n3Stmts = getFieldStmts(field, stmtType);
	            List<String> copyOfN3 = makeListCopy(n3Stmts);
	            out.put( fieldName, copyOfN3 );
	        }
	        return out;
	    }
	    
	    public static Map<String,List<String>> fieldsToAssertionMap( Map<String,FieldVTwo> fields){
	       return fieldsToN3Map(fields, assertionsType);
	    }

	     public static Map<String,List<String>> fieldsToRetractionMap( Map<String,FieldVTwo> fields){
	      return fieldsToN3Map(fields, retractionsType);
	    }	
	     
	     //this works differently based on whether this is object property editing or data property editing
	     //Object prop version below
	     //Also updating to allow an array to be returned with the uri instead of a single uri
	     //Note this would require more analysis in context of multiple uris possible for a field
	     public static Map<String,List<String>> newToUriMap(Map<String,String> newResources, WebappDaoFactory wdf){
	         HashMap<String,List<String>> newVarsToUris = new HashMap<String,List<String>>();
	         HashSet<String> newUris = new HashSet<String>();
	         for( String key : newResources.keySet()){        	
	             String prefix = newResources.get(key);
	         	String uri = makeNewUri(prefix, wdf);
	         	while( newUris.contains(uri) ){
	         		uri = makeNewUri(prefix,wdf);
	         	}
	         	List<String> urisList = new ArrayList<String>();
	         	urisList.add(uri);
	         	newVarsToUris.put(key,urisList);
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
	     
	     //Data prop version, from processDatapropRdfForm.jsp
	     //TODO: Should this even be set this way as this needs to be changed somehow?
	     public static String defaultUriPrefix = "http://vivo.library.cornell.edu/ns/0.1#individual";
	     public static Map<String, List<String>> newToUriMap(Map<String, String> newResources,
	             Model model) {
	         HashMap<String, List<String>> newUris = new HashMap<String, List<String>>();
	         for (String key : newResources.keySet()) {
	        	 List<String> urisList = new ArrayList<String>();
		         urisList.add(makeNewUri(newResources.get(key), model));
	             newUris.put(key, urisList);
	         }
	         return newUris;
	     }

	     //This is the data property  method, this is to be removed
	     //TODO: Remove this method and ensure this is not called
	     public static String makeNewUri(String prefix, Model model) {
	         if (prefix == null || prefix.length() == 0)
	             prefix = defaultUriPrefix;

	         String uri = prefix + random.nextInt();
	         Resource r = ResourceFactory.createResource(uri);
	         while (model.containsResource(r)) {
	             uri = prefix + random.nextInt();
	             r = ResourceFactory.createResource(uri);
	         }
	         return uri;
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
		public static boolean checkForEmptyString(
				MultiValueEditSubmission submission,
				EditConfigurationVTwo configuration, VitroRequest vreq) {
			// TODO Auto-generated method stub
			if(isDataProperty(configuration, vreq)) {
				// Our editors have gotten into the habbit of clearing the text from the
			    // textarea and saving it to invoke a delete.  see Issue VITRO-432   
		        if (configuration.getFields().size() == 1) {
		            String onlyField = configuration.getFields().keySet().iterator()
		                    .next();
		            List<Literal> value = submission.getLiteralsFromForm().get(onlyField);
		            if( value == null || value.size() == 0){
		            	log.debug("No parameters found in submission for field \"" + onlyField +"\"");
		            	return true;
		            }else {
		            	if(value.size() == 1) {
		            		if( "".equals(value.get(0).getLexicalForm())) {
		            			log.debug("Submission was a single field named \"" + onlyField + "\" with an empty string");
		            			return true;
		            		}
		            	}
		            }
		        }
			}
			return false;    
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
		
		public static String getPredicateAnchorPostEdit(String urlPattern,
				String predicateLocalName) {
			String predicateAnchor = null;
			if( urlPattern.endsWith("individual") || urlPattern.endsWith("entity") ){           
                if( predicateLocalName != null && predicateLocalName.length() > 0){
                    predicateAnchor = "#" + predicateLocalName;
                }
            }
			return predicateAnchor;
		}
	}
}
