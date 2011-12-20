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
                <h4>Expecting content?</h4>
                <p>Try <a title="Rebuild the search index for this site" href="${urls.base}/SearchIndex">rebuilding the search index</a>.</p>
            </span>
        </#if>
    <#else>
        <span class="contentNote">
            <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
        </span>
    </#if>
    
    <h3>There is currently no content in the system, or you need to create class groups and assign your classes to them.</h3>
    
    <#if user.loggedIn && user.hasSiteAdminAccess>
        <p>You can <a href="${urls.siteAdmin}" title="Manage content">add content and manage this site</a> from the Site Administration page.</p>
    </#if>
</#assign>