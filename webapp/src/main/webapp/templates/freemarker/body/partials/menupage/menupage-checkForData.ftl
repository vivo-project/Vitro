<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign populatedClasses = 0 />

<#if vClassGroup??> <#-- the controller may put a null -->
    <#list vClassGroup as vClass>
        <#-- Check to see if any of the classes in this class group have individuals -->
        <#if (vClass.entityCount > 0)>
            <#assign populatedClasses = populatedClasses + 1 />
        </#if>
    </#list>
</#if>

<#if (populatedClasses == 0)>
    <#assign noData = true />
<#else>
    <#assign noData = false />
</#if>

<#assign noDataNotification>
    <#if user.loggedIn>
        <#if user.authorizedToRebuildSearchIndex>
            <span class="contentNote">
                <h4>Expecting content?</h4>
                <p><a title="${i18n().try_rebuilding_index}" href="${urls.base}/SearchIndex">${i18n().try_rebuilding_index}</a>.</p>
            </span>
        </#if>
    <#else>
        <span class="contentNote">
            <p>${i18n().please} <a href="${urls.login}" title="${i18n().login_to_manage_site}">${i18n().log_in}</a> ${i18n().to_manage_content}</p>
        </span>
    </#if>
    
    <h3>${i18n().no_content_in_system(page.title)}</h3>
    <#if user.loggedIn && user.hasSiteAdminAccess>
        <p>${i18n().you_can} <a href="${urls.siteAdmin}" title="Manage content">${i18n().add_content_manage_site}</a> ${i18n().from_site_admin_page}</p>
    </#if>
    
    <p>${i18n().browse_all_public_content} <a href="${urls.index}" title="${i18n().browse_all_content}">${i18n().index_page}</a>.</p>
</#assign>