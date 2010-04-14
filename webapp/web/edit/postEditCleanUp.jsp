<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="com.hp.hpl.jena.rdf.model.ResourceFactory"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jstl/functions" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.1" prefix="str" %>


<%
    /* Clear any cruft from session. */
    String resourceToRedirectTo = null;	
    String urlPattern = null;
    String predicateLocalName = null;
    String predicateAnchor = "";
    if( session != null ) {
        EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
        //In order to support back button resubmissions, don't remove the editConfig from session.
        //EditConfiguration.clearEditConfigurationInSession(session, editConfig);
        
        EditSubmission editSub = EditSubmission.getEditSubmissionFromSession(session,editConfig);        
        EditSubmission.clearEditSubmissionInSession(session, editSub);
        
        if( editConfig != null ){
            String predicateUri = editConfig.getPredicateUri();            
            if( predicateUri != null ){
            	Property prop = ResourceFactory.createProperty(predicateUri);
            	predicateLocalName = prop.getLocalName();            	
            }                        
                        
            if( editConfig.getEntityToReturnTo() != null && editConfig.getEntityToReturnTo().startsWith("?") ){
            	resourceToRedirectTo = (String)request.getAttribute("entityToReturnTo");
            }else{            
            	resourceToRedirectTo = editConfig.getEntityToReturnTo();
            }              
        }
        
        //set up base URL
        if( editConfig == null || editConfig.getUrlPatternToReturnTo() == null){
        	urlPattern = "/individual";            
        }else{
        	urlPattern = editConfig.getUrlPatternToReturnTo();        	
        }
        
        //looks like a redirec to an profile page, try to add anchor for property that was just edited.
        if( urlPattern.endsWith("individual") || urlPattern.endsWith("entity") ){        	
       		if( predicateLocalName != null && predicateLocalName.length() > 0){
       			predicateAnchor = "#" + predicateLocalName;
       			request.setAttribute("predicateAnchor", predicateAnchor);
       		}
        }
    }
    
    /* The parameter extra=true is just for ie6. */
    if( resourceToRedirectTo != null ){    	
    	  if( urlPattern != null && ( urlPattern.endsWith("entity") || urlPattern.endsWith("individual") )){  %>
		      <%-- Here we're building the redirect URL to include an (unencoded) fragment identifier such as: #propertyName  --%>               
		      <c:url context="/" var="encodedUrl" value="<%=urlPattern%>">
			     <c:param name="uri" value="<%=resourceToRedirectTo%>" />
			     <c:param name="extra" value="true"/> 
              </c:url>
		      <c:redirect url="${encodedUrl}${predicateAnchor}" />
       <% } else { %>
              <c:url context="/" var="encodedUrl" value="<%=urlPattern%>">              
                 <c:param name="uri" value="<%=resourceToRedirectTo%>" />
                 <c:param name="extra" value="true"/> 
              </c:url>
              <c:redirect url="${encodedUrl}${predicateAnchor}" />                    
		<%} %>
    <% } else { %>
        <c:redirect url="<%= Controllers.LOGIN %>" />
    <% } %>

<%!
Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.edit.postEditCleanUp.jsp");
%>



