<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page errorPage="/error.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabWebUtil" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Tab" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>

<% /***********************************************
 Display a primary tab.

 A display of a primary tab will include:
 primary tab heading,
 the primary tab description,
 the primary tab body,
 the secondary tabs ,
 the primary content tabs.

 expected request.attributes:
 'leadingTab'  Tab to be desplayed as root of display hierarchy

 bdc34 2006-01-03 created
 **********************************************/
    Tab leadingTab = (Tab) request.getAttribute("leadingTab");
    if (leadingTab == null) {
        String e = "tabprimary expects that request attribute 'leadingTab' be set to a TabBean object";
        throw new JspException(e);
    }
    TabWebUtil.stashTabsInRequest(leadingTab, request); %>
    <div id="content">
        <div id='contents'>
            <jsp:include page='/templates/tabs/tabBasic.jsp' flush='true'>
                <jsp:param name='tabId' value='<%= leadingTab.getTabId() %>' />
                <jsp:param name='tabDepth' value='1' />
            </jsp:include>
            <jsp:include page='/templates/tabs/tabAdmin.jsp' flush='true' />
        </div> <!-- contents -->
    </div><!-- content -->