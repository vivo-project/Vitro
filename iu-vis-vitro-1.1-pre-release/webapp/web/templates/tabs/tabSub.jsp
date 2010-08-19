<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.Collection" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Tab" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabWebUtil" %>
<%@ page import="java.util.Iterator" %>
<%@ page errorPage="/error.jsp"%>
<%
    int CutOffDepth = 3; //tab depth at which subtabs stop being shown
    int HorzSubTabCutOff = 8; //subtab count at which we switch to tabSubAsList.jsp
    int EntsInDepth2TabsCutOff = 4; //subtab count at which entities stop being shown in depth 2 tabs.
    int TabDescCutoffDepth = 3;//depth at which descriptions of subtabs stop being shown

    /***********************************************
     Display a set of subtabs for a tab

     request.attributes
     "leadingTab" the tab that is at the top of the display hierarchy

     request.parameters
     "tabId" id of the tab to do subtabs for

     "noEntities" can be set to 'true' or 'false' and will control if
     subtabs have entities. defaults to false/having entites;

     "tabDepth" String that is the depth of the tab in the display
     leadingTab = 1, child of leadingTab = 2, etc.
     Here tabDepth does not default, it must be set

     bdc34 2006-01-12 created
     **********************************************/
    Tab leadingTab = (Tab) request.getAttribute("leadingTab");
    if (leadingTab == null) {
        String e = "tabSub expects that request attribute 'leadingTab' be set";
        throw new JspException(e);
    }

    String tabId = request.getParameter("tabId");
    if (tabId == null) {
        String e = "tabSub expects that request parameter 'tabId' be set";
        throw new JspException(e);
    }

    Tab tab = null;
    tab = TabWebUtil.findStashedTab(tabId, request);
    if (tab == null) {
        String e = "tabSub expects that request attribute 'leadingTab' will have the tab with tabId as a sub tab";
        throw new JspException(e);
    }

    String obj = request.getParameter("tabDepth");
    int depth = 1; //depth 1 represents primary tab level, 2 is secondary, etc.
    if (obj == null) {
        String e = "tabSub expects that request parameter 'tabDepth' be set";
        throw new JspException(e);
    }
    depth = Integer.parseInt((String) obj);
    int childDepth = depth + 1;

    Collection children = tab.filterChildrenForSubtabs();
    if (depth < CutOffDepth && children != null) {
        if (children.size() >= HorzSubTabCutOff) { /* too many children, do tabSubAsList instead */ %>
    			<jsp:include page='tabSubAsList.jsp' >
							<jsp:param name='tabDepth' value='<%=depth%>' />
						 	<jsp:param name='tabId' value='<%=tabId%>' />
				</jsp:include>    			
        <% } else {

            //here we figure out if these subtabs should have entities
            //if we were passed a parameter, then maybe no entities
            obj = request.getParameter("noEntities");
            String noEntities = "true".equalsIgnoreCase(obj) ? "true" : "false";
            //if we have more subtabs then the cutoff, no entities for the subtabs
            noEntities = (children.size() >= EntsInDepth2TabsCutOff ? "true" : noEntities);
            //if we are the first set of subtabs on a primary tab it seems there are no entities? sort of odd
            noEntities = ((tab.PRIMARY_TAB == tab.getTabtypeId() && childDepth == 2) ? "true" : noEntities);

            String noDesc = (childDepth >= TabDescCutoffDepth) ? "true" : "false";

            Iterator childIter = children.iterator();
            boolean hasChildren = childIter.hasNext();
            int columnSize = children.size();
            if (hasChildren) { %>

	<div class='subtabs'><!-- tabSub.jsp -->
		<table><tr>
		<% } 	
		while( childIter.hasNext() ) {
			Tab subtab = (Tab)childIter.next();
			    TabWebUtil.stashTabsInRequest(subtab, request);	 		%>			
			    
			    <% if (columnSize==1) {%>
			      <td class="span-24">
			    <% } %>
			    
			    <% else if (columnSize==2) {%>
			      <td class="span-12">
			    <% } %>
			    
			    <% else if (columnSize==3) {%>
			      <td class="span-8">
          <% } %>
          
			    <% else if (columnSize==4) {%>
			      <td class="span-6">
			    <% } %>
			    
			    <% else {%>
			      <td>
			    <% } %>
			    
						<jsp:include page='tabBasic.jsp'>
							<jsp:param name='tabDepth' value='<%=childDepth%>' />
						 	<jsp:param name='tabId' value='<%=subtab.getTabId()%>' />
						 	<jsp:param name='noDesc' value='<%=noDesc%>'/>
						 	<jsp:param name='noEntities' value='<%=noEntities%>'/>
						 	<jsp:param name='noContent' value='true'/>						 	
						 	<jsp:param name='noSubtabs' value='true'/>						 							 	
						 	<jsp:param name='subtabsAsList' value='true'/>								 	
						</jsp:include>
						</td>					
		<% }	
		if( hasChildren ){ %>		
			</tr></table>
			</div><!--  end subtabs div-->
		<%}
		} 
	} %>
