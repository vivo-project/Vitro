/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;

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
public class EditRequestDispatch extends FreemarkerHttpServlet {
    private static final long serialVersionUID = 1L;
    public static Log log = LogFactory.getLog(EditRequestDispatch.class);
    
    final String DEFAULT_OBJ_FORM = "defaultObjPropForm.jsp";
    final String DEFAULT_ERROR_FORM = "error.jsp";
    final String DEFAULT_ADD_INDIVIDUAL = "defaultAddMissingIndividualForm.jsp";
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {      
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        //get edit key.  
        //The edit key links submissions to EditConfiguration objects in the session.
        HttpSession session = vreq.getSession();        
        String editKey = 
            (EditConfiguration.getEditKey(vreq) == null) 
                ? EditConfiguration.newEditKey(session)
                : EditConfiguration.getEditKey(vreq);
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
         String url = "/edit/editRequestDispatch.jsp"; 
         vreq.setAttribute("formUrl", url + "?" + vreq.getQueryString());

         //this are only used by the old jsp forms
         vreq.setAttribute("preForm", "/edit/formPrefix.jsp");
         vreq.setAttribute("postForm", "/edit/formSuffix.jsp");

         if ("delete".equals(command)) {
            // %><jsp:forward page="/edit/forms/propDelete.jsp"/><%
             return null;
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
//             %><c:redirect url="/individual">
//                   <c:param name="uri" value="${param.objectUri}"/>
//                   <c:param name="relatedSubjectUri" value="${param.subjectUri}"/>
//                   <c:param name="relatingPredicateUri" value="${param.predicateUri}"/>
//               </c:redirect>
//             <%
             return null;
         }
                   

         String form = DEFAULT_OBJ_FORM;
          
         // *** handle the case where the form is specified as a request parameter ***
         if( predicateUri == null && ( formParam != null && !formParam.isEmpty()) ){
             //case where a form was passed as a http parameter
             form = formParam;              
             vreq.setAttribute("form", form);              
             //followed by <jsp:foward page="/edit/forms/${form}"/>              
             return null;
         }

         
         // *** handle the case where the form is decided by the predicate parameter ***

         //check to see if we have a predicate and if it has a custom form
         //if it has a custom form associated with it then use that form, 
         //otherwise use the default object property form

         String customForm = null;
         ObjectProperty objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
         if( objectProp != null ){
             vreq.setAttribute("predicate", objectProp);
             customForm = objectProp.getCustomEntryForm();
         }

         // Forward to create new is part of the default object property form
         // it should be handled in that form's EditConfiguration, not here.
         // The code that sets up the EditConfiguration should decide on 
         // different configurations and templates to use based on isForwardToCreateNew.         
         if( isFowardToCreateNew(vreq, objectProp, command)){
             return handleForwardToCreateNew(vreq, command, objectProp, isEditOfExistingStmt);
         }
                  
         //Offer create new and select from existing are ignored if there is a custom form
         if (customForm != null && customForm.length() > 0) {                            
             //if there is a custom form on the predicate, use that
             form = objectProp.getCustomEntryForm();                                   
         } else {
             //if it is nothing special, then use the default object property form              
             form = DEFAULT_OBJ_FORM ;                  
         }          
         vreq.setAttribute("form", form);
         
         // Now here we can no longer forward to a JSP.
         // Somehow we need to be able to specify some java code that generates the          
         // EditConfiguration and the do the freemarker template merge.
                          
         return null;
    }
    
    /*
         Forward to create new is part of the default object property form
         it should be handled in that form's EditConfiguration, not here.
         The code that sets up the EditConfiguration should decide on 
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
        return null;
    }        
    
    private ResponseValues doHelp(VitroRequest vreq, String message){
        //output some sort of help message for the developers.
        
        return null;
    }
}
