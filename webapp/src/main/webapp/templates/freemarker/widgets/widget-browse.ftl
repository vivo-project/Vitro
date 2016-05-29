<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Browse widget -->

<#macro assets>
  <#-- 
   Are there stylesheets or scripts needed? 
   ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/browse.css" />')}
   ${scripts.add('<script type="text/javascript" src="${urls.base}/js/browse.js"></script>'}
   -->        
</#macro>

<#macro allClassGroups>
    <section id="browse" role="region">
        <h4>${i18n().browse_capitalized}</h4>
        
        <ul id="browse-classgroups" role="list">
        <#list vclassGroupList as group>
            <#if (group.individualCount > 0)>
                <li role="listitem"><a href="${urls.currentPage}?classgroupUri=${group.uri?url}" title="${i18n().group_name}">${group.displayName?capitalize} <span class="count-classes">(${group.individualCount})</span></a></li>
            </#if>
        </#list>
        </ul>
    </section>
    
    <#-- <@classGroup /> -->
</#macro>

<#macro classGroup>
    <section id="browse" role="region">
        <h4>${i18n().browse_capitalized}</h4>
        
         <section id="browse-classes" role="navigation">
             <nav>
                 <ul id="classes-in-classgroup" role="list">
                     <#list classes as class>
                        <#if (class.individualCount > 0)>
                            <li role="listitem"><a href="${urls.currentPage}?classgroupUri=${classGroup.uri?url}&vclassUri=${class.uri?url}" title="${i18n().class_name}">${class.name} <span class="count-individuals"> (${class.individualCount})</span></a></li>
                        </#if>
                     </#list>
                 </ul>
             </nav>
        </section>
    </section>
</#macro>

<#macro vclass>
    <section id="browse" role="region">
    <h4>${i18n().browse_capitalized}</h4>    
        <div>
            vclass ${class.name} from ${classGroup.displayName}
            This has classGroup, classes, individualsInClass and class.
        </div> 
         
        <ul>
            <#list individualsInClass as ind>
                <li><a href="${urls.base}/individual?uri=${ind.uri?url}" title="${i18n().individual_nam}">${ind.name}</a></li>
            </#list>
        </section>
</#macro>

<#macro vclassAlpha>
    <section id="browse" role="region">
    <h4>${i18n().browse_capitalized}</h4>     
        <div>${i18n().vclassAlpha_not_implemented}</div> 
    </section>
</#macro>
