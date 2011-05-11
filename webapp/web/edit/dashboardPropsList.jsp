<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousEditorPages" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.KeywordProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Property" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginStatusBean" %>
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.dashboardPropsList.jsp");
%>
<%
boolean showSelfEdits=false;
boolean showCuratorEdits=false;

IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(request);
String editorUri = SelfEditingIdentifierFactory.getSelfEditingUri(ids);
if (editorUri != null) {
    showSelfEdits=true;
    log.debug("self editing active");
} else {
    log.debug("self editing inactive");
}

if (PolicyHelper.isAuthorizedForActions(request, new UseMiscellaneousEditorPages())) {
	showCuratorEdits=true;
	log.debug("curator editing active");
} else {
	log.debug("curator editing inactive");
}
%>

<c:set var='entity' value='${requestScope.entity}'/><%-- just moving this into page scope for easy use --%>
<%
	log.debug("Starting dashboardPropsList.jsp");

	// The goal here is to retrieve a list of object and data properties appropriate for the vclass
	// of the individual, by property group, and sorted the same way they would be in the public interface

	Individual subject = (Individual) request.getAttribute("entity");
	if (subject==null) {
    	throw new Error("Subject individual must be in request scope for dashboardPropsList.jsp");
	}
	
	String defaultGroupName=null;
	String unassignedGroupName = (String) request.getAttribute("unassignedPropsGroupName");
	if (unassignedGroupName != null && unassignedGroupName.length()>0) {
	    defaultGroupName = unassignedGroupName;
	    log.debug("found temp group attribute \""+unassignedGroupName+"\" for unassigned properties");
	}

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
	PropertyGroupDao pgDao = wdf.getPropertyGroupDao();
	ArrayList<PropertyGroup> groupsList = (ArrayList) request.getAttribute("groupsList");
	if (groupsList != null) {
	    if (groupsList.size()>1) {%>
	        <ul id="propGroupNav">
<%        	for (PropertyGroup g : groupsList) { %>
				<li><h2><a href="#<%=g.getLocalName()%>" title="<%=g.getName()%>"><%=g.getName()%></a></h2></li>
<%		    }%>
			</ul>
<%	    }
	} else {
		ArrayList<Property> mergedList = (ArrayList) request.getAttribute("dashboardPropertyList");
		if (mergedList!=null) {
            String lastGroupName = null;
		    int groupCount=0;%>
		    <ul id="propGroupNav">
<%		    for (Property p : mergedList) {
    		    String groupName = defaultGroupName; // may be null
 			    String groupLocalName = defaultGroupName; // may be null
			    String groupPublicDescription=null;
			    String propertyLocalName = p.getLocalName() == null ? "unspecified" : p.getLocalName();
			    String openingGroupLocalName = (String) request.getParameter("curgroup");
    		    if (p.getGroupURI()!=null) {
    		        PropertyGroup pg = pgDao.getGroupByURI(p.getGroupURI());
    		        if (pg != null) {
		    		    groupName=pg.getName();
		    		    groupLocalName=pg.getLocalName();
		    		    groupPublicDescription=pg.getPublicDescription();
    		        }
    		    }
		        if (groupName != null && !groupName.equals(lastGroupName)) {
		    	    lastGroupName=groupName;
		            ++groupCount;
	    
			        if (openingGroupLocalName == null || openingGroupLocalName.equals("")) {
				        openingGroupLocalName = groupLocalName;
			        }
			        if (openingGroupLocalName.equals(groupLocalName)) {%>
      		            <li class="currentCat"><h2><a href="#<%=groupLocalName%>" title="<%=groupName%>"><%=groupName%></a></h2></li>
<%			        } else { %>
        		        <li><h2><a href="#<%=groupLocalName%>" title="<%=groupName%>"><%=groupName%></a></h2></li>
<%		            } 
                }
            }%>
            </ul>
<%      }
    }%>

