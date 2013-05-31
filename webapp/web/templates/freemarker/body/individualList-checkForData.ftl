<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if individuals?has_content>
    <#assign noData = false />
<#else>
    <#assign noData = true />
</#if>

<#assign noDataNotification>    
    <#if user.loggedIn>
        <#if user.authorizedToRebuildSearchIndex>
            <span class="contentNote">
                <h4>${i18n().expecting_content}</h4>
                <p><a title="${i18n().try_rebuilding_index}" href="${urls.base}/SearchIndex">${i18n().try_rebuilding_index}</a>.</p>
            </span>
        </#if>
    <#else>
        <span class="contentNote">
            <p>${i18n().please} <a href="${urls.login}" title="${i18n().login_to_manage_site}">${i18n().log_in}</a> ${i18n().to_manage_content}</p>
        </span>
    </#if>
    
    <h3>${i18n().no_content_in_system(title)}</h3>
    
    <#if user.loggedIn && user.hasSiteAdminAccess>
        <p>${i18n().you_can} <a href="${urls.siteAdmin}" title="${i18n().add_content_manage_site}">${i18n().add_content_manage_site}</a> ${i18n().from_site_admin_page}</p>
    </#if>
</#assign>