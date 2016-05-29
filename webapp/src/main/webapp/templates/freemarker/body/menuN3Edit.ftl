<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div id="navigationManagement">
    <h2>${i18n().menu_management}</h2>
    
    <#if errorMessage??>
        <div id="errorAlert">
            <img src="../images/iconAlert.png" alt="${i18n().error_alert_icon}" height="31" width="32">
            <p>${errorMessage}</p>
        </div>
    </#if>
    
    <#if message??>
        <p>${message}</p>
    </#if>
    
    <#if menuN3??>
        <form class="" action="${urls.currentPage}" method="post">
            <label for="navigatioN3">${i18n().setup_navigation_menu}</label>
            <textarea name="navigationN3" id="navigationN3" cols="45" rows="40" class="maxWidth">
                ${menuN3}<#t><#-- The trim directive here is to trim leading and trailing white-space -->
            </textarea><#lt><#-- This directive trims only leading white-space -->
            <input name="submit" id="submit" value="${i18n().save_button}" type="submit"/> or <a href="${urls.base}${cancelUrl}" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
        </form>
    </#if>
</div>