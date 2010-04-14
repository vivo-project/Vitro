<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClassGroup,edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>

<%@page import="com.hp.hpl.jena.vocabulary.OWL"%><jsp:useBean id="loginHandler" class="edu.cornell.mannlib.vedit.beans.LoginFormBean" scope="session" />
<%  /***********************************************
         Display Browse Results (the "Index" menu command)

         request.attributes:
         a List with the name "classgroups"
         collatedGroupNames
         additionalParameterStr

         request.parameters:
         None yet.

          Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
          for debugging info.

        **********************************************/
if (request.getAttribute("classgroups") == null){
    String e="browsesGroup.jsp expects that request attribute 'classgroups' be set to a List of classgroups to display.";
    throw new JspException(e);
}
if (request.getAttribute("portalState") == null){
    String e="browsesGroup.jsp expects that request attribute 'portalState' be set to a portal state [PortalFlag] object.";
    throw new JspException(e);
}

String additionalParameterStr = ""; //we expect this to already be encoded as a url.
if(request.getAttribute("passthru") != null){
    additionalParameterStr = (String)request.getAttribute("passthru");
}

if( request.getAttribute("classgroupsIsEmpty") != null && ((Boolean)request.getAttribute("classgroupsIsEmpty")) == true){
	%>

	<div id="content" class="siteMap">
	<p>There not yet any items in the system.</p>
	</div> <!-- content -->
<% } else { %>		
<div id="content" class="siteMap">
    <form name="filterForm" action="browsecontroller" method="post" class="padded" >
    <jsp:include page="portalFlagChoices.jsp" flush="true" >
        <jsp:param name="action" value="browse" />
    </jsp:include>
    </form>
    <%
        Collection classgroupList = (Collection) request.getAttribute("classgroups");
        if (classgroupList != null) {
            Iterator groupIter = classgroupList.iterator();
            Object groupObj = null;
            while (groupIter.hasNext()) {
                groupObj = groupIter.next();
                if (groupObj != null && groupObj instanceof VClassGroup) {
                    VClassGroup theGroup = (VClassGroup) groupObj; %>
                <h2><%=theGroup.getPublicName()%></h2>
    <%          if (theGroup.getVitroClassList()!=null && theGroup.getVitroClassList().size()>0) {%>
                    <ul>
    <%              Iterator classIter=theGroup.getVitroClassList().iterator();
                    Object classObj=null;
                    while (classIter.hasNext()) {
                        classObj = classIter.next();
                        if (classObj!=null && classObj instanceof VClass) {
                            VClass theClass=(VClass)classObj;
                            //filter out owl:Thing
                            if( theClass.getName() == null || OWL.Thing.getURI().equals(theClass.getURI()))
                            	continue;
                           	String linkStr=response.encodeURL("entitylist");
               				if (theClass.getURI() == null)
                   				theClass.setURI("null://null");
                           	String queryStr="?vclassId="+URLEncoder.encode(theClass.getURI(),"UTF-8")+additionalParameterStr; %>
                           	<li><a href="<%=linkStr+queryStr%>"><%=theClass.getName()%></a> (<%=theClass.getEntityCount()%>)</li>
    <%                  }
                    }%>
                    </ul>
    <%          } else {%>
                    <ul><li>no entities</li></ul>
    <%          }
            }
        }
    }%>
</div> <!-- content -->
<% } %> 