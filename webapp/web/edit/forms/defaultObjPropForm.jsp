<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%!
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.defaultObjPropForm.jsp");
%>
<%
    Individual subject = (Individual)request.getAttribute("subject");
    ObjectProperty prop = (ObjectProperty)request.getAttribute("predicate");

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();  
%>

<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.SelectListGenerator"%>
<%@page import="java.util.Map"%>
<%@page import="com.hp.hpl.jena.ontology.OntModel"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary"%>

<v:jsonset var="queryForInverse" >
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    SELECT ?inverse_property
    WHERE {
        ?inverse_property owl:inverseOf ?predicate
    }
</v:jsonset>

<v:jsonset var="n3ForEdit"  >
    ?subject ?predicate ?objectVar.
</v:jsonset>

<v:jsonset var="n3Inverse" >
    ?objectVar ?inverseProp ?subject.
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"                   : "${formUrl}",
    "editKey"                   : "${editKey}",
    "urlPatternToReturnTo"      : "/entity",

    "subject"      : [ "subject", "${subjectUriJson}" ] ,
    "predicate"    : [ "predicate", "${predicateUriJson}" ],
    "object"       : [ "objectVar" ,  "${objectUriJson}" , "URI"],

    "n3required"                : [ "${n3ForEdit}" ],
    "n3optional"                : [ "${n3Inverse}" ],
    "newResources"              : { },

    "urisInScope"               : { },
    "literalsInScope"           : { },

    "urisOnForm"                : ["objectVar"],
    "literalsOnForm"            : [ ],
    "filesOnForm"               : [ ],

    "sparqlForLiterals"         : { },
    "sparqlForUris"             : {"inverseProp" : "${queryForInverse}" },

    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { },
    "fields"                    : { "objectVar" : {
                                       "newResource"      : "false",
                                       "queryForExisting" : { },
                                       "validators"       : [ "nonempty" ],
                                       "optionsType"      : "INDIVIDUALS_VIA_OBJECT_PROPERTY",
                                       "subjectUri"       : "${subjectUriJson}",
                                       "subjectClassUri"  : "",
                                       "predicateUri"     : "${predicateUriJson}",
                                       "objectClassUri"   : "",
                                       "rangeDatatypeUri" : "",
                                       "rangeLang"        : "",
                                       "literalOptions"   : [ ] ,
                                       "assertions"       : ["${n3ForEdit}", "${n3Inverse}"]
                                     }
                                  }
  }
</c:set>

<%  
    log.debug(request.getAttribute("editjson"));

    /* now put edit configuration Json object into session */
    EditConfiguration editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
    EditConfiguration.putConfigInSession(editConfig, session);
    String formTitle   ="";
    String submitLabel ="";   
    Model model = (Model)application.getAttribute("jenaOntModel");
    if( request.getAttribute("object") != null ){//this block is for an edit of an existing object property statement
        editConfig.prepareForObjPropUpdate( model );
        formTitle   = "Change entry for: <em>"+prop.getDomainPublic()+"</em>";
        submitLabel = "save change";
    } else {
        editConfig.prepareForNonUpdate( model );
        if ( prop.getOfferCreateNewOption() ) {
        	//Try to get the name of the class to select from
       	  	VClass classOfObjectFillers = null;
    
		    if( prop.getRangeVClassURI() == null ) {    	
		    	// If property has no explicit range, try to get classes 
		    	List<VClass> classes = wdf.getVClassDao().getVClassesForProperty(subject.getVClassURI(), prop.getURI());
		    	if( classes == null || classes.size() == 0 || classes.get(0) == null ){	    	
			    	// If property has no explicit range, we will use e.g. owl:Thing.
			    	// Typically an allValuesFrom restriction will come into play later.	    	
			    	classOfObjectFillers = wdf.getVClassDao().getTopConcept();	    	
		    	} else {
		    		if( classes.size() > 1 )
		    			log.debug("Found multiple classes when attempting to get range vclass.");
		    		classOfObjectFillers = classes.get(0);
		    	}
		    }else{
		    	classOfObjectFillers = wdf.getVClassDao().getVClassByURI(prop.getRangeVClassURI());
		    	if( classOfObjectFillers == null )
		    		classOfObjectFillers = wdf.getVClassDao().getTopConcept();
		    }
        	
            log.debug("property set to offer \"create new\" option; custom form: ["+prop.getCustomEntryForm()+"]");
            formTitle   = "Select an existing "+classOfObjectFillers.getName()+" for "+subject.getName();
            submitLabel = "Select existing";
        } else {
            formTitle   = "Add an entry to: <em>"+prop.getDomainPublic()+"</em>";
            submitLabel = "Save entry";
        }
    }
    
    if( prop.getSelectFromExisting() ){
    	// set ProhibitedFromSearch object so picklist doesn't show
        // individuals from classes that should be hidden from list views
        OntModel displayOntModel = 
            (OntModel) pageContext.getServletContext()
                .getAttribute("displayOntModel");
        if (displayOntModel != null) {
            ProhibitedFromSearch pfs = new ProhibitedFromSearch(
                DisplayVocabulary.SEARCH_INDEX_URI, displayOntModel);
            if( editConfig != null )
                editConfig.setProhibitedFromSearch(pfs);
        }
    	Map<String,String> rangeOptions = SelectListGenerator.getOptions(editConfig, "objectVar" , wdf);    	
    	if( rangeOptions != null && rangeOptions.size() > 0 ) {
    		request.setAttribute("rangeOptionsExist", true);
    	    request.setAttribute("rangeOptions.objectVar", rangeOptions);
    	} else { 
    		request.setAttribute("rangeOptionsExist",false);
    	}
    }
%>
<jsp:include page="${preForm}"/>
<h1>JSP form, must be removed for the 1.4!</h1>
<h2><%=formTitle%></h2>

<c:if test="${requestScope.predicate.selectFromExisting == true }">
  <c:if test="${requestScope.rangeOptionsExist == true }">
  	<form class="editForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" >    
	    <c:if test="${!empty predicate.publicDescription}">
	    	<p>${predicate.publicDescription}</p>
	    </c:if>
	    <v:input type="select" id="objectVar" size="80" />
	    <div style="margin-top: .2em">
 	        <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="true"/>
	    </div>    
    </form>
  </c:if>
  <c:set var="offerCancel" value="false"/>
  <c:if test="${requestScope.rangeOptionsExist == false }">
  	<c:set var="offerCancel" value="true"/>
    <p>There are no entries in the system to select from.</p>
  </c:if>
</c:if>

<c:if test="${requestScope.predicate.offerCreateNewOption == true}">
 	<c:if test="${requestScope.rangeOptionsExist == true }">
    	<p style="margin-top: 2.2em">If you don't find the appropriate entry on the selection list above:</p>
  	</c:if>
  	<c:if test="${requestScope.rangeOptionsExist == false }">
  		<p style="margin-top: 5em">Please create a new entry.</p>  		    
  	</c:if>	
	<c:url var="createNewUrl" value="/edit/editRequestDispatch.jsp"/>
	<form class="editForm" action="${createNewUrl}">        
        <input type="hidden" value="${param.subjectUri}" name="subjectUri"/>
        <input type="hidden" value="${param.predicateUri}" name="predicateUri"/>
        <input type="hidden" value="${param.objectUri}" name="objectUri"/>        
		<input type="hidden" value="create" name="cmd"/>        
		<v:input type="typesForCreateNew" id="typeOfNew" />
        <v:input type="submit" id="submit" value="Add a new item of this type" cancel="${offerCancel}"/>
	</form>                            
</c:if>


  

<c:if test="${(requestScope.predicate.offerCreateNewOption == false) && (requestScope.predicate.selectFromExisting == false)}">
 <p>This property is currently configured to prohibit editing. </p>
</c:if>

<c:if test="${ (!empty param.objectUri) && (empty param.deleteProhibited) }" >
    <form class="deleteForm" action="editRequestDispatch.jsp" method="get">       
	 	<label for="delete"><h3 class="delete-entry">Delete this entry?</h3></label>
        <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
        <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
        <input type="hidden" name="objectUri"    value="${param.objectUri}"/>    
        <input type="hidden" name="cmd"          value="delete"/>
        <c:if test="${(requestScope.predicate.offerCreateNewOption == false) && (requestScope.predicate.selectFromExisting == false)}">
            <v:input type="submit" id="delete" value="Delete" cancel="cancel" />           
        </c:if>
        <c:if test="${(requestScope.predicate.offerCreateNewOption == true) || (requestScope.predicate.selectFromExisting == true)}">
            <v:input type="submit" id="delete" value="Delete" cancel="" />
        </c:if>
    </form>
</c:if>

<jsp:include page="${postForm}"/>
