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
                <p>Try <a title="Rebuild the search index for this site" href="${urls.base}/SearchIndex">rebuilding the search index</a>.</p>
            </span>
        </#if>
    <#else>
        <span class="contentNote">
            <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
        </span>
    </#if>
    
    <h3>There is currently no ${page.title} content in the system</h3>
    <#if user.loggedIn && user.hasSiteAdminAccess>
        <p>You can <a href="${urls.siteAdmin}" title="Manage content">add content and manage this site</a> from the Site Administration page.</p>
    </#if>
    
    <p>You can browse all of the public content currently in the system using the <a href="${urls.index}" title="browse all content">index page</a>.</p>
</#assign>