/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import static edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils.getPredicateUri;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.DirectRedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationGenerator;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit.EditConfigurationTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit.MultiValueEditSubmissionTemplateModel;

/**
 * This servlet is intended to handle all requests to create a form for use
 * by the N3 editing system.  It will examine the request parameters, determine
 * which form to use, execute a EditConfiguration setup, and evaluate the
 * view indicated by the EditConfiguration.
 * 
 * Do not add code to this class to achieve some behavior in a 
 * form.  Try adding the behavior logic to the code that generates the
 * EditConfiguration for the form.  
 */
public class EditRequestDispatchController extends FreemarkerHttpServlet {
    private static final long serialVersionUID = 1L;
    public static Log log = LogFactory.getLog(EditRequestDispatchController.class);
    
    final String DEFAULT_OBJ_FORM = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultObjectPropertyFormGenerator";
    final String DEFAULT_DATA_FORM = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDataPropertyFormGenerator";
    //TODO: Create this generator
    final String RDFS_LABEL_FORM = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.RDFSLabelGenerator";
    final String DEFAULT_DELETE_FORM = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDeleteGenerator";

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return SimplePermission.DO_FRONT_END_EDITING.ACTION;
	}

	@Override
    protected ResponseValues processRequest(VitroRequest vreq) {
      
    	try{
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        if(isMenuMode(vreq)) {
        	return redirectToMenuEdit(vreq);
        }
        
        //check some error conditions and if they exist return response values
         //with error message
         if(isErrorCondition(vreq)){
        	 return doHelp(vreq, getErrorMessage(vreq));
         }
        
         //if edit form needs to be skipped to object instead
         if(isSkipPredicate(vreq)) {
             log.debug("The predicate is a annotated as a skip.");
        	 return processSkipEditForm(vreq);
         }                  
     
        //Get the edit generator name
         String editConfGeneratorName = processEditConfGeneratorName(vreq);
        
         //session attribute 
         setSessionRequestFromEntity(vreq);
 
         // make new or get an existing edit configuration          
         EditConfigurationVTwo editConfig = setupEditConfiguration(editConfGeneratorName, vreq);
         log.debug("editConfiguration:\n" + editConfig );

         //if the EditConfig indicates a URL to skip to, then redirect to that URL
         if( editConfig.getSkipToUrl() != null ){
             return new DirectRedirectResponseValues(editConfig.getSkipToUrl());
         }
         
         //what template?
         String template = editConfig.getTemplate();
        
         //Get the multi value edit submission object
         MultiValueEditSubmission submission = getMultiValueSubmission(vreq, editConfig);
         MultiValueEditSubmissionTemplateModel submissionTemplateModel = new MultiValueEditSubmissionTemplateModel(submission);
         
         //what goes in the map for templates?
         Map<String,Object> templateData = new HashMap<String,Object>();
         EditConfigurationTemplateModel etm = new EditConfigurationTemplateModel( editConfig, vreq);
         templateData.put("editConfiguration", etm);
         templateData.put("editSubmission", submissionTemplateModel);
         //Corresponding to original note for consistency with selenium tests and 1.1.1
         templateData.put("title", "Edit");
         templateData.put("submitUrl", getSubmissionUrl(vreq));
         templateData.put("cancelUrl", etm.getCancelUrl());
         templateData.put("editKey", editConfig.getEditKey());
         //This may change based on the particular generator? Check if true
         templateData.put("bodyClasses", "formsEdit");
         return new TemplateResponseValues(template, templateData);
         }catch(Throwable th){
        	
        	 HashMap<String,Object> map = new HashMap<String,Object>();
        	 map.put("errorMessage", th.toString());
        	 log.error(th,th);
        	 return new TemplateResponseValues("error-message.ftl", map);
        
         }
    }    



	private boolean isMenuMode(VitroRequest vreq) {
		//Check if special model, in which case forward
	    //bdc34: the EditRequestDispatchController cannot hijack 
	    // all edits to the display model and force them into /editDisplayModel .
	    // Consider changing URLs used on special menu form to point to /editMenu or something.
    	//return(vreq.getParameter("switchToDisplayModel") != null);
	    return false;
	}

	private ResponseValues redirectToMenuEdit(VitroRequest vreq) {
	    throw new RuntimeException("The EditRequestDispatchController cannot hijack"+ 
        "all edits to the display model and force them into /editDisplayModel ."+
        " Consider changing URLs used on special menu form to point to /editDisplayModel");        
	}
	
    private MultiValueEditSubmission getMultiValueSubmission(VitroRequest vreq, EditConfigurationVTwo editConfig) {
		return EditSubmissionUtils.getEditSubmissionFromSession(vreq.getSession(), editConfig);
	}

	//TODO: should more of what happens in this method
    //happen in the generators?
	private EditConfigurationVTwo setupEditConfiguration(String editConfGeneratorName,
			VitroRequest vreq) throws Exception {	    	    	    
    	HttpSession session = vreq.getSession();
    	//Originally, this code called makeEditConfiguration before checking for/setting the edit key
    	//which meant that in the case of page reload on an error, you would recreate an edit configuration
    	//using the generator
    	//Given recent updates enabling modification of edit configuration dynamically through AJAX,
    	//we will first check whether the edit key exists and if there is already an edit configuration
    	//in the session - and then will utilize the edit configuration that already exists
    	//edit key is set here, NOT in the generator class
    	EditConfigurationVTwo editConfig = null;
    	EditConfigurationVTwo existingConfig = EditConfigurationVTwo.getConfigFromSession(session, vreq);
    	//if delete form from the editing page, then edit configuration already exists and the 
    	//delete generator wouldn't be used, we need to make sure that it is used if it's a delete option
    	if(existingConfig != null && !isDeleteForm(vreq)) {
    		editConfig = existingConfig;
    	} else {
    		editConfig = 
    	    	    makeEditConfigurationVTwo( editConfGeneratorName, vreq, session);
    	}
    	 
    	if(editConfig == null) {
    	    log.error("editConfig is null! How did this happen?");
    	}
    	String editKey = EditConfigurationUtils.getEditKey(vreq); 
    	editConfig.setEditKey(editKey);        
        

        //put edit configuration in session so it can be accessed on form submit.
        EditConfigurationVTwo.putConfigInSession(editConfig, session);
		return editConfig;
	}

	private void setSessionRequestFromEntity(VitroRequest vreq) {
		HttpSession session = vreq.getSession();
		String subjectUri = vreq.getParameter("subjectUri");
		if(session.getAttribute("requestedFromEntity") == null) {
			session.setAttribute("requestedFromEntity", subjectUri);
		}
		
	}

	/**
	 * Determine the java class name to use for the EditConfigurationGenerator.
	 * 
	 * Forwarding should NOT be done here.  If an EditConfiguration needs to 
	 * forward to a URL use editConfig.getSkipToUrl(). Also consider using a skip predicate annotation.
	 */
	private String processEditConfGeneratorName(VitroRequest vreq) {
	    String editConfGeneratorName = null;
	    
	    String predicateUri =  getPredicateUri(vreq);
	    String domainUri = EditConfigurationUtils.getDomainUri(vreq);
	    String rangeUri = EditConfigurationUtils.getRangeUri(vreq);
	    
        // *** handle the case where the form is specified as a request parameter ***	    
        String formParam = getFormParam(vreq);
        if(  formParam != null && !formParam.isEmpty() ){
            // please, always do this case first.
            //form parameter must be a fully qualified java class name of a EditConfigurationVTwoGenerator implementation.
            editConfGeneratorName = formParam;
            
        //  *** do magic cmd=delete 
        }else if(isDeleteForm(vreq)) {
            //TODO: cmd=delete would be better if it was moved to the the EditConfigurationGenerator that uses it.
        	return DEFAULT_DELETE_FORM;

      	// *** check for a predicate URI in the request        	
        }else if( predicateUri != null && !predicateUri.isEmpty() ){                      
            Property prop = getProperty( predicateUri, domainUri, rangeUri, vreq);
            if (prop != null && rangeUri != null) {
                editConfGeneratorName = getCustomEntryForm(prop);
            } else if( prop != null && prop.getCustomEntryForm() != null ){
                //there is a custom form, great! let's use it.
                editConfGeneratorName = prop.getCustomEntryForm();
                
            }else if( RDFS.label.getURI().equals( predicateUri ) ) {
                // set RDFS_LABLE_FORM after a custom entry form on the property
                // so that there is a chance that rdfs:label could have a configurable 
                // custom form it the future
                editConfGeneratorName = RDFS_LABEL_FORM;
                
            }else if( isDataProperty(prop) ){                   
                editConfGeneratorName = DEFAULT_DATA_FORM;
            }else{
                editConfGeneratorName = DEFAULT_OBJ_FORM;
            }
            
        // *** default to the object property form when there is nothing
        }else{       
            editConfGeneratorName = DEFAULT_OBJ_FORM;
        }
                
        if( editConfGeneratorName == null )
            log.error("problem: editConfGeneratorName is null but " +
            		"processEditConfGeneratorName() should never return null.");
        
        log.debug("generator name is " + editConfGeneratorName);
        return editConfGeneratorName;
	}

	private String getCustomEntryForm(Property prop){
	    if (prop.getCustomEntryForm() == null) {
	        return DEFAULT_OBJ_FORM;
	    } else {
	        return prop.getCustomEntryForm();
	    }
	}
	
	private Property getProperty(String predicateUri, String domainUri, String rangeUri, VitroRequest vreq) {	   
		Property p = null;
		try{
    		p = vreq.getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURIs(
    		        predicateUri, domainUri, rangeUri);
    		if(p == null) {
    			p = vreq.getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(predicateUri);
    		}
		}catch( Throwable th){
  		    //ignore problems
		    log.debug("Not really a problem if we cannot get a property because we "+
		            "might be editing arbritrary RDF", th);
		}
		return p;
	}

	private boolean isVitroLabel(String predicateUri) {
		return predicateUri.equals(VitroVocabulary.LABEL);
	}

    private boolean isDataProperty( Property prop ) {
        return ( prop != null && prop instanceof DataProperty );        
    }


	//if skip edit form
	private boolean isSkipPredicate(VitroRequest vreq) {
		 //Certain predicates may be annotated to change the behavior of the edit
        //link.  Check for this annotation and, if present, simply redirect 
        //to the normal individual display for the object URI instead of bringing
        //up an editing form.
        //Note that we do not want this behavior for the delete link (handled above).
        // This might be done in the custom form jsp for publicaitons already.
        // so maybe this logic shouldn't be here?
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        String predicateUri = vreq.getParameter("predicateUri");
        boolean isEditOfExistingStmt = isEditOfExistingStmt(vreq);
        return (isEditOfExistingStmt && (wdf.getObjectPropertyDao().skipEditForm(predicateUri)));
	}

    private ResponseValues processSkipEditForm(VitroRequest vreq) {        
        ParamMap params = new ParamMap();
        params.put("uri",EditConfigurationUtils.getObjectUri(vreq));
        params.put("relatedSubjectUri",EditConfigurationUtils.getSubjectUri(vreq));
        params.put("relatingPredicateUri",EditConfigurationUtils.getPredicateUri(vreq));
                
        return new DirectRedirectResponseValues(
                UrlBuilder.getUrl(UrlBuilder.Route.INDIVIDUAL, params),
                HttpServletResponse.SC_SEE_OTHER);		
	}

	//Check error conditions
    //TODO: Do we need both methods or jsut one?
    private boolean isErrorCondition(VitroRequest vreq) {
    	 String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
         String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
         String formParam = getFormParam(vreq);
         //if no form parameter, then predicate uri and subject uri must both be populated
    	if (formParam == null || "".equals(formParam)) {
            if ((predicateUri == null || predicateUri.trim().length() == 0)) {
            	return true;
            }
            if (subjectUri == null || subjectUri.trim().length() == 0){
            	return true;
                        
            }
        }
    	
    	//Check predicate - if not vitro label and neither data prop nor object prop return error
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	//TODO: Check if any error conditions are not met here
    	//At this point, if there is a form paramter, we don't require a predicate uri
    	if(formParam == null 
    			&& predicateUri != null 
    			&& !EditConfigurationUtils.isObjectProperty(predicateUri, vreq) 
    			&& !isVitroLabel(predicateUri)
    			&& !EditConfigurationUtils.isDataProperty(predicateUri, vreq))
    	{
    		return true;
    	}
    	return false;
    }
    
    private String getErrorMessage(VitroRequest vreq) {
    	String errorMessage = null;
    	 String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
         String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
         String formParam = getFormParam(vreq);
         if (formParam == null || "".equals(formParam)) {
             if ((predicateUri == null || predicateUri.trim().length() == 0)) {
            	 errorMessage = "No form was specified, both predicateUri and"
                     + " editform are empty. One of these is required"
                     + " by editRequestDispatch to choose a form.";
             }
             if (subjectUri == null || subjectUri.trim().length() == 0){
                 return "subjectUri was empty. If no editForm is specified," +
                 		" it is required by EditRequestDispatch.";                
             }
         }
         return errorMessage;
    }
    
	//should return null
	private String getFormParam(VitroRequest vreq) {
		String formParam = (String) vreq.getParameter("editForm");
		return formParam;
	}
    
    private boolean isEditOfExistingStmt(VitroRequest vreq) {
        String objectUri = vreq.getParameter("objectUri");

    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	if(objectUri != null) {
        	Individual object = wdf.getIndividualDao().getIndividualByURI(objectUri);
        	return (object != null);
    	}
    	return false;
    }
    
    //Check whether command is delete and either process or save
    //Original code involved doing a jsp forward
    //TODO: Check how to integrate deletion
    private boolean isDeleteForm(VitroRequest vreq) {
    	String command = vreq.getParameter("cmd");
        if ("delete".equals(command)) {
       	 	return true;
        }
        return false;
    }
    
       
    private EditConfigurationVTwo makeEditConfigurationVTwo(
            String editConfGeneratorName, VitroRequest vreq, HttpSession session) throws Exception {
    	
    	EditConfigurationGenerator EditConfigurationVTwoGenerator = null;
    	
        Object object = null;
        try {
            Class classDefinition = Class.forName(editConfGeneratorName);
            object = classDefinition.newInstance();
            EditConfigurationVTwoGenerator = (EditConfigurationGenerator) object;
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }    	
        
        if(EditConfigurationVTwoGenerator == null){
        	throw new Error("Could not find EditConfigurationVTwoGenerator " + editConfGeneratorName);        	
        } else {
            log.debug("Created EditConfiguration using " + editConfGeneratorName);
            return EditConfigurationVTwoGenerator.getEditConfiguration(vreq, session);
        }
        
    }

    
    private ResponseValues doHelp(VitroRequest vreq, String message){
        //output some sort of help message for the developers.
        
    	HashMap<String,Object> map = new HashMap<String,Object>();
   	 map.put("errorMessage", "help is not yet implemented");
   	 return new TemplateResponseValues("error-message.ftl", map);    }
    
    
    //Get submission url
    private String getSubmissionUrl(VitroRequest vreq) {
    	return vreq.getContextPath() + "/edit/process";
    }
    
    
}
