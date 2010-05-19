<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List class groups, and classes within each group. -->

<#if message??>
    <p>${message}</p>
<#else>
    <div class="siteMap">
        <#list classGroups as classGroup>
            <h2>${classGroup.publicName}</h2>
            <ul>
                <#list classGroup.classes as class> 
                    <li><a href="${class.url}">${class.name}</a> (${class.entityCount})</li>
                </#list>
            </ul>
        </#list>
    </div>
</#if>
