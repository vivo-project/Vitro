<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.ArrayList" %>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<c:set var="singlePortal" value="${requestScope.singlePortal}"/>
<c:set var="creatingNewPortal" value="${requestScope.creatingNewPortal}"/>
<c:set var="multiPortal" value = "${!singlePortal || creatingNewPortal}"/>
<c:set var="appNameLabel" value="${ !multiPortal ? 'Site Name' : 'Portal Application Name' }"/> 

<c:set var="smallCell" value="style='width: 33%;'" />
<c:set var="longField" value="${multiPortal == true ? \"style='width: 90%;'\" : \"style='width: 75%;'\"}" />
 
<tr class="editformcell">
    <td valign="top" colspan="2">
        <b>${appNameLabel}</b> <i>(max 50 characters)</i><br />
            <input type="text" name="AppName" value="<form:value name="AppName"/>" ${longField} maxlength="50" />
            <font color="red"><form:error name="AppName"/></font>
    </td>

    <c:if test="${multiPortal}">
        <td valign="top" colspan="1">
            <b>Portal ID number</b><br />            
            <c:choose>
                <c:when test="${_action == 'insert'}">
                  <input type="text" name="PortalId" ${smallCell}
                         value="<form:value name="PortalId"/>" maxlength="5" />
                </c:when>
                <c:otherwise>
                  <input type="text" name="PortalId" disabled="disabled" ${smallCell}
                         value="<form:value name="PortalId"/>" maxlength="5" />
                </c:otherwise>
            </c:choose>
            <font color="red"><form:error name="PortalId" /> </font>
        </td>
        <td valign="top" colspan="1">
            <b>URL ending</b><br/>
            <input type="text" name="Urlprefix" ${smallCell} value="<form:value name="Urlprefix"/>"/>
            <font color="red"><form:error name="PortalId"/><font>
        </td>
    </c:if>     
</tr>

<!-- With introduction of new logo that includes tagline as part of the image, hiding this field for now to reduce user confusion -->        
<!-- <tr class="editformcell">
    <td valign="top" colspan="2">
        <b>Tagline </b> <i>appears in header, just to the right of the logo (leave blank to have no tagline)</i><br />
        <input type="text" name="ShortHand" value="<form:value name="ShortHand"/>" ${longField} maxlength="50" />
        <font color="red"><form:error name="ShortHand"/></font>
    </td>
</tr> -->

<!-- mb has added the following input text area to collect the homepage blurb, it is not functional -->
<tr class="editformcell">
    <td valign="bottom" colspan="4">
        <b>Homepage blurb</b> <i>used for short institution summary (HTML is allowed)</i><br />
        <textarea name="HomepageBlurb" style="width: 90%;" ROWS="7" wrap="physical"><form:value name=""/></textarea>
        <font color="red"><form:error name=""/></font>
    </td>
</tr>


<tr class="editformcell">
    <td valign="bottom" colspan="2">
        <b>Contact Email Address</b> <i>contact form submissions will be sent to this address</i><br />
        <input type="text" name="ContactMail" value="<form:value name="ContactMail"/>" ${longField} maxlength="255" />
        <font color="red"><form:error name="ContactMail"/></font>
    </td>

</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="1">
        <b>Root Tab</b><br />
        <select name="RootTabId">
            <form:option name="RootTabId"/>
        </select>
        <font color="red"><form:error name="RootTabId"/></font>
    </td>
    <td valign="top" colspan="1">
        <b>Theme</b><br />
        <select name="ThemeDir">
            <form:option name="ThemeDir" />
        </select>
        <font color="red"><form:error name="ThemeDir"/></font>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="2">
        <b>Copyright text</b> <i>used in footer (e.g., name of your institution)</i><br />
        <input type="text" name="CopyrightAnchor" value="<form:value name="CopyrightAnchor"/>" ${longField} maxlength="120" />
        <font color="red"><form:error name="CopyrightAnchor"/></font>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="2">
        <b>Copyright URL</b> <i>copyright text links to this URL</i><br />
        <input type="text" name="CopyrightURL" value="<form:value name="CopyrightURL"/>" ${longField} maxlength="120" />
        <font color="red"><form:error name="CopyrightURL"/></font>
    </td>
</tr>

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

<tr class="editformcell hideFromVivoWeb">
    <td valign="bottom" colspan="2">
        <b>Flag 1 Filtering</b> <i>if true, then filter pages generated for this portal by flag1</i><br/>
        <select name="Flag1Filtering" >
            <form:option name="Flag1Filtering"/>
        </select>
        <font color="red"><form:error name="Flag1Filtering"/></font>
    </td>
</tr>
<tr class="editformcell hideFromVivoWeb">   
    <td valign="bottom" colspan="1">
        <b>Banner image</b><br />
        <input type="text" name="BannerImage" value="<form:value name="BannerImage"/>" style="width:90%" maxlength="255" />
        <font color="red"><form:error name="BannerImage"/></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Banner image width</b><br />
        <input type="text" name="BannerWidth" value="<form:value name="BannerWidth"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="BannerWidth"/></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Banner image height</b><br />
        <input type="text" name="BannerHeight" value="<form:value name="BannerHeight"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="BannerHeight"/></font>
    </td>
</tr>
<tr class="editformcell hideFromVivoWeb">   
    <td valign="bottom" colspan="1">
        <b>Logotype image</b><br />
        <input type="text" name="LogotypeImage" value="<form:value name="LogotypeImage"/>" style="width:90%" maxlength="255" />
        <font color="red"><form:error name="LogotypeImage"/></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Logotype image width</b><br />
        <input type="text" name="LogotypeWidth" value="<form:value name="LogotypeWidth"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="LogotypeWidth"/></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Logotype image height</b><br />
        <input type="text" name="LogotypeHeight" value="<form:value name="LogotypeHeight"/>" ${smallCell} maxlength="11" />
        <font color="red"><form:error name="LogotypeHeight"/></font>
    </td>
</tr>  

<% /*

<tr class="editformcell">
    <!-- this needs to be added to the bean -->
    <td valign="bottom" colspan="1">
        <b>Flag 1 values</b><br/>
            <input disabled="disabled" type="text" name="field9Value" value="" style="width:60" maxlength="255" />
            <font color="red"></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Flag 2 numeric equivalent</b> <i>THIS FILTERING HAS BEEN TRANSFERRED TO TABS</i><br />
            <input type="text" name="Flag2Numeric" value="<form:value name="Flag2Numeric"/>" style="width:33%" maxlength="11" />
            <font color="red"><form:error name="Flag2Numeric"/></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Flag 3 numeric equivalent</b> <i>THIS FILTERING HAS BEEN TRANSFERRED TO TABS</i><br />
            <input type="text" name="Flag3Numeric" value="<form:value name="Flag3Numeric"/>" style="width:33%" maxlength="11" />
            <font color="red"><form:error name="Flag3Numeric"/></font>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="1">
        <b>Filter by XXXX on Advanced Search form?</b><br />
            <select disabled="disabled" name="field23Value">
                    <option selected value="false">false</option>
            </select>
            <font color="red"></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Filter by XXXY on Advanced Search form?</b><br />
            <select disabled="disabled" name="field24Value">
                    <option value="false">false</option>
            </select>
            <font color="red"></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Filter by XXYX on Advanced Search form?</b><br />
            <select disabled="disabled" name="field25Value">
                    <option value="false">false</option>
            </select>
            <font color="red"></font>
    </td>
</tr>
*/ %>