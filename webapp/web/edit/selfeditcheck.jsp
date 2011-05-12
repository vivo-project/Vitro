<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.util.*" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.NetId"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers"%>
<%@page import="java.io.IOException"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.policy.SelfEditingPolicy"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>


<h1>SelfEditing Sanity Check</h1>

<h3>Is there a self editing policy in the context?</h3>
<% 
PolicyList spl = ServletPolicyList.getPolicies(application);
SelfEditingPolicy sePolicy = null;
ListIterator it = spl.listIterator();
String found = "Could not find a SelfEditingPolicy";
while(it.hasNext()){
    PolicyIface p = (PolicyIface)it.next();
    if( p instanceof SelfEditingPolicy ){
        found = "Found a SelfEditingPolicy";
        sePolicy = (SelfEditingPolicy)p;
    }
}
%>
<%= found %>

<h3>Do you have a REMOTE_USER header from CUWebAuth?</h3>

<% String user = request.getHeader("REMOTE_USER"); 
if( user != null && user.length() > 0){
    %> Found a remote user of <%= user %>. <%
}else{
    %> Could not find a remote user.  Maybe you are not logged into CUWebAutn? <%
}
 %>
 <h3>Check if we can get a SelfEditingIdentifer for <%= user %></h3>
 <%
 SelfEditingIdentifierFactory.SelfEditing selfEditingId = null;
 IdentifierBundle ib  = null;
if( user != null && user.length() > 0){
  ib = RequestIdentifiers.getIdBundleForRequest(request);
  for( Object obj : ib){
      if( obj instanceof SelfEditingIdentifierFactory.SelfEditing )
          selfEditingId = (SelfEditingIdentifierFactory.SelfEditing) obj;
  }
  if( selfEditingId != null )
      found = "found a SelfEditingId " + selfEditingId.getValue();
  else
      found = "Cound not find a SelfEditingId";
%>
  <%= found %>
<%}else{%> 
   Cannot check becaue user is <%= user %>.
<%} %>   


<h3>Is that SelfEditingIdentifer blacklisted?</h3>
<% if( user == null || user.length() == 0 ){ %>
 No REMOTE_USER to check 
<% }else if( selfEditingId == null ){ %>
     no SelfEditingId to check   
<% }else if( selfEditingId.getBlacklisted() != null){%>
    SelfEditingId blacklisted because of <%= selfEditingId.getBlacklisted() %>
<% } else {%>
    SelfEditingId is not blacklisted.
<% } %>

<h3>Can an object property be edited with this SelfEditingId and Policy?</h3>
<% if( user == null || selfEditingId == null ){ %>
No
<% }else{ 
    AddObjectPropStmt whatToAuth = new AddObjectPropStmt(
           selfEditingId.getValue(),"http://mannlib.cornell.edu/fine#prp999" ,"http://mannlib.cornell.edu/fine#prp999");                
    PolicyDecision pdecison = sePolicy.isAuthorized(ib, whatToAuth);         
%> The policy decision was <%= pdecison %> 
  
<% } %>