<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Tab" %>
<%@ page import="java.util.Collection,java.util.Iterator" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabWebUtil" %>
<%@ page errorPage="/error.jsp"%>
<%
	int CutOffDepth = 3;
		/***********************************************
		 Tab Content is to display the sub tabs of type primaryTabContent
		 
		 Primary Content Tabs will have:
		 tab heading,
		 tab description
		 request.attributes
		 "leadingTab" the tab that is at the top of the display hierarchy
		  		  
		 "tabDepth" String that is the depth of the tab in the display
		 leadingTab = 1, child of leadingTab = 2, etc.
		 Here tabDepth does not default, it must be set
		 
		 bdc34 2006-01-12 created		 
        **********************************************/
        
    Tab leadingTab =(Tab) request.getAttribute("leadingTab");       
    if(leadingTab== null ) {
	        String e="tabContent expects that request attribute 'leadingTab' be set";
    	    throw new JspException(e);
     }    
             
    String tabId = request.getParameter("tabId");
    if( tabId == null ){
     	String e="tabContent expects that request parameter 'tabId' be set";
   	    throw new JspException(e);
    }
        
	Tab tab = null;
   	tab = TabWebUtil.findStashedTab(tabId,request);
   	if( tab == null ){
        String e="tabContent expects that request attribute 'leadingTab' will have the tab with tabId as a sub tab";
   	    throw new JspException(e);       	
     }
        
   String obj= request.getParameter("tabDepth");
    int depth = 1; //depth 1 represents primary tab level, 2 is secondary, etc.        
    if( obj == null ){
      	String e="tabContent expects that request parameter 'tabDepth' be set";
   	    throw new JspException(e);
    }
    depth = Integer.parseInt((String)obj);                    
	int childDepth = depth + 1;
    	
    Collection children = tab.filterChildrenForContentTabs();
    if( depth < CutOffDepth && children!= null ){
		Iterator childIter=children.iterator();                
		boolean hasChildren = childIter.hasNext();
		int columnSize = children.size();
		if(   hasChildren ){ %>
		  
			<div id='tabContent'><!-- tabContent.jsp -->
			<table><tr>
		<% } 	
		while( childIter.hasNext() ) {
			Tab contentTab = (Tab)childIter.next();
			    TabWebUtil.stashTabsInRequest(contentTab, request);	 		%>	
			    
			    <% if (columnSize==2) {%>
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
						 	<jsp:param name='tabId' value='<%=contentTab.getTabId()%>' />
						 	<jsp:param name='noContent' value='true'/>
						 	<jsp:param name='noSubtabs' value='true'/>
						</jsp:include>
						</td>					
		<% }	
		if( hasChildren ){ %>		
			</tr></table>
			</div><!--  end tabContent div-->
		<%}%>
<% } %>
