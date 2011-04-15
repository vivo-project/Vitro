<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List class groups, and classes within each group. -->

<#include "classgroups-checkForData.ftl">

<#if (!noData)>
    <section class="siteMap" role="region">
    <div id="isotope-container">
        <#list classGroups as classGroup>
            <#-- Only render classgroups that have at least one class with individuals -->
            <#if (classGroup.individualCount > 0)>
                <#if classGroup.publicName?has_content>               
                  <div class="class-group">
                    <h2>${classGroup.publicName}</h2>
                </#if>
                <ul role="list">
                    <#list classGroup.classes as class> 
                        <#-- Only render classes with individuals -->
                        <#if (class.individualCount > 0)>
                            <li role="listitem"><a href="${class.url}">${class.name}</a> (${class.individualCount})</li>
                        </#if>
                    </#list>
                </ul>
                </div>
            </#if>
        </#list>
      </div> <!-- end isotope-container -->
    </section>


${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/isotope/jquery.isotope.min.js"></script>')}
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
