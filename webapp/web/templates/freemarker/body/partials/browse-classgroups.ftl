<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Browse classgroups on the home page. Could potentially become a widget -->

<@allClassGroups />

<#macro allClassGroups>
    <section id="browse" role="region">
        <h4>Browse by</h4>
        
        <ul id="browse-classgroups" role="list">
        <#assign selected = 'class="selected" ' />
        <#list vClassGroups as group>
            <#-- Only display populated class groups -->
            <#if (group.individualCount > 0)>
                <#-- Catch the first populated class group. Will be used later as the default selected class group -->
                <#if !firstPopulatedClassGroup??>
                    <#assign firstPopulatedClassGroup = group />
                </#if>
                <#-- Remove "index.jsp" from URL (should verify with RY if this can be taken care of in the controller) -->
                <#if currentPage == "index.jsp">
                    <#assign currentPage = "" />
                </#if>
                <#-- Determine the active (selected) group -->
                <#assign activeGroup = "" />
                <#if !classGroup??>
                    <#if group_index == 0>
                        <#assign activeGroup = selected />
                    </#if>
                <#elseif classGroup.uri == group.uri>
                    <#assign activeGroup = selected />
                </#if>
                <li role="listitem"><a ${activeGroup}href="${urls.base}/${currentPage}?classgroupUri=${group.uri?url}#browse">${group.publicName?capitalize} <span class="count-classes">(${group.individualCount})</span></a></li>
            </#if>
        </#list>
        </ul>
        
        <#-- If requesting the home page without any additional URL parameters, select the first populated class group-->
        <#assign defaultSelectedClassGroup = firstPopulatedClassGroup />
        
        <section id="browse-classes" role="navigation">
            <nav>
                <ul id="classgroup-list" role="list">
                    <#if classes??>
                        <#-- We don't need to send parameters because the data we need is delivered as template variables -->
                        <@classesInClassgroup />
                    <#else>
                        <#-- We need to pass the data to the macro because the only template variable provided by default is vClassGroups -->
                        <@classesInClassgroup classes=defaultSelectedClassGroup.classes classGroup=defaultSelectedClassGroup />
                    </#if>
                </ul>
            </nav>
        </section> <!-- #browse-classes -->
    </section> <!-- #browse -->
</#macro>

<#macro classesInClassgroup classes=classes classGroup=classGroup>
     <#list classes as class>
        <#if (class.individualCount > 0)>
            <li role="listitem"><a href="${urls.base}/individuallist?vclassId=${class.uri?url}">${class.name} <span class="count-individuals"> (${class.individualCount})</span></a></li>
        </#if>
     </#list>
</#macro>