<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.admin.resourceDisplayVisibility.jsp");
%>
<%	/* Here is the calling form, as implemented for props_edit.jsp */
//  <form action="edit/editRequestDispatch.jsp" method="get"> */
//	    <input name="home" type="hidden" value="${portalBean.portalId}" />
//	    <input name="subjectUri" type= "hidden" value="${property.URI}" />
//	    <input name="urlPattern" type="hidden" value="/propertyEdit" />
//      <input name="editform" type="hidden" value="admin/resourceDisplayVisibility.jsp"/>
//      <input type="submit" class="form-button" value="Set Property Display Visibility"/>
//	</form>
//	<form action="edit/editRequestDispatch.jsp" method="get">
//	    <input name="home" type="hidden" value="${portalBean.portalId}" />
//	    <input name="subjectUri" type="hidden" value="${property.URI}" />
//	    <input name="urlPattern" type="hidden" value="/propertyEdit" />
//	    <input name="editform" type="hidden" value="admin/resourceEditVisibility.jsp"/>
//	    <input type="submit" class="form-button" value="Set Property Editing Visibility"/>
//	</form>

/* This form uses the unusual tactic of always configuring itself as if it were doing an update. */
    Individual subject = (Individual)request.getAttribute("subject");

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    
    String urlPatternToReturnTo = (String)request.getAttribute("urlPatternToReturnTo");
    if (urlPatternToReturnTo==null || urlPatternToReturnTo.length()==0) {
        log.warn("urlPatternToReturnTo is null when starting resourceDisplayVisibility.jsp");
        System.out.println("urlPatternToReturnTo is null when starting resourceDisplayVisibility.jsp");
    } else {
        log.warn("urlPatternToReturnTo is "+urlPatternToReturnTo+" when starting resourceDisplayVisibility.jsp");
        System.out.println("urlPatternToReturnTo is "+urlPatternToReturnTo+" when starting resourceDisplayVisibility.jsp");
    }
%>
<c:choose>
	<c:when test="${!empty urlPatternToReturnTo}"><c:set var="urlPattern" value="${urlPatternToReturnTo}"/></c:when>
	<c:otherwise><c:set var="urlPattern" value="/entity" /></c:otherwise>
</c:choose>
<c:set var="roleOptions"> 
    ["<%=BaseResourceBean.RoleLevel.PUBLIC.getURI()%>","<%=BaseResourceBean.RoleLevel.PUBLIC.getLabel() %>" ],
    ["<%=BaseResourceBean.RoleLevel.SELF.getURI()%>","<%=BaseResourceBean.RoleLevel.SELF.getLabel() %>" ],
    ["<%=BaseResourceBean.RoleLevel.EDITOR.getURI()%>","<%=BaseResourceBean.RoleLevel.EDITOR.getLabel() %>" ],
    ["<%=BaseResourceBean.RoleLevel.CURATOR.getURI()%>","<%=BaseResourceBean.RoleLevel.CURATOR.getLabel() %>" ],
    ["<%=BaseResourceBean.RoleLevel.DB_ADMIN.getURI()%>","<%=BaseResourceBean.RoleLevel.DB_ADMIN.getLabel() %>" ],
    ["<%=BaseResourceBean.RoleLevel.NOBODY.getURI()%>","<%=BaseResourceBean.RoleLevel.NOBODY.getLabel() %>" ]
</c:set>
<v:jsonset var="displayRoleAssertions">
	@prefix vitro: <http://vitro.mannlib.connell.edu/ns/vitro/0.7#> .
	?subject vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayRole .
</v:jsonset>
<v:jsonset var="displayRoleExisting">
	prefix vitro: <http://vitro.mannlib.connell.edu/ns/vitro/0.7#>
	SELECT ?existingDisplayRole
	WHERE {
	    ?subject vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayRole .
	}
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"              : "${formUrl}",
    "editKey"              : "${editKey}",
    "urlPatternToReturnTo" : "${urlPattern}",

    "subject"      : [ "subject", "${subjectUriJson}" ] ,
    "predicate"    : [ "predicate", "${predicateUriJson}" ],
    "object"       : [ "objectVar" ,  "http://example.org/valueJustToMakeItLookLikeAnUpdate" , "URI"],

    "n3required"                : [ "" ],
    "n3optional"                : [ "" ],
    "newResources"              : { },

    "urisInScope"               : { "displayRole" : "http://vitro.mannlib.cornell.edu/ns/vitro/role#public" },
    "literalsInScope"           : { },

    "urisOnForm"                : ["displayRole"],
    "literalsOnForm"            : [ ],
    "filesOnForm"               : [ ],

    "sparqlForLiterals"         : { },
    "sparqlForUris"             : { },

    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { "displayRole" : "${displayRoleExisting}" },
    "fields"                    : { "displayRole" : {
                                       "newResource"      : "false",                                       
                                       "validators"       : [ ],
                                       "optionsType"      : "LITERALS",
                                       "predicateUri"     : "${predicateUriJson}",
                                       "objectClassUri"   : "",
                                       "rangeDatatypeUri" : "",
                                       "rangeLang"        : "",
                                       "literalOptions"   : [ ${roleOptions} ],
                                       "assertions"       : ["${displayRoleAssertions}"]
                                     }
                                  }
  }
</c:set>

<%  /* now put edit configuration Json object into session */
    EditConfiguration editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
    if (editConfig.getUrlPatternToReturnTo()==null) {
        System.out.println("urlPatternToReturnTo not initialized in resourceDisplayVisibility.jsp");
        log.debug("urlPatternToReturnTo not initialized");
    } else {
        System.out.println("urlPatternToReturnTo initialized to "+editConfig.getUrlPatternToReturnTo()+" in resourceDisplayVisibility.jsp");
        log.debug("urlPatternToReturnTo initialized to "+editConfig.getUrlPatternToReturnTo()+"\n");       
    }
    EditConfiguration.putConfigInSession(editConfig, session);
    Model model = (Model)application.getAttribute("jenaOntModel");
    editConfig.prepareForObjPropUpdate( model );
%>
<jsp:include page="${preForm}"/>

<h2>Set Display Visibility</h2>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <v:input type="select" id="displayRole" size="80" />
    <v:input type="submit" id="submit" value="submit" cancel="${param.subjectUri}"/>
</form>

<jsp:include page="${postForm}"/>
