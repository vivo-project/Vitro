/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationGenerator;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit.EditConfigurationTemplateModel;

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
    final String DEFAULT_ERROR_FORM = "error.jsp";
    final String DEFAULT_ADD_INDIVIDUAL = "defaultAddMissingIndividualForm.jsp";
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        Map mapTest = vreq.getParameterMap();
        java.util.Iterator testIterator = mapTest.keySet().iterator();
        while(testIterator.hasNext()) {
        	String paramKey = (String) testIterator.next();
        	System.out.println("Param key is " + paramKey + " and test iterator is value is " +  mapTest.get(paramKey).toString());
        }
    	try{
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        //get edit key.  
        //The edit key links submissions to EditConfiguration objects in the session.
        HttpSession session = vreq.getSession();        
        String editKey = 
            (EditConfigurationVTwo.getEditKey(vreq) == null) 
                ? EditConfigurationVTwo.newEditKey(session)
                : EditConfigurationVTwo.getEditKey(vreq);
        vreq.setAttribute("editKey", editKey);
        
        //set title to Edit to maintain functionality from 1.1.1 and avoid updates to Selenium tests
        vreq.setAttribute("title","Edit");       

         String subjectUri = vreq.getParameter("subjectUri");
         String predicateUri = vreq.getParameter("predicateUri");
         String formParam = vreq.getParameter("editform");
         String command = vreq.getParameter("cmd");         
         
         //check some error conditions
         if (formParam == null || "".equals(formParam)) {
             if ((predicateUri == null || predicateUri.trim().length() == 0)) {
                 return doHelp(vreq, "No form was specified, both predicateUri and"
                                 + " editform are empty. One of these is required"
                                 + " by editRequestDispatch to choose a form.");
             }
             
             if (subjectUri == null || subjectUri.trim().length() == 0){
                 return doHelp(vreq, "subjectUri was empty. If no editForm is specified," +
                 		" it is required by EditRequestDispatch.");                 
             }
         }                  
         
         vreq.setAttribute("subjectUri", subjectUri);
         vreq.setAttribute("subjectUriJson", MiscWebUtils.escape(subjectUri));
         if (predicateUri != null) {
             vreq.setAttribute("predicateUri", predicateUri);
             vreq.setAttribute("predicateUriJson", MiscWebUtils.escape(predicateUri));
         } 
         
         if (formParam != null && formParam.length() > 0) {
             vreq.setAttribute("editForm", formParam);
         } else {
             formParam = null;
         }
                         
         //bdc34: typeOfNew is used by some forms like defaultAddMissingindividuaForm
         //it should be moved out of this code and into the configuration for those forms
         String typeOfNew = vreq.getParameter("typeOfNew");
         if( typeOfNew != null )
             vreq.setAttribute("typeOfNew", typeOfNew);             
         
         vreq.setAttribute("urlPatternToReturnTo", vreq
                 .getParameter("urlPattern") == null ? "/entity" : vreq
                 .getParameter("urlPattern"));
         log.debug("setting urlPatternToReturnTo as "
                 + vreq.getAttribute("urlPatternToReturnTo"));

         /* since we have the URIs lets put the individuals in the request */         
         if( subjectUri != null ){
             Individual subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
             if( subject != null )
                 vreq.setAttribute("subject", subject);             
         }

         String objectUri = vreq.getParameter("objectUri");
         if (objectUri != null) {
             vreq.setAttribute("objectUri", objectUri);
             vreq.setAttribute("objectUriJson", MiscWebUtils.escape(objectUri));            
         }
         
         boolean isEditOfExistingStmt = false;
         if (objectUri != null) {
             Individual object = wdf.getIndividualDao().getIndividualByURI(objectUri);
             if (object != null) {
                 vreq.setAttribute("object", object);
                 isEditOfExistingStmt = true;
             }
         }

        // Keep track of what form we are using so it can be returned to after a failed validation 
        // I'd like to get this from the request but sometimes that doesn't work well, internal forwards etc.
         //TODO: this needs to be the same as the mapping in web.xml 
         vreq.setAttribute("formUrl", "/edit/editRequestDispatch?" + vreq.getQueryString());

         if ("delete".equals(command)) {
             //TODO: delete command is used with the defualt delete form
             //maybe it doesn't need to be in here?
        	 HashMap<String,Object> map = new HashMap<String,Object>();
        	 map.put("errorMessage", "delete command is not yet implemented");
        	 return new TemplateResponseValues("error-message.ftl", map);
         }

         //Certain predicates may be annotated to change the behavior of the edit
         //link.  Check for this annotation and, if present, simply redirect 
         //to the normal individual display for the object URI instead of bringing
         //up an editing form.
         //Note that we do not want this behavior for the delete link (handled above).
         // This might be done in the custom form jsp for publicaitons already.
         // so maybe this logic shouldn't be here?
         if ( isEditOfExistingStmt && (wdf.getObjectPropertyDao().skipEditForm(predicateUri)) ) {
             log.debug("redirecting to object for predicate " + predicateUri);
             //TODO: implement this feature
//             %><c:redirect url="/individual">
//                   <c:param name="uri" value="${param.objectUri}"/>
//                   <c:param name="relatedSubjectUri" value="${param.subjectUri}"/>
//                   <c:param name="relatingPredicateUri" value="${param.predicateUri}"/>
//               </c:redirect>
//             <%
             
             HashMap<String,Object> map = new HashMap<String,Object>();
        	 map.put("errorMessage", "skip edit form for object properties is not yet implemented");
        	 return new TemplateResponseValues("error-message.ftl", map);
         } 

         //use default object property form if nothing else works
         String editConfGeneratorName = DEFAULT_OBJ_FORM;
          
         // *** handle the case where the form is specified as a request parameter ***
         if( predicateUri == null && ( formParam != null && !formParam.isEmpty()) ){
             //form parameter must be a fully qualified java class name of a EditConfigurationVTwoGenerator implementation.
             editConfGeneratorName = formParam;              
         }
         
         // *** handle the case where the form is decided by the predicate parameter ***

         //check to see if we have a predicate and if it has a custom form
         //if it has a custom form associated with it then use that form, 
         //otherwise use the default object property form

         String customForm = null;
         ObjectProperty objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
         if( objectProp != null ){
             vreq.setAttribute("predicate", objectProp);
             //custom entry form use to be a jsp but it should now be a fully qualified java class name of a 
             //EditConfigurationVTwoGenerator implementation.
             customForm = objectProp.getCustomEntryForm();
             if (customForm != null && customForm.length() > 0) {                            
                 //if there is a custom form on the predicate, use that
                 editConfGeneratorName = objectProp.getCustomEntryForm();
             }
         }

         // The default object proepty form offers the option to create a new item
         // instead of selecting from existing individuals in the system.
         // This is where the request to create a new indivdiual is handled.
         //
         // Forward to create new is part of the default object property form
         // it should be handled in that form's EditConfigurationVTwo, not here.
         // The code that sets up the EditConfigurationVTwo should decide on 
         // different configurations and templates to use based on isForwardToCreateNew. 
         //TODO: make sure that forward to create new works on the default object property form
         if( isFowardToCreateNew(vreq, objectProp, command)){
             return handleForwardToCreateNew(vreq, command, objectProp, isEditOfExistingStmt);
         }
                     
         vreq.setAttribute("form", editConfGeneratorName);
         
         /****  make new or get an existing edit configuration ***/         
     
         EditConfigurationVTwo editConfig = makeEditConfigurationVTwo( editConfGeneratorName, vreq, session);
         editConfig.setEditKey(editKey);
         EditConfigurationVTwo.putConfigInSession(editConfig, session);
         
         //what template?
         String template = editConfig.getTemplate();
         String formTitle = (String)vreq.getAttribute("formTitle");
         
         //what goes in the map for templates?
         Map<String,Object> templateData = new HashMap<String,Object>();
         templateData.put("EditConfiguration", new EditConfigurationTemplateModel( editConfig, vreq));
         templateData.put("formTitle", formTitle);
         
         return new TemplateResponseValues(template, templateData);
         }catch(Throwable th){
        	
        	 HashMap<String,Object> map = new HashMap<String,Object>();
        	 map.put("errorMessage", th.toString());
        	 log.error(th,th);
        	 return new TemplateResponseValues("error-message.ftl", map);
        
         }
    }
    
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
        	log.error("could not find EditConfigurationVTwoGenerator " + editConfGeneratorName);
        	return null;
        } else {
            return EditConfigurationVTwoGenerator.getEditConfiguration(vreq, session);
        }
        
    }

    /*
         Forward to create new is part of the default object property form
         it should be handled in that form's EditConfigurationVTwo, not here.
         The code that sets up the EditConfigurationVTwo should decide on 
         different configurations and templates to use based on isForwardToCreateNew.
     */
    boolean isFowardToCreateNew(VitroRequest vreq, ObjectProperty objectProp, String command){       
        //Offer create new and select from existing are ignored if there is a custom form
        if( objectProp != null && objectProp.getCustomEntryForm() != null && !objectProp.getCustomEntryForm().isEmpty()){        
            return false;
        } else {
     
            boolean isForwardToCreateNew = 
                ( objectProp != null && objectProp.getOfferCreateNewOption() && objectProp.getSelectFromExisting() == false)
                || ( objectProp != null && objectProp.getOfferCreateNewOption() && "create".equals(command));

            return isForwardToCreateNew;
        }
    }
    
    ResponseValues handleForwardToCreateNew(VitroRequest vreq, String command, ObjectProperty objectProp, boolean isEditOfExistingStmt){                          
        vreq.setAttribute("isForwardToCreateNew", new Boolean(true));
        
        //If a objectProperty is both provideSelect and offerCreateNewOption
        // and a user goes to a defaultObjectProperty.jsp form then the user is
        // offered the option to create a new Individual and replace the 
        // object in the existing objectPropertyStatement with this new individual. 
        boolean isReplaceWithNew =
            isEditOfExistingStmt && "create".equals(command) 
            && objectProp != null && objectProp.getOfferCreateNewOption() == true;                

        // If an objectProperty is selectFromExisitng==false and offerCreateNewOption == true
        // the we want to forward to the create new form but edit the existing object
        // of the objPropStmt.
        boolean isForwardToCreateButEdit = 
            isEditOfExistingStmt && objectProp != null 
            && objectProp.getOfferCreateNewOption() == true 
            && objectProp.getSelectFromExisting() == false
            && ! "create".equals(command);

        //bdc34: maybe when doing a create new, the custom form should be on the class, not the property?
        String form;
        if( isReplaceWithNew ){
            vreq.setAttribute("isReplaceWithNew", new Boolean(true));
            form = DEFAULT_ADD_INDIVIDUAL;
        }else  if( isForwardToCreateButEdit ){
            vreq.setAttribute("isForwardToCreateButEdit", new Boolean(true));
            form = DEFAULT_ADD_INDIVIDUAL;
        }else {
            form = DEFAULT_ADD_INDIVIDUAL;
        }
        
        //forward to form?
        HashMap<String,Object> map = new HashMap<String,Object>();
   	 map.put("errorMessage", "forweard to create new is not yet implemented");
   	 return new TemplateResponseValues("error-message.ftl", map);
    }        
    
    private ResponseValues doHelp(VitroRequest vreq, String message){
        //output some sort of help message for the developers.
        
    	HashMap<String,Object> map = new HashMap<String,Object>();
   	 map.put("errorMessage", "help is not yet implemented");
   	 return new TemplateResponseValues("error-message.ftl", map);    }
}
