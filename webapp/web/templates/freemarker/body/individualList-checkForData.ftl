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
                <h4>Expecting content?</h4>
                <p>Try <a title="Rebuild the search index for this site" href="${urls.base}/SearchIndex">rebuilding the search index</a>.</p>
            </span>
        </#if>
    <#else>
        <span class="contentNote">
            <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
        </span>
    </#if>
    
    <h3>There is currently no ${title} content in the system</h3>
    
    <#if user.loggedIn && user.hasSiteAdminAccess>
        <p>You can <a href="${urls.siteAdmin}" title="Manage content">add content and manage this site</a> from the Site Administration page.</p>
    </#if>
</#assign>