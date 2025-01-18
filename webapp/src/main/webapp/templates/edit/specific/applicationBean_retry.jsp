<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

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


        <!-- <div class="themePreferences" style="display: hidden;"> -->
        <div class="themePreferences">
            <label for="ThemePrimaryColor">Primary Color</label>
            <div class="themeColorPickerWrapper">

                <div class="advancedThemeColor">
                    <input type="hidden" id="ThemePrimaryColorLighterHidden" name="ThemePrimaryColorLighter" value="null" />
                    <input type="color" id="ThemePrimaryColorLighter" name="" value="<form:value name="ThemePrimaryColorLighter"/>" initial-value="<form:value name="ThemePrimaryColorLighter"/>" />
                    <div>
                        <small>Lighter</small>
                    </div>
                </div>

                <div>
                    <input type="hidden" id="ThemePrimaryColorHidden" name="ThemePrimaryColorLighter" value="null" />
                    <input type="color" id="ThemePrimaryColor" name="" value="<form:value name="ThemePrimaryColor"/>" initial-value="<form:value name="ThemePrimaryColor"/>" />
                    <div class="advancedThemeColor">
                        <small>Base</small>
                    </div>
                </div>

                <div class="advancedThemeColor">
                    <input type="hidden" id="ThemePrimaryColorDarkerHidden" name="ThemePrimaryColorLighter" value="null" />
                    <input type="color" id="ThemePrimaryColorDarker" name="" value="<form:value name="ThemePrimaryColorDarker"/>" initial-value="<form:value name="ThemePrimaryColorDarker"/>" />
                    <div>
                        <small>Darker</small>
                    </div>
                </div>
                <a href="#" class="themeColorReset" id="resetPrimaryColorLink"><span>reset</span></a>

        
            </div>
            
            <div class="advancedThemeColor">
                <label for="ThemeBannerColor">Banner Color</label>
                <div class="themeColorPickerWrapper">
                    <input type="hidden" id="ThemeBannerColorHidden" name="ThemeBannerColor" value="null" />
                    <input type="color" id="ThemeBannerColor" name="" value="<form:value name="ThemeBannerColor"/>" initial-value="<form:value name="ThemeBannerColor"/>" />
                    <a href="#" class="themeColorReset" id="resetBannerColorLink"><span>reset</span></a>
                </div>
            </div>
            

            <div>
                <label for="ThemeSecondaryColor">Secondary Color</label>
                <div class="themeColorPickerWrapper">
                    <input type="hidden" id="ThemeSecondaryColorHidden" name="ThemeSecondaryColor" value="null" />
                    <input type="color" id="ThemeSecondaryColor" name="" value="<form:value name="ThemeSecondaryColor"/>" initial-value="<form:value name="ThemeSecondaryColor"/>" />
                    <a href="#" class="themeColorReset" id="resetSecondaryColorLink"><span>reset</span></a>
                </div>
            </div>

            <div>
                <label for="ThemeAccentColor">Accent Color</label>
                <div class="themeColorPickerWrapper">
                    <input type="hidden" id="ThemeAccentColorHidden" name="ThemeAccentColor" value="null" />
                    <input type="color" id="ThemeAccentColor" name="" value="<form:value name="ThemeAccentColor"/>" initial-value="<form:value name="ThemeAccentColor"/>" />
                    <a href="#" class="themeColorReset" id="resetAccentColorLink"><span>reset</span></a>
                </div>
            </div>

            <div class="advancedThemeColor">
                <label for="ThemeTextColor">Text Color</label>
                <div class="themeColorPickerWrapper">
                    <input type="hidden" id="ThemeTextColorHidden" name="ThemeTextColor" value="null" />
                    <input type="color" id="ThemeTextColor" name="" value="<form:value name="ThemeTextColor"/>" initial-value="<form:value name="ThemeTextColor"/>" />
                    <a href="#" class="themeColorReset" id="resetTextColorLink"><span>reset</span></a>
                </div>
            </div>

            <div class="advancedThemeColor">
                <label for="ThemeLinkColor">Link Color</label>
                <div class="themeColorPickerWrapper">
                    <input type="hidden" id="ThemeLinkColorHidden" name="ThemeLinkColor" value="null" />
                    <input type="color" id="ThemeLinkColor" name="" value="<form:value name="ThemeLinkColor"/>" initial-value="<form:value name="ThemeLinkColor"/>" />
                    <a href="#" class="themeColorReset" id="resetLinkColorLink"><span>reset</span></a>
                </div>
            </div>

            <div class="advancedThemeColor">
                <br>
                <a id="hideAdvancedColorsButton" href="#">Hide advanced color settings</a>
            </div>

            <div>
                <br>
                <a id="advancedColorsButton" href="#">Show advanced color settings</a>
                <br>
            </div>
        </div>


        <br>
        <label>Banner Logo</label>


        <label>Logo Url</label>
        <input type="text" name="LogoUrl" value="<form:value name="LogoUrl"/>" ${longField} maxlength="120" size="40" />
        <c:set var="LogoUrl"><form:error name="LogoUrl"/></c:set>
        <c:if test="${!empty LogoUrlError}">
            <span class="notice"><c:out value="${LogoUrlError}"/></span>
        </c:if>

        <br>
        <img src="<form:value name="LogoUrl"/>" alt="" srcset="">
        <a href="${updateLogoUrl}">Change LOGO</a>
        <br>
        <br>
        <!-- <c:set var="AccentColorError"><form:error name="AccentColor"/></c:set>
        <c:if test="${!empty AccentColorError}">
            <span class="notice"><c:out value="${AccentColorError}"/></span>
        </c:if> -->

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
