<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros used to build the statistical information on the home page -->

<#-- Get the classgroups so they can be used to qualify searches -->
<#macro allClassGroupNames classGroups>
    <#list classGroups as group>
        <#-- Only display populated class groups -->
        <#if (group.individualCount > 0)>
            <li role="listitem"><a href="" title="${group.uri}">${group.displayName?capitalize}</a></li>
        </#if>
    </#list>
</#macro>

<#-- builds the "stats" section of the home page, i.e., class group counts -->
<#macro allClassGroups classGroups>
    <#-- Loop through classGroups first so we can account for situations when all class groups are empty -->
    <#assign selected = 'class="selected" ' />
    <#assign classGroupList>
        <section id="home-stats" class="home-sections" >
            <h4>Statistics</h4>
            <ul id="stats">
                <#assign groupCount = 1>
                <#list classGroups as group>
                    <#if (groupCount > 6) >
                        <#break/>
                    </#if>
                    <#-- Only display populated class groups -->
                    <#if (group.individualCount > 0)>
                        <#-- Catch the first populated class group. Will be used later as the default selected class group -->
                        <#if !firstPopulatedClassGroup??>
                            <#assign firstPopulatedClassGroup = group />
                        </#if>
                        <#if group.displayName != "equipment" && group.displayName != "courses" >
                            <li>
                                <a href="#">
                                    <p  class="stats-count">
                                        <#if (group.individualCount > 10000) >
                                            <#assign overTen = group.individualCount/1000>
                                            ${overTen?round}<span>k</span>
                                        <#elseif (group.individualCount > 1000)>
                                            <#assign underTen = group.individualCount/1000>
                                            ${underTen?string("0.#")}<span>k</span>
                                        <#else>
                                            ${group.individualCount}<span>&nbsp;</span>
                                        </#if>
                                    </p>
                                    <p class="stats-type">${group.displayName?capitalize}</p>
                                </a>
                            </li>
                            <#assign groupCount = groupCount + 1>
                        </#if>
                    </#if>
                    
                </#list>
            </ul>
        </section>
    </#assign>

    <#-- Display the class group browse only if we have at least one populated class group -->
    <#if firstPopulatedClassGroup??>
            ${classGroupList}
    <#else>
        <h3 id="noContentMsg">There is currently no content in the system, or you need to create class groups and assign your classes to them.</h3>
        
        <#if user.loggedIn>
            <#if user.hasSiteAdminAccess>
                <p>You can <a href="${urls.siteAdmin}" title="Manage content">add content and manage this site</a> from the Site Administration page.</p>
            </#if>
        <#else>
            <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
        </#if>
    </#if>
            
</#macro>
