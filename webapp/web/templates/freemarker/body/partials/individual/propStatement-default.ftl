<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default object property statement template -->

<a href="${profileUrl(statement.object)}" rel="${property.curie}">
    <#if statement.label?has_content>
        <span property="rdfs:label">${statement.label}</span><#t>
    <#else>${statement.localname}<#t>
    </#if>
</a> ${statement.moniker!} 