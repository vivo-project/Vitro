<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="java.util.HashMap"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%! private static HashMap<String,String> defaultsForXSDtypes ;
  static {
	defaultsForXSDtypes = new HashMap<String,String>();
	//defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","2001-01-01T12:00:00");
	defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","#Unparseable datetime defaults to now");
  }
%>
<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms.defaultDatapropForm.jsp");
	log.debug("Starting defaultDatapropForm.jsp");
	VitroRequest vreq = new VitroRequest(request);
    String subjectUri   = vreq.getParameter("subjectUri");
    String predicateUri = vreq.getParameter("predicateUri");

    DataPropertyStatement dps = (DataPropertyStatement)vreq.getAttribute("dataprop");
    
    String datapropKeyStr = vreq.getParameter("datapropKey");
    int dataHash=0;

    DataProperty prop = (DataProperty)vreq.getAttribute("predicate");
    if( prop == null ) throw new Error("In defaultDatapropForm.jsp, could not find predicate " + predicateUri);
    vreq.setAttribute("propertyName",prop.getPublicName());

    Individual subject = (Individual)vreq.getAttribute("subject");
    if( subject == null ) throw new Error("In defaultDatapropForm.jsp, could not find subject " + subjectUri);
    vreq.setAttribute("subjectName",subject.getName());
    
    String rangeDatatypeUri = vreq.getWebappDaoFactory().getDataPropertyDao().getRequiredDatatypeURI(subject, prop);
    //String rangeDatatypeUri = prop.getRangeDatatypeURI();
    vreq.setAttribute("rangeDatatypeUriJson", MiscWebUtils.escape(rangeDatatypeUri));
    
    if( dps != null ){
        try {
            dataHash = Integer.parseInt(datapropKeyStr);
            log.debug("dataHash is " + dataHash);            
        } catch (NumberFormatException ex) {
            log.debug("could not parse dataprop hash "+ 
                    "but there was a dataproperty; hash: '"+datapropKeyStr+"'"); 
        }
        
        String rangeDatatype = dps.getDatatypeURI();
        if( rangeDatatype == null ){
            log.debug("no range datatype uri set on data property statement when property's range datatype is "+prop.getRangeDatatypeURI()+" in defaultDatapropForm.jsp");
            vreq.setAttribute("rangeDatatypeUriJson","");
        } else {
            log.debug("range datatype uri of ["+rangeDatatype+"] on data property statement in defaultDatapropForm.jsp");
            vreq.setAttribute("rangeDatatypeUriJson",rangeDatatype);
        }
        String rangeLang = dps.getLanguage();
        if( rangeLang == null ) {
            log.debug("no language attribute on data property statement in defaultDatapropForm.jsp");
            vreq.setAttribute("rangeLangJson","");
        }else{
            log.debug("language attribute of ["+rangeLang+"] on data property statement in defaultDatapropForm.jsp");
            vreq.setAttribute("rangeLangJson", rangeLang);
        }
    } else {
        log.debug("No incoming dataproperty statement attribute for property "+prop.getPublicName()+"; adding a new statement");                
        if(rangeDatatypeUri != null && rangeDatatypeUri.length() > 0) {                        
            String defaultVal = defaultsForXSDtypes.get(rangeDatatypeUri);
            if( defaultVal == null )            	
            	vreq.setAttribute("rangeDefaultJson", "");
            else
            	vreq.setAttribute("rangeDefaultJson", '"' + MiscWebUtils.escape(defaultVal)  + '"' );
        }
    }

%>
<c:set var="localName" value="<%=prop.getLocalName()%>"/>
<c:set var="dataLiteral" value="${localName}Edited"/>

<v:jsonset var="n3ForEdit"  >
    ?subject ?predicate ?${dataLiteral}.
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"              : "${formUrl}",
    "editKey"              : "${editKey}",
    "datapropKey"          : "<%=datapropKeyStr==null?"":datapropKeyStr%>",
    "urlPatternToReturnTo" : "/entity",

    "subject"   : ["subject",   "${subjectUriJson}" ],
    "predicate" : ["predicate", "${predicateUriJson}"],
    "object"    : ["${dataLiteral}","","DATAPROPHASH"],  
    
    "n3required"                : ["${n3ForEdit}"],
    "n3optional"                : [ ],
    "newResources"              : { },
    "urisInScope"               : { },
    "literalsInScope"           : { },
    "urisOnForm"                : [ ],
    "literalsOnForm"            : ["${dataLiteral}"],
    "filesOnForm"               : [ ],
    "sparqlForLiterals"         : { },
    "sparqlForUris"             : { },
    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { },
    "optionsForFields"          : { },
    "fields"                    : { "${dataLiteral}" : {
                                       "newResource"      : "false",
                                       "validators"       : ["datatype:${rangeDatatypeUriJson}"],
                                       "optionsType"      : "LITERALS",
                                       "literalOptions"   : [ ${rangeDefaultJson} ],
                                       "predicateUri"     : "",
                                       "objectClassUri"   : "",
                                       "rangeDatatypeUri" : "${rangeDatatypeUriJson}",
                                       "rangeLang"        : "${rangeLangJson}",
                                       "assertions"       : ["${n3ForEdit}"]
                                     }
                                  }
  }
</c:set>

<%
    if( log.isDebugEnabled()) log.debug(request.getAttribute("editjson"));

    EditConfiguration editConfig = new EditConfiguration((String)vreq.getAttribute("editjson"));
    EditConfiguration.putConfigInSession(editConfig, session);

    String formTitle   =""; // don't add local page variables to the request
    String submitLabel ="";

    if( datapropKeyStr != null && datapropKeyStr.trim().length() > 0  ) {
        Model model =  (Model)application.getAttribute("jenaOntModel");
        editConfig.prepareForDataPropUpdate(model,dps);
        formTitle   = "Change text for: <em>"+prop.getPublicName()+"</em>";
        submitLabel = "Save change";
    } else {
        formTitle   ="Add new entry for: <em>"+prop.getPublicName()+"</em>";
        submitLabel ="Save entry";
    }
%>

<%--the following  parameters configure the tinymce textarea --%>
<jsp:include page="${preForm}">
	<jsp:param name="useTinyMCE" value="true"/>
</jsp:include>

<h1>JSP form, must be removed for the 1.4!</h1>

<h2><%=formTitle%></h2>
<form class="editForm" action="<c:url value="/edit/processDatapropRdfForm.jsp"/>" method="post" > <%-- see VITRO-435 Jira issue: need POST, not GET --%>
	<c:if test="${!empty predicate.publicDescription}">
    	<label for="${dataLiteral}"><p class="propEntryHelpText">${predicate.publicDescription}</p></label>
	</c:if>
    <v:input type="textarea" id="${dataLiteral}" rows="2"/>
    <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="true"/>
</form>

<c:if test="${ (!empty param.datapropKey) && (empty param.deleteProhibited) }">
     <form class="deleteForm" action="editDatapropStmtRequestDispatch.jsp" method="post">                               
	 		<label for="delete"><h3>Delete this entry?</h3></label>
            <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
            <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
            <input type="hidden" name="datapropKey"  value="${param.datapropKey}"/>                
            <input type="hidden" name="cmd"          value="delete"/>
            <v:input type="submit" id="delete" value="Delete" cancel="" />
     </form>
</c:if>
<jsp:include page="${postForm}"/>



