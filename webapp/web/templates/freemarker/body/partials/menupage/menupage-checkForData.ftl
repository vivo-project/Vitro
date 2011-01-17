<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#list vClassGroup as vClass>
    <#assign populatedClasses = 0 />
    <#-- Check to see if any of the classes in this class group have individuals -->
    <#if (vClass.entityCount > 0)>
        <#assign populatedClasses = populatedClasses + 1 />
    </#if>
</#list>
<#if (populatedClasses == 0)>
    <#assign noData = true />
<#else>
    <#assign noData = false />
</#if>

<#assign noDataNotification>
    <h3>There are currently no ${page.title?lower_case} in the system</h3>
    
    <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
    
    <p>You can browse all of the public content currently in the system using the <a href="${urls.index}" title="browse all content">index page</a>.</p>
</#assign>