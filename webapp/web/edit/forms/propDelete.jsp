<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RoleIdentifier"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerConfigurationLoader"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerConfiguration" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper"%>

<%@page import="freemarker.template.Configuration"%>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jstl/functions" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages" %>
<% request.setAttribute("requestedActions", new UseMiscellaneousPages()); %>
<vitro:confirmAuthorization />

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
        
        String editorUri = EditN3Utils.getEditorUri(request);        
         wdf = wdf.getUserAwareDaoFactory(editorUri);
        
        if (prop.getStubObjectRelation()) {
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
    
    // Get the statement data to be displayed
    String templateName = request.getParameter("templateName");
    Map params = request.getParameterMap();
    Map<String, String> statement = new HashMap<String, String>();
    for (Object key : params.keySet()) {
        String keyString = (String) key; //key.toString()
        if (keyString.startsWith("statement_")) {
            keyString = keyString.replaceFirst("statement_", "");
            String value = ( (String[]) params.get(key))[0];
            statement.put(keyString, value);
        }
    }
    
    // Process the statement data through the template to create the display string
    String statementDisplay = null;
    if (! statement.isEmpty()) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("statement", statement);
 
        /* Some propStatements (propStatement-educationalTraining.ftl) make reference to the individual,  
         * but instead of adding it to the data model we'll test in the template for non-existence. If
         * this becomes more common, add it here instead. We don't want to generate the property
         * list for the individual, though, so should modify IndividualTemplateModel to make that
         * conditional, or perhaps use a different template model that contains only the limited
         * information needed in the propStatement templates.
         */
        //map.put("individual", new IndividualTemplateModel(subject, vreq));

        //map.putAll(FreemarkerHttpServlet.getDirectives());
        //map.putAll(FreemarkerHttpServlet.getMethods());
        ServletContext context = getServletContext();
        FreemarkerConfiguration fmConfig = FreemarkerConfigurationLoader.getConfig(vreq, context);
        fmConfig.resetRequestSpecificSharedVariables();
        TemplateProcessingHelper helper = new TemplateProcessingHelper(fmConfig, vreq, context);
        statementDisplay =  helper.processTemplateToString(templateName, map);       
    }
    request.setAttribute("statementDisplay", statementDisplay);
%>

<jsp:include page="${preForm}"/>

<h1>JSP form, must be removed for the 1.4!</h1>

<form action="editRequestDispatch.jsp" method="get">
    <label for="submit"><h2>Are you sure you want to delete the following entry from <em>${propertyName}</em>?</h2></label>
    <div class="toBeDeleted objProp">${statementDisplay}</div>
    <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
    <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
    <input type="hidden" name="objectUri"    value="${param.objectUri}"/>
    <input type="hidden" name="y"            value="1"/>
    <input type="hidden" name="cmd"          value="delete"/>
    <p class="submit"><v:input type="submit" id="submit" value="Delete" cancel="true" /></p>
	
</form>

<jsp:include page="${postForm}"/>
