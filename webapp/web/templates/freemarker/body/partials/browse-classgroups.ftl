<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Browse class groups on the home page. Could potentially become a widget -->

${stylesheets.add("/css/browseClassGroups.css")}

<#macro allClassGroups classGroups>
    <#-- Loop through classGroups first so we can account for situations when all class groups are empty -->
    <#assign selected = 'class="selected" ' />
    <#assign classGroupList>
        <#list classGroups as group>
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
                <li role="listitem"><a data-uri="${group.uri}" ${activeGroup}href="${urls.base}/${currentPage}?classgroupUri=${group.uri?url}#browse" title="Browse ${group.publicName?capitalize}">${group.publicName?capitalize} <span class="count-classes">(${group.individualCount})</span></a></li>
            </#if>
        </#list>
    </#assign>
    
    <#-- Display the class group browse only if we have at least one populated class group -->
    <#if firstPopulatedClassGroup??>
        <section id="browse" role="region">
            <h4>Browse by</h4>
            
            <ul id="browse-classgroups" role="list">
                ${classGroupList}
            </ul>
            
            <#-- If requesting the home page without any additional URL parameters, select the first populated class group-->
            <#assign defaultSelectedClassGroup = firstPopulatedClassGroup />
            
            <section id="browse-classes" role="navigation">
                <nav>
                    <ul id="classes-in-classgroup" class="vis" role="list">
                        <#if classes??>
                            <#-- We don't need to send parameters because the data we need is delivered as template variables -->
                            <@classesInClassgroup />
                        <#else>
                            <#-- We need to pass the data to the macro because the only template variable provided by default is classGroups -->
                            <@classesInClassgroup classes=defaultSelectedClassGroup.classes classGroup=defaultSelectedClassGroup />
                        </#if>
                    </ul>
                </nav>
                <#if classes??>
                    <#-- We don't need to send parameters because the data we need is delivered as template variables -->
                    <@visualGraph />
                <#else>
                    <#-- We need to pass the data to the macro because the only template variable provided by default is classGroups -->
                    <@visualGraph classes=defaultSelectedClassGroup.classes classGroup=defaultSelectedClassGroup />
                </#if>
            </section> <!-- #browse-classes -->
        </section> <!-- #browse -->
        
        <#----------------------------------------------------------------------------------
        requestedPage is currently provided by FreemarkerHttpServlet. Should this be moved
        to PageController? Maybe we should have Java provide the domain name directly
        instead of the full URL of the requested page? Chintan was also asking for a
        template variable with the domain name for an AJAX request with visualizations.
        ------------------------------------------------------------------------------------>
        <#assign domainName = requestedPage?substring(0, requestedPage?index_of("/", 7)) />

        <script type="text/javascript">
            var browseData = {
                baseUrl: '${domainName + urls.base}',
                dataServiceUrl: '${domainName + urls.base}/dataservice?getVClassesForVClassGroup=1&classgroupUri=',
                defaultBrowseClassGroupUri: '${firstPopulatedClassGroup.uri!}'
            };
        </script>

        ${scripts.add("/js/browseClassGroups.js")}
    <#else>
        <#-- Would be nice to update classgroups-checkForData.ftl with macro so it could be used here as well -->
        <#-- <#include "classgroups-checkForData.ftl"> -->
        <h3>There is currently no content in the system</h3>
        
        <#if !user.loggedIn>
            <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
        </#if> 
    </#if>
</#macro>


<#macro classesInClassgroup classes=classes classGroup=classGroup>
     <#list classes as class>
        <#if (class.individualCount > 0)>
            <li role="listitem"><a href="${urls.base}/individuallist?vclassId=${class.uri?url}" title="Browse all ${class.name} content">${class.name} <span class="count-individuals"> (${class.individualCount})</span></a></li>
        </#if>
     </#list>
</#macro>


<#macro pieChart classes=classes classGroup=classGroup>
    <section id="visual-graph" role="region">
        <table class="graph-data">
            <#list classes?sort_by("individualCount") as class>
                <#assign countPercentage = (class.individualCount / classGroup.individualCount) * 100 />
                <#if (class.individualCount > 0 && countPercentage?round > 0)>
                    <tr>
                        <th>${class.name}</th>
                        <td>${countPercentage?round}%</td>
                    </tr>
                </#if>
            </#list>
        </table>
          
        <section id="pieViz" role="region"></section>
    </section>
    
    ${scripts.add("/themes/wilma/js/jquery_plugins/raphael/raphael.js", "/themes/wilma/js/jquery_plugins/raphael/pie.js")}
</#macro>


<#macro visualGraph classes=classes classGroup=classGroup>
    <section id="visual-graph" class="barchart" role="region">
        <#-- Will be populated dynamically via AJAX request -->
    </section>
    
    ${scripts.add("/js/raphael/raphael.js", "/js/raphael/g.raphael.js", "/js/raphael/g.bar.js")}
</#macro>