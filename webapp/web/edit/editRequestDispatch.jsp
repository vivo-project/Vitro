<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>

<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.editRequestDispatch.jsp");
%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages" %>
<% request.setAttribute("requestedActions", new UseMiscellaneousPages()); %>
<vitro:confirmAuthorization />

<%
    /*
    Decide which form to forward to, set subjectUri, subjectUriJson, predicateUri, and predicateUriJson in request.
    Also get the Individual for the subjectUri and put it in the request scope.

    If objectUri is set as a http parameter, then set objectUri and objectUriJson in request, also get the
    Individual for the objectUri and put it in the request.

    /* *************************************
    Parameters:
        subjectUri
        predicateUri
        objectUri (optional)
        cmd (optional)
        typeOfNew (optional)
     ************************************** */

    final String DEFAULT_OBJ_FORM = "defaultObjPropForm.jsp";
    final String DEFAULT_ERROR_FORM = "error.jsp";
    final String DEFAULT_ADD_INDIVIDUAL = "defaultAddMissingIndividualForm.jsp";

   String editKey = (EditConfiguration.getEditKey(request) == null) 
       ? EditConfiguration.newEditKey(session)
       : EditConfiguration.getEditKey(request);
   request.setAttribute("editKey", editKey);
   
   //set title to Edit to maintain functionality from 1.1.1 and avoid updates to Selenium tests
   request.setAttribute("title","Edit");
  
   // set the referrer URL, if available
   setEditReferer(editKey, request.getHeader("Referer"), request.getSession()); 

   /* Figure out what type of edit is being requested,
      setup for that type of edit OR forward to some
      thing that can do the setup  */

   String subjectUri = request.getParameter("subjectUri");
   String predicateUri = request.getParameter("predicateUri");
   String formParam = request.getParameter("editform");
   String command = request.getParameter("cmd");
   String typeOfNew = request.getParameter("typeOfNew");
  
   //If there is no specified editForm then the subjectURI and the predicate
   //are needed to determine which form to use for this edit. 
   if (formParam == null || "".equals(formParam)) {
       if (subjectUri == null || subjectUri.trim().length() == 0)
           throw new Error(
                   "subjectUri was empty, it is required by editRequestDispatch");
       if ((predicateUri == null || predicateUri.trim().length() == 0)
               && (formParam == null || formParam.trim().length() == 0)) {
           throw new Error(
                   "No form was specified, since both predicateUri and"
                           + " editform are empty, One of these is required"
                           + " by editRequestDispatch to choose a form.");
       }
   }else{
       log.debug("Found editform in http parameters.");
   }
   request.setAttribute("subjectUri", subjectUri);
   request.setAttribute("subjectUriJson", MiscWebUtils.escape(subjectUri));
   if (predicateUri != null) {
       request.setAttribute("predicateUri", predicateUri);
       request.setAttribute("predicateUriJson", MiscWebUtils.escape(predicateUri));
   } 
   
   if (formParam != null && formParam.length() > 0) {
       request.setAttribute("editForm", formParam);
   } else {
       formParam = null;
   }
   String objectUri = request.getParameter("objectUri");
   if (objectUri != null) {
       request.setAttribute("objectUri", objectUri);
       request.setAttribute("objectUriJson", MiscWebUtils.escape(objectUri));            
   }
   if( typeOfNew != null )
	   request.setAttribute("typeOfNew", typeOfNew);	   	     
   
   request.setAttribute("urlPatternToReturnTo", request
           .getParameter("urlPattern") == null ? "/entity" : request
           .getParameter("urlPattern"));
   log.debug("setting urlPatternToReturnTo as "
           + request.getAttribute("urlPatternToReturnTo"));

   /* since we have the URIs lets put the individuals in the request */
   /* get some data to make the form more useful */
   VitroRequest vreq = new VitroRequest(request);
   WebappDaoFactory wdf = vreq.getWebappDaoFactory();

   if( subjectUri != null ){
       Individual subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
       if( subject != null ){
           request.setAttribute("subject", subject);
       }
   }

   boolean isEditOfExistingStmt = false;
   if (objectUri != null) {
       Individual object = wdf.getIndividualDao().getIndividualByURI(
               objectUri);
       if (object != null) {
           request.setAttribute("object", object);
           isEditOfExistingStmt = true;
       }
   }

   /* keep track of what form we are using so it can be returned to after a failed validation */
   String url = "/edit/editRequestDispatch.jsp"; //I'd like to get this from the request but...
   request.setAttribute("formUrl", url + "?"
           + request.getQueryString());

   request.setAttribute("preForm", "/edit/formPrefix.jsp");
   request.setAttribute("postForm", "/edit/formSuffix.jsp");

   if ("delete".equals(command)) {
       %><jsp:forward page="/edit/forms/propDelete.jsp"/><%
       return;
   }

   //Certain predicates may be annotated to change the behavior of the edit
   //link.  Check for this annnotation and, if present, simply redirect 
   //to the normal individual display for the object URI instead of bringing
   //up an editing form.
   //Note that we do not want this behavior for the delete link (handled above).
   if ( (predicateUri != null) && (objectUri != null) && (wdf.getObjectPropertyDao().skipEditForm(predicateUri)) ) {
       log.debug("redirecting for predicate " + predicateUri);
       %><c:redirect url="/individual">
             <c:param name="uri" value="${param.objectUri}"/>
             <c:param name="relatedSubjectUri" value="${param.subjectUri}"/>
             <c:param name="relatingPredicateUri" value="${param.predicateUri}"/>
         </c:redirect>
       <%
       return;
   }
   
    if (session.getAttribute("requestedFromEntity") == null)
        session.setAttribute("requestedFromEntity", subjectUri);

    ObjectProperty objectProp = null;
    String customForm = null;
    String form = DEFAULT_OBJ_FORM;
    
    if( predicateUri != null && ( formParam == null || formParam.length() == 0 ) ){       
        objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
        customForm = objectProp.getCustomEntryForm();
        request.setAttribute("predicate", objectProp);  
        //Offer create new and select from existing are ignored if there is a custom form
        if (customForm != null && customForm.length() > 0) {
        	//bdc34: maybe this should be the custom form on the class, not the property.
                form = objectProp.getCustomEntryForm();
        }
        else {
	        boolean isForwardToCreateNew = 
	            ( objectProp != null && objectProp.getOfferCreateNewOption() && objectProp.getSelectFromExisting() == false)
	         || ( objectProp != null && objectProp.getOfferCreateNewOption() && "create".equals(command));   
	        if (isForwardToCreateNew) {
	        	
	        	request.setAttribute("isForwardToCreateNew", new Boolean(true));
	            
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
	                
	                if( isReplaceWithNew ){
	                    request.setAttribute("isReplaceWithNew", new Boolean(true));
	                    form = DEFAULT_ADD_INDIVIDUAL;
	                }else  if( isForwardToCreateButEdit ){
	                    request.setAttribute("isForwardToCreateButEdit", new Boolean(true));
	                    form = DEFAULT_ADD_INDIVIDUAL;
	                }else {
	                    form = DEFAULT_ADD_INDIVIDUAL;
	                }
	            
	        }
	        
	        if( ! isForwardToCreateNew ){
	            if( objectProp != null && objectProp.getCustomEntryForm() != null && objectProp.getCustomEntryForm().length() > 0){
	                form = objectProp.getCustomEntryForm();
	            }else{
	                form = DEFAULT_OBJ_FORM ;
	            }
	        }
        }
        
    } else {
        //case where a form was passed as a http parameter
        form = formParam;
    }
    request.setAttribute("form", form);
%>
<jsp:forward page="/edit/forms/${form}" />


<%!

    //bdc34: This isn't used anywhere, don't migrate forward to java code in 1.3
    private static synchronized void setEditReferer(String editKey, String refererUrl, HttpSession session) {
	     if (refererUrl != null) { 
	    	 Object editRefererObj = session.getAttribute("editRefererMap");
	    	 HashMap<String,String> editRefererMap = 
	    		 (editRefererObj != null && (editRefererObj instanceof HashMap)) 
	    		 ? (HashMap<String,String>) editRefererObj 
	    	     : new HashMap<String,String>();
	         session.setAttribute("editRefererMap", editRefererMap);
	    	 editRefererMap.put(editKey, refererUrl);	 
	     }
    }

%>
