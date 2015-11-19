<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign populatedClassGroups = 0 />

<#list classGroups as classGroup>
    <#-- Check to see if any of the class groups have individuals -->
    <#if (classGroup.individualCount > 0)>
        <#assign populatedClassGroups = populatedClassGroups + 1 />
    </#if>
</#list>
<#if (populatedClassGroups == 0)>
    <#assign noData = true />
<#else>
    <#assign noData = false />
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
    
    <h3>${i18n().no_content_create_groups_classes}</h3>
    
    <#if user.loggedIn && user.hasSiteAdminAccess>
        <p>${i18n().you_can} <a href="${urls.siteAdmin}" title="${i18n().add_content_manage_site}">${i18n().add_content_manage_site}</a> ${i18n().from_site_admin_page}</p>
    </#if>
</#assign>