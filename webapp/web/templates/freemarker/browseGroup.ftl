<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if message??>
    <p>${message}</p>
<#else>
    <#list classGroups as classGroup>
        <h2>${classGroup.publicName}</h2>
        <#--include "${classGroup.publicName}.ftl"-->
        <ul>
            <#list classGroup.vitroClassList as class>
                <#assign url = "${entityListUri}${class.URI?url}">
                <li><a href="${url}">${class.name}</a> (${class.entityCount})</li>
            </#list>
        </ul>
    </#list>
</#if>