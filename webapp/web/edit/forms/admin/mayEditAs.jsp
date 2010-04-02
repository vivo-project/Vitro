<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.ModelSelector" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel"%>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%!
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.admin.mayEditAs.jsp");
    public static String RANGE_CLASS = "http://xmlns.com/foaf/0.1/Agent";    
    public static String PREDICATE = VitroVocabulary.MAY_EDIT_AS;
%>
<%
    String subjectUri = (String)request.getAttribute("subjectUri");    

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    
    VClass rangeClass = wdf.getVClassDao().getVClassByURI( RANGE_CLASS );
    if( rangeClass == null ) log.debug("Cannot find class for range for property."
            + " Looking for " + RANGE_CLASS);
    request.setAttribute("rangeClassUriJson", MiscWebUtils.escape(RANGE_CLASS));
    
    request.setAttribute("predicateUriJson", MiscWebUtils.escape(PREDICATE));
    
    request.setAttribute("objectUriJson" , MiscWebUtils.escape((String)request.getAttribute("objectUri")));    
%>

<v:jsonset var="n3ForEdit"  >
    ?subject ?predicate ?objectVar.
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"                   : "${formUrl}",
    "editKey"                   : "${editKey}",
    "urlPatternToReturnTo"      : "/userEdit",

    "subject"      : [ "subject", "${subjectUriJson}" ] ,
    "predicate"    : [ "predicate", "${predicateUriJson}" ],
    "object"       : [ "objectVar" ,  "${objectUriJson}" , "URI"],

    "n3required"                : [ "${n3ForEdit}" ],
    "n3optional"                : [ ],
    "newResources"              : { },

    "urisInScope"               : { },
    "literalsInScope"           : { },

    "urisOnForm"                : ["objectVar"],
    "literalsOnForm"            : [ ],
    "filesOnForm"               : [ ],

    "sparqlForLiterals"         : { },
    "sparqlForUris"             : { },

    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { },
    "fields"                    : { "objectVar" : {
                                       "newResource"      : "false",
                                       "queryForExisting" : { },
                                       "validators"       : [ ],
                                       "optionsType"      : "INDIVIDUALS_VIA_VCLASS",
                                       "subjectUri"       : "${subjectUriJson}",
                                       "subjectClassUri"  : "",
                                       "predicateUri"     : "",
                                       "objectClassUri"   : "${rangeClassUriJson}",
                                       "rangeDatatypeUri" : "",
                                       "rangeLang"        : "",
                                       "literalOptions"   : [ ] ,
                                       "assertions"       : ["${n3ForEdit}"]
                                     }
                                  }
  }
</c:set>

<%  /* now put edit configuration Json object into session */
    EditConfiguration editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
    EditConfiguration.putConfigInSession(editConfig, session);
    String formTitle   ="";
    String submitLabel ="";
    Model model = (Model)application.getAttribute("jenaOntModel");
    editConfig.setWriteModelSelector( new ModelSelector(){
    	public OntModel getModel(HttpServletRequest request, ServletContext context){
    		return (OntModel)context.getAttribute("userAccountsOntModel");
    	}
    });
    if( request.getAttribute("object") != null ){//this block is for an edit of an existing object property statement
        editConfig.prepareForObjPropUpdate( model );
        formTitle   = "Change person that user may edit as";
        submitLabel = "save change";
    } else {
        editConfig.prepareForNonUpdate( model );     
        formTitle   = "Add person that user may edit as";
        submitLabel = "add edit right";        
    }
%>
<jsp:include page="${preForm}"/>

<h2><%=formTitle%></h2>
<form class="editForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" method="post">      
    <v:input type="select" id="objectVar" size="80" />
    <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="${param.subjectUri}"/>    
</form>

<c:if test="${!empty param.objectUri}" >
    <form class="deleteForm" action="<c:url value="/edit/n3Delete.jsp"/>" method="post">       
        <label for="delete"><h3>Remove the right to edit as this person?</h3></label>
        <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
        <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
        <input type="hidden" name="objectVar"    value="${param.objectUri}"/>    
        <input type="hidden" name="editform"    value="edit/admin/mayEditAs.jsp"/>
        <v:input type="submit" id="delete" value="Remove" cancel="" />
    </form>
</c:if>

<jsp:include page="${postForm}"/>
