<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Resource" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Literal" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Property" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.StandardModelSelector"%>
<%@ page import="com.hp.hpl.jena.shared.Lock"%>
<%@ page import="com.hp.hpl.jena.ontology.OntModel"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jstl/functions" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages" %>
<% request.setAttribute("requestedActions", new UseMiscellaneousPages()); %>
<vitro:confirmAuthorization />

<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms.datapropStmtDelete");

    String subjectUri   = request.getParameter("subjectUri");
    String predicateUri = request.getParameter("predicateUri");
    String datapropKeyStr  = request.getParameter("datapropKey");
    int dataHash = 0;
    if (datapropKeyStr!=null && datapropKeyStr.trim().length()>0) {
        try {
            dataHash = Integer.parseInt(datapropKeyStr);
        } catch (NumberFormatException ex) {
            throw new JspException("Cannot decode incoming datapropKey String value "+datapropKeyStr+" as an integer hash in datapropStmtDelete.jsp");
        }
    }

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    String editorUri = EditN3Utils.getEditorUri(vreq);        
    wdf = wdf.getUserAwareDaoFactory(editorUri);
    
    DataProperty prop = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
    if( prop == null ) throw new Error("In datapropStmtDelete.jsp, could not find property " + predicateUri);
    request.setAttribute("propertyName",prop.getPublicName());

    Individual subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
    if( subject == null ) throw new Error("could not find subject " + subjectUri);
    request.setAttribute("subjectName",subject.getName());

    String dataValue=null;
    
    Model model = (Model)application.getAttribute("jenaOntModel");
    
    String vitroNsProp  = vreq.getParameter("vitroNsProp");
    
    DataPropertyStatement dps = RdfLiteralHash.getPropertyStmtByHash(subject.getURI(), predicateUri, dataHash, model);
    
    if( log.isDebugEnabled() ){
        log.debug("attempting to delete dataPropertyStatement: subjectURI <" + dps.getIndividualURI() +">");
        log.debug( "predicateURI <" + dps.getDatapropURI() + ">");
        log.debug( "literal \"" + dps.getData() + "\"" );
        log.debug( "lang @" + (dps.getLanguage() == null ? "null" : dps.getLanguage()));
        log.debug( "datatype ^^" + (dps.getDatatypeURI() == null ? "null" : dps.getDatatypeURI() ));       
    }
    if( dps.getIndividualURI() == null || dps.getIndividualURI().trim().length() == 0){
        log.debug("adding missing subjectURI to DataPropertyStatement" );
        dps.setIndividualURI( subjectUri );
    }
    if( dps.getDatapropURI() == null || dps.getDatapropURI().trim().length() == 0){
        log.debug("adding missing datapropUri to DataPropertyStatement");
        dps.setDatapropURI( predicateUri );
    }
    
    if (dps!=null) {
        dataValue = dps.getData().trim();
        
      	//do the delete
        if( request.getParameter("y") != null ) {

            wdf.getDataPropertyStatementDao().deleteDataPropertyStatement(dps);      	                
            %>

			<%-- grab the predicate URI and trim it down to get the Local Name so we can send the user back to the appropriate property --%>
		    <c:set var="predicateUri" value="${param.predicateUri}" />
		    <c:set var="localName" value="${fn:substringAfter(predicateUri, '#')}" />
            <c:url var="redirectUrl" value="../entity">
                <c:param name="uri" value="${param.subjectUri}"/>
            </c:url>
            <c:redirect url="${redirectUrl}${'#'}${localName}"/>

<%      } else { %>
            <jsp:include page="${preForm}"/>
<h1>JSP form, must be removed for the 1.4!</h1>

            <form action="editDatapropStmtRequestDispatch.jsp" method="get">
			    <label for="submit"><h2>Are you sure you want to delete the following entry from <em>${propertyName}</em>?</h2></label>
                <div class="toBeDeleted dataProp"><%=dataValue%></div>
                <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
                <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
                <input type="hidden" name="datapropKey"  value="${param.datapropKey}"/>
                <input type="hidden" name="y"            value="1"/>
                <input type="hidden" name="cmd"          value="delete"/>
                <input type="hidden" name="vitroNsProp"  value="${param.vitroNsProp}" /> 
                <v:input type="submit" id="submit" value="Delete" cancel="true" />
            </form>
            <jsp:include page="${postForm}"/>
<%      }
     } else {
           throw new Error("In datapropStmtDelete.jsp, no match via hashcode to existing data property "+predicateUri+" for subject "+subject.getName()+"\n");
     }%>
