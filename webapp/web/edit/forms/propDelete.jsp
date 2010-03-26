<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RoleIdentifier"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Utils"%>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Link" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.LinksDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>

<%@ page import="java.util.List" %>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jstl/functions" %>

<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.propDelete.jsp");

public WebappDaoFactory getUnfilteredDaoFactory() {
    return (WebappDaoFactory) getServletContext().getAttribute("webappDaoFactory");
}
%>

<%-- grab the predicate URI and trim it down to get the Local Name so we can send the user back to the appropriate property --%>
    <c:set var="predicateUri" value="${param.predicateUri}" />
    <c:set var="localName" value="${fn:substringAfter(predicateUri, '#')}" />
    <c:url var="redirectUrl" value="../entity">
        <c:param name="uri" value="${param.subjectUri}"/>
    </c:url>

<%  
    if( session == null) {
        throw new Error("need to have session");
    }
    boolean selfEditing = VitroRequestPrep.isSelfEditing(request);
    if (!selfEditing && !LoginFormBean.loggedIn(request, LoginFormBean.NON_EDITOR)) {%>
        <c:redirect url="<%= Controllers.LOGIN %>" />       
<%  }

    String subjectUri   = request.getParameter("subjectUri");
    String predicateUri = request.getParameter("predicateUri");
    String objectUri    = request.getParameter("objectUri");

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    if( wdf == null ) {
        throw new Error("could not get a WebappDaoFactory");
    }
    ObjectProperty prop = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
    if( prop == null ) {
        throw new Error("In propDelete.jsp, could not find property " + predicateUri);
    }
    request.setAttribute("propertyName",prop.getDomainPublic().toLowerCase());

    //do the delete
    if( request.getParameter("y") != null ) {
        
        String editorUri = EditN3Utils.getEditorUri(request,session,application);        
         wdf = wdf.getUserAwareDaoFactory(editorUri);
        
        if (prop.getForceStubObjectDeletion()) {
            Individual object = (Individual)request.getAttribute("object");
            if (object==null) {
                object = getUnfilteredDaoFactory().getIndividualDao().getIndividualByURI(objectUri);
            }
            if( object != null ) {
                log.warn("Deleting individual "+object.getName()+" since property has been set to force range object deletion");
                wdf.getIndividualDao().deleteIndividual(object);
            } else {
                throw new Error("Could not find object as request attribute or in model: '" + objectUri + "'");
            }
        }
        wdf.getPropertyInstanceDao().deleteObjectPropertyStatement(subjectUri,predicateUri,objectUri); %>
        <c:redirect url="${redirectUrl}${'#'}${localName}"/>
<%  }
    
    Individual subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
    if( subject == null ) throw new Error("could not find subject " + subjectUri);
    request.setAttribute("subjectName",subject.getName());

    boolean foundClass = false;
    String customShortView = null;
    String shortViewPrefix = "/templates/entity/";
    Individual object = getUnfilteredDaoFactory().getIndividualDao().getIndividualByURI(objectUri);
    
    if( object == null ) {
        //log.warn("Could not find object individual "+objectUri+" via wdf.getIndividualDao().getIndividualByURI(objectUri)");
        request.setAttribute("objectName","(name unspecified)");
      } else if (FrontEndEditingUtils.isVitroNsObjProp(predicateUri)) {
          Model model = (Model)application.getAttribute("jenaOntModel");
          request.setAttribute("individual", object);
          request.setAttribute("objectName", FrontEndEditingUtils.getVitroNsObjDisplayName(predicateUri, object, model));
          log.debug("setting object name " + (String)request.getAttribute("objectName") + " for vitro namespace object property " + predicateUri);
    } else {
        for (VClass clas : object.getVClasses(true)) { // direct VClasses, not inferred, and not including Vitro namespace
            request.setAttribute("rangeClassName", clas.getName());
            foundClass = true;
            customShortView = clas.getCustomShortView();
    	    if (customShortView != null && customShortView.trim().length()>0) {
    	        log.debug("setting object name from VClass custom short view");
    	        request.setAttribute("customShortView",shortViewPrefix + customShortView.trim());
    	        request.setAttribute("individual",object);
            } else {
                log.debug("No custom short view for class, so setting object name from object individual name");
                request.setAttribute("objectName",object.getName());
            }
        }
        if (!foundClass) {
            VClass clas = prop.getRangeVClass();
	        if (clas != null) {
	            customShortView = clas.getCustomShortView();
	            if (customShortView != null && customShortView.trim().length()>0) {
	                log.warn("setting object name from VClass custom short view \""+customShortView.trim()+"\"");
	                request.setAttribute("customShortView",shortViewPrefix + customShortView.trim());
	                request.setAttribute("individual",object);
	            } else {
	                log.error("No custom short view jsp set for VClass "+clas.getName()+" so cannot render link name correctly");
	                request.setAttribute("objectName",object.getName());
	            }
            }        
        }
    }%>

<jsp:include page="${preForm}"/>

<form action="editRequestDispatch.jsp" method="get">
    <label for="submit"><h2>Are you sure you want to delete the following entry for <em>${propertyName}</em>?</h2></label>
    <div class="toBeDeleted objProp">
    	<c:choose>
    		<c:when test="${!empty customShortView}">
    			<c:set scope="request" var="individual" value="${individual}"/>
    			<jsp:include page="${customShortView}" flush="true"/>
                <c:remove var="customShortView"/>
    		</c:when>
    		<c:otherwise>${objectName}</c:otherwise>
    	</c:choose>
	</div>
    <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
    <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
    <input type="hidden" name="objectUri"    value="${param.objectUri}"/>
    <input type="hidden" name="y"            value="1"/>
    <input type="hidden" name="cmd"          value="delete"/>
    <p class="submit"><v:input type="submit" id="submit" value="Delete" cancel="${param.subjectUri}" /></p>
	
</form>

<jsp:include page="${postForm}"/>
