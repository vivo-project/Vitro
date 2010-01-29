<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.lang.Integer"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.flags.PortalFlagChoices" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.flags.AuthFlag" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.flags.PortalFlag" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page errorPage="/error.jsp"%>

<%
    /***********************************************
     Display A Form for filling out Portal Flags

     request.attributes:
     PortalFlag object via attribute "portalState".
     request.parameters:
     None yet.

     Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output for debugging info.

     This was written by jc55 and split into this file from browseGroups.jsp by bdc34.
     **********************************************/
    PortalFlag portalFilterState = (PortalFlag) request.getAttribute("portalFlag");    
    if (portalFilterState == null) {
        String e = "portalFlagChoices.jsp expects that request attribute 'portalFlag' be set to a portal state [PortalFlag] object.";
        throw new JspException(e);
    }
    AuthFlag authFlag = (AuthFlag) request.getAttribute("authFlag");
    if (authFlag == null) {
        String e = "portalFlagChoices.jsp expects that request attribute 'authFlag' be set to a authorization state [AuthFlag] object.";
        throw new JspException(e);
    }
    ApplicationBean appBean = (ApplicationBean) request.getAttribute("appBean");
    if (appBean == null) {
        String e = "portalFlagChoices.jsp expects request attribute 'appBean' be set to an application bean object";
        throw new JspException(e);
    }
    Portal portal = (Portal) request.getAttribute("portalBean");
    if( portal == null )
        portal = new Portal();
    int portalId = portal.getPortalId();
%>

<script type="text/javascript" language="Javascript" >
//<!--
function submitFilterValue(element) {
    if (element.value==-1) {
        ; // do nothing
    } else {
        document.filterForm.filter.value=element.value;
        document.filterForm.submit();
    }
}

function checkAllFlag1( whichForm ) {
    if (whichForm.flag1.length) {
        for (i=0; i<whichForm.flag1.length; i++) {
            whichForm.flag1[i].checked="checked";
        }
    } else {
        whichForm.flag1.checked="checked";
    }
}

function unCheckAllFlag1( whichForm ) {
    if (whichForm.flag1.length) {
        for (i=0; i<whichForm.flag1.length; i++) {
            whichForm.flag1[i].checked="";
        }
    } else {
        whichForm.flag1.checked="";
    }
}

function checkAllFlag2( whichForm ) {
    if (whichForm.flag2.length) {
        for (i=0; i<whichForm.flag2.length; i++) {
            whichForm.flag2[i].checked="checked";
        }
    } else {
        whichForm.flag2.checked="checked";
    }
}

function unCheckAllFlag2( whichForm ) {
    if (whichForm.flag2.length) {
        for (i=0; i<whichForm.flag2.length; i++) {
            whichForm.flag2[i].checked="";
        }
    } else {
        whichForm.flag2.checked="";
    }
}


function checkAllFlag3( whichForm ) {
    if (whichForm.flag3.length) {
        for (i=0; i<whichForm.flag3.length; i++) {
            whichForm.flag3[i].checked="checked";
        }
    } else {
        whichForm.flag3.checked="checked";
    }
}

function unCheckAllFlag3( whichForm ) {
    if (whichForm.flag3.length) {
        for (i=0; i<whichForm.flag3.length; i++) {
            whichForm.flag3[i].checked="";
        }
    } else {
        whichForm.flag3.checked="";
    }
}
//-->
</script>
<%
String width1Str=request.getParameter("width1");
int width1=14;
if (width1Str!=null && !width1Str.equals("")) {
	try {
		width1=Integer.parseInt(width1Str);
	} catch (NumberFormatException ex) {
		throw new JspException("Error: width1 parameter cannot be decoded as integer in portalFlagChoices.jsp");
	}
}
%>

<div class="browseFilter">

<table width="100%" border="0" cellspacing="0" cellpadding="5" align="center" >
<%
// Draw the appropriate checkboxes if they have been populated
// We are dealing with 3 situations for showing portal options in browsing:
// #1 For normal browsing (not in one of the application's shared portals [the CALS research portals] the user has 2 radio button choices:
//         ___ [portal appName] only  OR ___ don't filter
//
// #2 When browsing in one of the application's shared portals [1 of the 4 CALS research portals or the composite "All CALS Research" portal],
//    the user has the same number of checkbox choices as there are shared portals (e.g, 4 for CALS)
//         Optionally limit to:
//            ___ New life sciences  ___ Environmental sciences  ___ Land grant mission  ___ Applied social sciences
//
// #3 When an editor has logged in and starts browsing, all portals with an id less <= the application's maxPortalId are always shown as options
//    unless filters are turned completely off by an incoming request parameter

boolean haveFlag1Choices=false;
if (appBean.isFlag1Active()&& appBean.getFlag1List()!=null && appBean.getFlag1List().size()>1) {
	if (portalFilterState.getFlag1DisplayStatus()==PortalFlag.SHOW_SHARED_PORTALS || authFlag.getUserSecurityLevel()>=ApplicationBean.FILTER_SECURITY_LEVEL) {
		haveFlag1Choices=true;%>
	    <tr valign="top" align="left">
	    <td width="<%=width1%>%" class="form-item">
	        <input type="radio" name="omit1" value="false" <%=portalFilterState.getFlag1Exclusive()?"":"checked='checked'"%> /> limit to
	        <input type="radio" name="omit1" value="true"  <%=portalFilterState.getFlag1Exclusive()?"checked='checked'":""%> /> omit
	    </td>
	    <td class="form-item">
	    <%=PortalFlagChoices.getFlag1Choices(portalFilterState,appBean,portalId)%>
	    </td></tr>
<%	}
}
String actionStr=request.getParameter("action");
boolean haveFlag2Choices=false;
if (appBean.isFlag2Active() && appBean.getFlag2List()!=null
	&& appBean.getFlag2List().size()>1
	&& (actionStr==null || (actionStr.equals("browse") && authFlag.getUserSecurityLevel()>=ApplicationBean.FILTER_SECURITY_LEVEL))) {
	haveFlag2Choices=true;%>
    <tr valign="top" align="left">
    <td class="form-item">
    <input type="radio" name="omit2" value="false" <%=portalFilterState.getFlag2Exclusive()?"":"checked='checked'"%> /> limit to
    <input type="radio" name="omit2" value="true"  <%=portalFilterState.getFlag2Exclusive()?"checked='checked'":""%> /> omit
    </td><td class="form-item">
    <%=PortalFlagChoices.getFlag2Choices(portalFilterState, appBean)%>
    </td></tr><%
}

boolean haveFlag3Choices=false;
if (appBean.isFlag3Active() && appBean.getFlag3List()!=null
	&& appBean.getFlag3List().size()>1
	&& (actionStr==null || (actionStr.equals("browse") && authFlag.getUserSecurityLevel()>=ApplicationBean.FILTER_SECURITY_LEVEL))) {
	haveFlag3Choices=true;%>
    <tr valign="top" align="left">
    <td class="form-item">
    <input type="radio" name="omit3" value="false" <%=portalFilterState.getFlag3Exclusive()?"":"checked='checked'"%> /> limit to
    <input type="radio" name="omit3" value="true"  <%=portalFilterState.getFlag3Exclusive()?"checked='checked'":""%> /> omit
    </td><td class="form-item">
    <%=PortalFlagChoices.getFlag3Choices(portalFilterState,appBean,portal)%>
    </td></tr><%
}

//if (actionStr!=null && actionStr.equals("browse")){
if (haveFlag1Choices || haveFlag2Choices || haveFlag3Choices) {%>
	<tr><td colspan="2">
    <input type="radio" name="filter" value="true" onclick="submitFilterValue(this);" /> filter as indicated above
<%  if (authFlag.getUserSecurityLevel()>=appBean.FILTER_SECURITY_LEVEL) {%>
    	<input type="radio" name="filter" value="false" onclick="submitFilterValue(this);" /> don't filter at all<%
   	}%>
	</td></tr><%
}%>
</table>

</div><!--browseFilter-->
