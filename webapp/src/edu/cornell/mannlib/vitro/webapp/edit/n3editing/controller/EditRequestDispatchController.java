/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import static edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils.getPredicateUri;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoFrontEndEditing;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.DirectRedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationGenerator;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;
import edu.cornell.mannlib.vitro.webapp.web.beanswrappers.ReadOnlyBeansWrapper;
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
	protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new DoFrontEndEditing());
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
    	return(vreq.getParameter("switchToDisplayModel") != null); 
	}

	private ResponseValues redirectToMenuEdit(VitroRequest vreq) {
		String queryString = vreq.getQueryString();
		String redirectPage = vreq.getContextPath() + "/editDisplayModel?" + queryString;
        return new DirectRedirectResponseValues(redirectPage, HttpServletResponse.SC_SEE_OTHER);

	}
    private MultiValueEditSubmission getMultiValueSubmission(VitroRequest vreq, EditConfigurationVTwo editConfig) {
		return EditSubmissionUtils.getEditSubmissionFromSession(vreq.getSession(), editConfig);
	}

	//TODO: should more of what happens in this method
    //happen in the generators?
	private EditConfigurationVTwo setupEditConfiguration(String editConfGeneratorName,
			VitroRequest vreq) {	    	    	    
    	HttpSession session = vreq.getSession();
    	EditConfigurationVTwo editConfig = 
    	    makeEditConfigurationVTwo( editConfGeneratorName, vreq, session);

        //edit key is set here, NOT in the generator class
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

	//Additional forwards.. should they be processed here to see which form should be forwarded to
	//e.g. default add individual form etc. and additional scenarios
	//TODO: Check if additional scenarios should be checked here
	private String processEditConfGeneratorName(VitroRequest vreq) {
    	//use default object property form if nothing else works
        String editConfGeneratorName = DEFAULT_OBJ_FORM;

        //Handle deletion before any of the other cases        
        if(isDeleteForm(vreq)) {
        	return DEFAULT_DELETE_FORM;
        }        
                        
        // *** handle the case where the form is specified as a request parameter ***
        String formParam = getFormParam(vreq);
        if(  formParam != null && !formParam.isEmpty() ){
            //form parameter must be a fully qualified java class name of a EditConfigurationVTwoGenerator implementation.
            return formParam;              
        }
        
        String predicateUri =  getPredicateUri(vreq);        
        if( isVitroLabel(predicateUri) ) { //in case of data property
        	return RDFS_LABEL_FORM;
        } 
        
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();        
        Property prop = getPropertyByUri(predicateUri, wdf);
        
        if(isDataProperty( prop , wdf )){
            editConfGeneratorName = DEFAULT_DATA_FORM;
        } else{
        	String customForm = prop.getCustomEntryForm();
        	if(customForm != null && !customForm.trim().isEmpty()) {
        		editConfGeneratorName = customForm;
        	}
        }
        
        log.debug("generator name is " + editConfGeneratorName);
        return editConfGeneratorName;
	}

    private String getCustomForm(String predicateUri, WebappDaoFactory wdf) {
		Property prop = getPropertyByUri(predicateUri, wdf);
		return prop.getCustomEntryForm();
	}

	private Property getPropertyByUri(String predicateUri, WebappDaoFactory wdf) {
		Property p = null;
		p = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
		if(p == null) {
			p = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
		}
		return p;
	}

	private boolean isVitroLabel(String predicateUri) {
		return predicateUri.equals(VitroVocabulary.LABEL);
	}

    private boolean isDataProperty(Property prop, WebappDaoFactory wdf) {
        if( prop != null && prop instanceof DataProperty ){
            return true;
        }else{
            DataProperty dataProp = wdf.getDataPropertyDao().getDataPropertyByURI(prop.getURI());
            if( dataProp != null )
                return true;
            else
                return false;
        }
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
    
    
    //
    
    private EditConfigurationVTwo makeEditConfigurationVTwo(
            String editConfGeneratorName, VitroRequest vreq, HttpSession session) {
    	
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
