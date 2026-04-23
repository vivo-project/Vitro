<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- List class groups, and classes within each group. -->

<#include "classgroups-checkForData.ftl">

<#if (!noData)>
    <section class="siteMap" role="region">
        <ul id="wookmark-container" style="position: relative;">
            <#list classGroups as classGroup>
                <#assign groupSize = 0 >
                <#assign classCount = 0 >
                <#assign splitGroup = false>
                <#-- Only render classgroups that have at least one populated class -->
                <#if (classGroup.individualCount > 0)>

                    <#list classGroup.classes as class>
                        <#if (class.individualCount > 0)>
                            <#assign groupSize = groupSize + 1 >
                        </#if>
                    </#list>

                    <li class="wookmark-group" style="float: left;">
                    <h2>${classGroup.displayName}</h2>
                        <ul class="class-group-list" role="list">
                            <#list classGroup.classes as class>
                                <#-- Only render populated classes -->
                                <#if (class.individualCount > 0)>
                                    <li role="listitem"><a href="${class.url}" title="${i18n().class_name}">${class.name}</a> (${class.individualCount})</li>
                                <#assign classCount = classCount + 1 >
                                </#if>
                                <#if (classCount > 34) && (classCount < groupSize) && !splitGroup >
                                   <#assign splitGroup = true >
                                   </ul></li>
                                   <li class="class-group" style="float: left; width: 200px;">
                                       <h2>${classGroup.displayName} (${i18n().continued})</h2>
                                          <ul role="list">
                                </#if>
                            </#list>
                        </ul>
                    </li> <!-- end class-group -->
                </#if>
            </#list>
          </ul> <!-- end isotope-container -->
    </section>

    ${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/wookmark.min.js"></script>')}
    <script>
        $('#wookmark-container').wookmark({
            itemWidth: 290,
            flexibleWidth: "30%"
        });
    </script>
<#else>
    ${noDataNotification}
</#if>
