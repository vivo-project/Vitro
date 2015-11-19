<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.ArrayList" %>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.EDIT_SITE_INFORMATION.ACTION); %>
<vitro:confirmAuthorization />

<c:set var="appNameLabel" value="Site name"/> 
<label for="site-name">${appNameLabel}<span class="note"> (max 50 characters)</span></label>
<input type="text" name="ApplicationName" value="<form:value name="ApplicationName"/>" ${longField} maxlength="50" />
<c:set var="ApplicationNameError"><form:error name="ApplicationName"/></c:set>
<c:if test="${!empty ApplicationNameError}">
    <span class="notice"><c:out value="${ApplicationNameError}"/></span>
</c:if>

<!-- With introduction of new logo that includes tagline as part of the image, hiding this field for now to reduce user confusion -->        
<!-- <tr class="editformcell">
    <td valign="top" colspan="2">
        <b>Tagline </b> <i>appears in header, just to the right of the logo (leave blank to have no tagline)</i><br />
        <input type="text" name="ShortHand" value="<form:value name="ShortHand"/>" ${longField} maxlength="50" />
        <font color="red"><form:error name="ShortHand"/></font>
    </td>
</tr> -->

        <label>Contact email address <span class="note">contact form submissions will be sent to this address</span></label>
        <input type="text" name="ContactMail" value="<form:value name="ContactMail"/>" ${longField} maxlength="255" size="30" />
        <c:set var="ContactMailError"><form:error name="ContactMail"/></c:set>
        <c:if test="${!empty ContactMailError}">
            <span class="notice"><c:out value="${ContactMailError}"/></span>
        </c:if>
        <br />

        <label class="display-inline">Theme</label>
        <select id="ThemeDir" name="ThemeDir">
            <form:option name="ThemeDir" />
        </select>

        <label>Copyright text<span class="note"> used in footer (e.g., name of your institution)</span></label>
        <input type="text" name="CopyrightAnchor" value="<form:value name="CopyrightAnchor"/>" ${longField} maxlength="120" size="40" />
        <c:set var="CopyrightAnchorError"><form:error name="CopyrightAnchor"/></c:set>
        <c:if test="${!empty CopyrightAnchorError}">
            <span class="notice"><c:out value="${CopyrightAnchorError}"/></span>
        </c:if>

        <label>Copyright URL<span class="note"> copyright text links to this URL</span></label>
        <input type="text" name="CopyrightURL" value="<form:value name="CopyrightURL"/>" ${longField} maxlength="120" size="30" />
        <c:set var="CopyrightURLError"><form:error name="CopyrightURL"/></c:set>
        <c:if test="${!empty CopyrightURLError}">
            <span class="notice"><c:out value="${CopyrightURLError}"/></span>
        </c:if>

<!--
<tr class="editformcell">
    <td valign="bottom" colspan="4">
        <b>About message</b> <i>used for main content area on About page (HTML is allowed)</i><br />
        <textarea name="AboutText" style="width: 90%;" ROWS="20" wrap="physical"><form:value name="AboutText"/></textarea>
        <font color="red"><form:error name="AboutText"/></font>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="4">
        <b>Acknowledgement message</b> <i>used for acknowledgement area on About page (HTML is allowed)</i></b><br />
        <textarea name="AcknowledgeText" style="width: 90%;" ROWS="5" wrap="physical"><form:value name="AcknowledgeText"/></textarea>
        <font color="red"><form:error name="AcknowledgeText"/></font>
    </td>
</tr>
-->
<!--
        <label>Banner image</label>
        <input type="text" name="BannerImage" value="<form:value name="BannerImage"/>" style="width:90%" maxlength="255" />
        <font color="red"><form:error name="BannerImage"/></font>
  
        <label>Banner image width</label>
        <input type="text" name="BannerWidth" value="<form:value name="BannerWidth"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="BannerWidth"/></font>

        <label>Banner image height</label>
        <input type="text" name="BannerHeight" value="<form:value name="BannerHeight"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="BannerHeight"/></font>

        <label>Logotype image</label>
        <input type="text" name="LogotypeImage" value="<form:value name="LogotypeImage"/>" style="width:90%" maxlength="255" />
        <font color="red"><form:error name="LogotypeImage"/></font>

        <label>Logotype image width</label>
        <input type="text" name="LogotypeWidth" value="<form:value name="LogotypeWidth"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="LogotypeWidth"/></font>

        <label>Logotype image height</label>
        <input type="text" name="LogotypeHeight" value="<form:value name="LogotypeHeight"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="LogotypeHeight"/></font>
 
-->