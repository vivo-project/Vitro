<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List class groups, and classes within each group. -->

<#include "classgroups-checkForData.ftl">

<#if (!noData)>
    <div class="siteMap">
        <#list classGroups as classGroup>
            <#-- Only render classgroups that have at least one class with individuals -->
            <#if (classGroup.individualCount > 0)>
                <h2>${classGroup.publicName}</h2>
                <ul>
                    <#list classGroup.classes as class> 
                        <#-- Only render classes with individuals -->
                        <#if (class.individualCount > 0)>
                            <li><a href="${class.url}">${class.name}</a> (${class.individualCount})</li>
                        </#if>
                    </#list>
                </ul>
            </#if>
        </#list>
    </div>
<#else>
    ${noDataNotification}
</#if>
