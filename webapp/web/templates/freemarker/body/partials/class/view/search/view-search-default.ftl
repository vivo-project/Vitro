<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default individual search view -->

<a href="${individual.profileUrl}">${individual.name}</a>
<#if individual.moniker?has_content> | ${individual.moniker}</#if>

<#if individual.description?has_content>
    <div class="searchFragment">${individual.description}</div>
</#if>