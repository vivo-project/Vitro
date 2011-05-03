<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabWebUtil" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Tab" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page errorPage="/error.jsp"%>
<% /***********************************************
 Display a single tab or subtab in the most basic fashion.

 Note: most of the depth based control is in tabSub.jsp
 tabSubAsList.jsp and tabEntities.jsp.  The only depth based
 control of what gets displayed that is in this code is
 the tab.body, which is only displayed on depth 1.

 request.attributes:
 a Tab object with the name "tab3223" where 3223 is
 the id of the tab to display.  This can be added to the attribute
 list using TabWebUtil.stashTabsInRequest which gets called
 in tabprimary.jsp.

 request.parameters:
 "tabDepth" String that is the depth of the tab in the display
 leadingTab = 1, child of leadingTab = 2, etc.
 if tabDepth is not set it defaults to 1
 "tabId" id of the tab to display, defaults to leadingTab.

 "noDesc" if true then don't display the tab.description, default is false.
 "noEntities" if true then don't display the associated entities for this tab.
 "noContent" if true then don't display the primary content tabs.
 "noSubtabs" if true then don't display the subtabs associated with this tab.
 "subtabsAsList" if true then display just children (not grand children,etc) subtabs as a list.

 Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html
 output for debugging info.

 bdc34 2006-01-03 created
 **********************************************/
    String INACTIVE = "";
    String DEFAULT_LABEL = null;

    /***************************************************
    nac26 2008-05-08 following brian's lead from menu.jsp to get the portalId so it can be added to the tab links */
    final Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.web.tabBasic.jsp");

    int portalId = 1;

    /**************************************************/

    String tabId = request.getParameter("tabId");
    if (tabId == null) {
        String e = "tabBasic expects that request parameter 'tabId' be set";
        throw new JspException(e);
    }

    Tab tab = TabWebUtil.findStashedTab(tabId, request);
    if (tab == null) {
        String e = "tabBasic expects that request attribute 'leadingTab' will have the tab with tabId as a sub tab";
        throw new JspException(e);
    }

    String obj = request.getParameter("tabDepth");
    int depth = 1; //depth 1 represents primary tab level, 2 is secondary, etc.
    if (obj == null) {
        String e = "tabBasic expects that request parameter 'tabDepth' be set";
        throw new JspException(e);
    }
    depth = Integer.parseInt((String) obj);

    boolean entities = true;
    if ("true".equalsIgnoreCase(request.getParameter("noEntities")))
        entities = false;
    boolean content = true;
    if ("true".equalsIgnoreCase(request.getParameter("noContent")))
        content = false;
    boolean subtabs = true;
    if ("true".equalsIgnoreCase(request.getParameter("noSubtabs"))) 
        subtabs = false;
    boolean subtabsAsList = false;
    if ("true".equalsIgnoreCase(request.getParameter("subtabsAsList"))) {
        subtabsAsList = true;
        subtabs = false;
    }

    boolean noDesc = false;
    if ("true".equalsIgnoreCase(request.getParameter("noDesc"))) {
        noDesc = true;
    }

    String tabEntsController = Controllers.TAB_ENTITIES;

    String titleLink = TabWebUtil.tab2TabAnchorElement(tab, request, depth, INACTIVE, DEFAULT_LABEL, portalId);
    if (depth == 1)//don't make a link for root dispaly tab
        titleLink = tab.getTitle();

    Integer headingDepth = depth + 1;
    String headingOpen = "<h" + headingDepth + ">";
    String headingClose = "</h" + headingDepth + ">";

    String tabDesc = "";
    if (!noDesc && tab.getDescription() != null && !tab.getDescription().equals("&nbsp;"))
        tabDesc = "<div class='tabDesc' >" + tab.getDescription() + "</div>";
    String tabBody = "";

    /* tab.body is only displayed for the depth 1 display tab */
    if (depth <= 2 && tab.getBody() != null && !tab.getBody().equals("&nbsp;"))
        tabBody = "<div class='tabBody'>" + tab.getBody() + "</div><!-- END div class='tabBody' -->";
%>
<div class="tab depth<%=depth%>" id="tab<%=tabId%>">
<%=headingOpen%><%=titleLink%><%=headingClose%>
<%=tabDesc%>
<%=tabBody%>
<% if( entities ){ %>
	<jsp:include page="<%=tabEntsController%>" flush="true">		
		<jsp:param name="tabId" value="<%=tab.getTabId()%>"/>					
		<jsp:param name="tabDepth" value="<%=depth%>"/>
	</jsp:include>
<% } %>
<% if( subtabs ) { %>								
	<jsp:include page="tabSub.jsp" flush="true">				
		<jsp:param name="tabId" value="<%=tab.getTabId()%>"/>
		<jsp:param name="tabDepth" value="<%=depth%>"/>
	</jsp:include>
<% }%>
<% if( subtabsAsList ) { %>								
	<jsp:include page="tabSubAsList.jsp" flush="true">				
		<jsp:param name="tabId" value="<%=tab.getTabId()%>"/>
		<jsp:param name="tabDepth" value="<%=depth%>"/>
	</jsp:include>
<% }%>
<% if(content) { %>
	<jsp:include page="tabContent.jsp" flush="true">
		<jsp:param name="tabId" value="<%=tab.getTabId()%>"/>
		<jsp:param name="tabDepth" value="1" />
	</jsp:include>		
<% } %>
</div><!-- tab depth<%=depth%> -->