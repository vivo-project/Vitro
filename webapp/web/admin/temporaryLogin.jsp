<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vedit.beans.LoginStatusBean" %>

<%@ page import="com.hp.hpl.jena.rdf.model.*" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.FakeSelfEditingIdentifierFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.policy.setup.CuratorEditingPolicySetup" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%  if (!LoginStatusBean.getBean(request).isLoggedInAtLeast(LoginStatusBean.CURATOR)) {
        %><c:redirect url="<%= Controllers.LOGIN %>" /><%
    }

    if(  request.getParameter("force") != null ){        
        VitroRequestPrep.forceToSelfEditing(request);
        String netid = request.getParameter("netid");
        // note that this affects the current user's session, not the whole servlet context
        FakeSelfEditingIdentifierFactory.clearFakeIdInSession( session );
        FakeSelfEditingIdentifierFactory.putFakeIdInSession( netid , session );
        // don't want to do this because would affect the whole session
        // if (!LoginStatusBean.getBean(request).isLoggedInAtLeast(LoginStatusBean.CURATOR)) {
        //	   CuratorEditingPolicySetup.removeAllCuratorEditingPolicies(getServletConfig().getServletContext());
        //} %>
        <jsp:forward page="/edit/login.jsp"/>                   
<%  }
    String loggedOutNetId = (String)session.getAttribute(FakeSelfEditingIdentifierFactory.FAKE_SELF_EDIT_NETID);
    if( request.getParameter("stopfaking") != null){
        VitroRequestPrep.forceOutOfSelfEditing(request);
        FakeSelfEditingIdentifierFactory.clearFakeIdInSession( session );        
     	// don't want to do this because would affect the whole session
        // if (!LoginStatusBean.getBean(request).isLoggedInAtLeast(LoginStatusBean.CURATOR)) {
        //	   CuratorEditingPolicySetup.replaceCuratorEditing(getServletConfig().getServletContext(),(Model)application.getAttribute("jenaOntModel"));
        //}
        %><c:redirect url="/"></c:redirect><%
    }    
    String netid = (String)session.getAttribute(FakeSelfEditingIdentifierFactory.FAKE_SELF_EDIT_NETID);
    String msg = "You have not configured a netid for testing self-editing. ";
    if( netid != null )
        msg = "You are testing self-editing as '" + netid + "'.";
    else
        netid = "";
        
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"><head><title>CUWebLogin</title>

 <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
 
 <link rel="stylesheet" type="text/css" href="temporaryLoginFiles/main.css">
 <link rel="stylesheet" type="text/css" href="temporaryLoginFiles/filter.css">
 <link rel="stylesheet" type="text/css" href="temporaryLoginFiles/dialogs.css">

 <script language="JavaScript">
 <!--
   function OpenCertDetails()
   {
      thewindow=window.open('https://www.thawte.com/cgi/server/certdetails.exe?code=uscorn123-1',
      'anew',config='height=400,width=450,toolbar=no,menubar=no,scrollbars=yes,resizable=no,location=no,directories=no,status=yes');
   }
 // -->
 </script>

</head><body onload="cuwl_focus_netid();">
<!-- header 1 -->
<div class="wrapper" id="header1wrap">
 <div class="inner" id="header1">
 <div id="logo">
  <a href="http://www.cornell.edu/" title="Cornell University"><img src="temporaryLoginFiles/cu.gif" alt="Cornell University" border="0" height="23" width="211"></a>
 </div>
 </div>
</div>
<!-- end header 1 -->


<hr class="hidden">

<!-- header 2 -->
<div id="header2wrap">

 <!-- these two divs are just for background color -->
 <div id="header2left">&nbsp;</div>
 <div id="header2right">&nbsp;</div>
 <!-- end background divs -->

 <div id="header2">

  <div id="title"><a href="http://www.cit.cornell.edu/authent/new/cuwl/cuwl.html"><img src="temporaryLoginFiles/cuwl-title.png" alt="CUWebLogin" border="0" height="74" width="213"></a></div>
  <div id="manage">
   Cornell University Login
  </div>

 </div>
</div>
<!-- end header 2 -->

<!-- header 3 -->
<div class="wrapper" id="header3wrap">
 <div class="inner" id="header3">

 <span>
  <a href="http://www.cit.cornell.edu/identity/cuweblogin.html">About
  CUWebLogin</a> 
 </span>

 </div>
</div>
<!-- end header 3 -->
    
<!-- ---------------------------- BEGIN main body -->
<div class="wrapper" id="main">
 <div class="inner" id="content">
  <hr class="hidden">
  <form name="dialog" method="post" action="temporaryLogin.jsp">
  <table>
   <tbody><tr>
    <td id="loginboxcell">
     <table class="loginbox" id="webloginbox">  
      <tbody><tr id="toprow">
       <td>
        <img src="temporaryLoginFiles/logindogs.gif" alt="">
       </td>
       <td>
        <img src="temporaryLoginFiles/KfWeb.gif" alt="Kerberos for Web"><br>
        <em>
        Please enter your Cornell NetID
        
        </em>
       </td>
      </tr>
      <tr>
       <td>
        &nbsp;
       </td>
       <td>
        <table id="entrybox">
         <tbody><tr>
          <td>NetID:</td>
          <td>
           <input class="textinput" name="netid" type="text" value="" />
           <input type="hidden" name="force" value="1"/>
          </td>
         </tr>
         <tr>
          <td><!-- Password: --></td>
          <td>
            <strong>For testing purposes only</strong>.
          </td>
         </tr>
        </tbody></table>
       </td>
      </tr>
      <tr>
       <td>
        &nbsp;
       </td>
       <td id="buttoncell">
        <input class="inputsubmitHead" name="cancel" value="Cancel" onclick="cancel_submit();" type="button">
        <input class="inputsubmitHead" name="ok" value="OK" type="submit">
       </td>
      </tr>
     </tbody></table>
    </td>
    <td id="infocell">
     <br>
      <table id="reasonbox">
        <tbody><tr><td>
            <c:if test="${!empty param.stopfaking}">
                You have successfully logged out from <%=loggedOutNetId%>.
                <c:url var="profileHref" value="/entity">
                    <c:param name="netid" value="<%=loggedOutNetId%>" />
                </c:url>
                Return to that <a href="${profileHref}" title="view your public profile">public profile</a>.
            </c:if>
        </td></tr>
      </tbody></table>
     <br>
      <!-- The Web site you are visiting requires you to authenticate with your NetID and Password -->
     <br>
  <!--   <a href="javascript:OpenCertDetails()">
     <IMG SRC="/images/thawte-seal.gif" BORDER=0 ALT='Click here for SSL Cert Details'>
     </a> -->
<!-- GeoTrust True Site [tm] Smart Icon tag. Do not edit. -->
<!-- <SCRIPT LANGUAGE="JavaScript" TYPE="text/javascript" SRC="//smarticon.geotrust.com/si.js"></SCRIPT> -->
<!-- <img src="temporaryLoginFiles/quickssl_anim.gif" border="0"> -->
<!-- end GeoTrust Smart Icon tag -->
     <br>
    </td>
   </tr>
  </tbody></table>
 
  </form>
  <hr class="hidden">
 </div>
</div>
<!-- ---------------------------- END main body -->

<!-- footer include in -->
<div class="wrapper" id="footer">
 <div class="inner">
  <em>Mann Library Notice:</em> <strong>This IS NOT an official CUWebLogin
   screen.  It is meant for testing purposes only</strong>.
 </div>
</div>
<!-- footer include out -->
</body></html>