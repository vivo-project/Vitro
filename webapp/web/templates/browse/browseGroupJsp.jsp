<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClassGroup,edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>

<div id="content" class="siteMap">
    
	<c:choose>
	    <c:when test="${classgroupsIsEmpty}">
	        <p>There are not yet any items in the system.</p>
	    </c:when>
	
        <c:otherwise>
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
                            String linkStr=response.encodeURL("entitylist");
                if (theClass.getURI() == null)
                    theClass.setURI("null://null");
                            String queryStr="?vclassId="+URLEncoder.encode(theClass.getURI(),"UTF-8"); %>
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
        </c:otherwise>
	</c:choose>


</div> <!-- content -->
