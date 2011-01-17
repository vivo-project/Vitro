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
    <h3>There is currently no content in the system</h3>
    
    <#if !user.loggedIn>
        <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
    </#if>
</#assign>