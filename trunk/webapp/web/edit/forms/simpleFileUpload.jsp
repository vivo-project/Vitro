<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%
/* Prompts the user for a file to upload and adds a statement between a newly created 
   file resource and the Subject using the incoming predicate.  Also try to make
   an inverse statement. */
   
    Individual subject = (Individual)request.getAttribute("subject");
    ObjectProperty prop = (ObjectProperty)request.getAttribute("predicate");

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();    
%>

<v:jsonset var="queryForInverse" >
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    SELECT ?inverse_property
    WHERE {
        ?inverse_property owl:inverseOf ?predicate
    }
</v:jsonset>

<v:jsonset var="n3ForEdit"  >
    ?subject ?predicate ?fileResource .
</v:jsonset>

<v:jsonset var="n3Inverse" >
    ?fileResource  ?inverseProp ?subject.
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"                   : "${formUrl}",
    "editKey"                   : "${editKey}",
    "urlPatternToReturnTo"      : "/entity",

    "subject"      : [ "subject", "${subjectUriJson}" ] ,
    "predicate"    : [ "predicate", "${predicateUriJson}" ],
    "object"       : [ "fileResource" ,  "${objectUriJson}" , "URI"],

    "n3required"                : [ "${n3ForEdit}" ],
    "n3optional"                : [ "${n3Inverse}" ],
    "newResources"              : { },

    "urisInScope"               : { },
    "literalsInScope"           : { },

    "urisOnForm"                : [ ],
    "literalsOnForm"            : [ ],
    "filesOnForm"               : [ "fileResource" ],

    "sparqlForLiterals"         : { },
    "sparqlForUris"             : {"inverseProp" : "${queryForInverse}" },

    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { },
    "fields"                    : { "fileResource" : {
                                       "newResource"      : "false",
                                       "queryForExisting" : { },                                       
                                       "validators"       : [ "nonempty" ],
                                       "optionsType"      : "FILE_UPLOAD",
                                       "subjectUri"       : "",
                                       "subjectClassUri"  : "",
                                       "predicateUri"     : "",
                                       "objectClassUri"   : "${fileResourceClass}",
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

    Model model = (Model)application.getAttribute("jenaOntModel");
    if( request.getAttribute("object") != null ){//this block is for an edit of an existing object property statement
        editConfig.prepareForObjPropUpdate( model );
    } else {
        editConfig.prepareForNonUpdate( model );    
    }
%>
<jsp:include page="${preForm}"/>

<h2>Upload a File</h2>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" ENCTYPE="multipart/form-data" method="POST">
    File <v:input type="file" id="fileResource" />
    <v:input type="submit" id="submit" value="submit" cancel="true"/>    
</form>

<jsp:include page="${postForm}"/>
