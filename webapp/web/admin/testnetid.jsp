<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.*" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="com.thoughtworks.xstream.XStream" %>
<%@ page import="com.thoughtworks.xstream.io.xml.DomDriver" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.EditLiteral" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Generator" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.util.*" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.NetId"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle"%>
<%@page import="java.io.IOException"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>


<http>
Testing getIndividualURIFromNetId()
<% 
  String[] netids = {"bdc34", "jc55", "bjl24" , "mhd6" , "tpb2" };
  for( String netid : netids){
      %><h2>Checking <%=netid %></h2><%
    checkNetId( netid, out, request, (WebappDaoFactory)application.getAttribute("webappDaoFactory"));   
  }  
%>
</http>


<%! 

final  String CUWEBAUTH_REMOTE_USER_HEADER = "REMOTE_USER"; 


private void checkNetId( String inNetId, JspWriter out, HttpServletRequest request, WebappDaoFactory wdf ) throws IOException{ 

    if( inNetId != null
            && inNetId.length() > 0
            && inNetId.length() < 100 ){
    
        SelfEditingIdentifierFactory.NetId netid = new NetId(inNetId);                        
        SelfEditingIdentifierFactory.SelfEditing selfE = null;
                
        IdentifierBundle idb = new ArrayIdentifierBundle();
        idb.add(netid);
        //out.println("added NetId object to IdentifierBundle from CUWEBAUTH header");            
        
        //VitroRequest vreq = new VitroRequest((HttpServletRequest)request);
        String uri = wdf.getIndividualDao().getIndividualURIFromNetId(inNetId);
    
       
        if( uri != null){
                    Individual ind = wdf.getIndividualDao().getIndividualByURI(uri);
                    if( ind != null ){
                        selfE = new SelfEditing( ind, null );                
                        idb.add(  selfE );
                        out.println("found a URI and an Individual for " + inNetId +  " URI: " + ind.getURI());
                    }else{
                            out.println("found a URI for the netid " + inNetId + " but could not build Individual");
                    }
        }else{
            out.println("could not find a Individual with the neditd of " + inNetId );
        }
        
    }else{
        out.println("no remote user value found or value was longer than 100 chars.");    
    }
}
%>