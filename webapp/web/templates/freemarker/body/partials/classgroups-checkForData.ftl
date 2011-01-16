<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#list classGroups as classGroup>
    <#assign populatedClassGroups = 0 />
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
    
    <p>Please <a href="${urls.login}" title="log in to manage this site">login</a> to manage content.</p>
</#assign>