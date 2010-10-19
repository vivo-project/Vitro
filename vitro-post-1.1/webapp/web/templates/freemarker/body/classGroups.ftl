<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List class groups, and classes within each group. -->

<div class="siteMap">
    <#list classGroups as classGroup>
        <h2>${classGroup.publicName}</h2>
        <ul>
            <#list classGroup.classes as class> 
                <li><a href="${class.url}">${class.name}</a> (${class.individualCount})</li>
            </#list>
        </ul>
    </#list>
</div>
