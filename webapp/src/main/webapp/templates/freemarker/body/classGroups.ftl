<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- List class groups, and classes within each group. -->

<#include "classgroups-checkForData.ftl">

<#if (!noData)>
    <section class="siteMap" role="region">
        <div id="isotope-container">
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
 
                    <div class="class-group">             
                        <h2>${classGroup.displayName}</h2>
                        <ul role="list">
                            <#list classGroup.classes as class> 
                                <#-- Only render populated classes -->
                                <#if (class.individualCount > 0)>
                                    <li role="listitem"><a href="${class.url}" title="${i18n().class_name}">${class.name}</a> (${class.individualCount})</li>
                                <#assign classCount = classCount + 1 >
                                </#if>
                                <#if (classCount > 34) && (classCount < groupSize) && !splitGroup >
                                   <#assign splitGroup = true >
                                   </ul></div>
                                   <div class="class-group">
                                       <h2>${classGroup.displayName} (${i18n().continued})</h2>
                                          <ul role="list">
                                </#if>
                            </#list>
                        </ul>
                    </div> <!-- end class-group -->
                </#if>
            </#list>
          </div> <!-- end isotope-container -->
    </section>

    ${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/isotope/jquery.isotope.min.js"></script>')}
    <script>
        var initHeight = $("#isotope-container").height();
        initHeight = (initHeight/2.05) ;
        $("#isotope-container").css("height",initHeight + "px");
    </script>
    <script>
        $('#isotope-container').isotope({
          // options
          itemSelector : '.class-group',
          layoutMode : 'fitColumns'
        });
    </script>
<#else>
    ${noDataNotification}
</#if>
