<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Browse widget -->

<#macro assets>
  <#-- 
   Are there stylesheets or scripts needed? 
   ${stylesheets.add("/css/browse.css")} 
   ${scripts.add("/js/browse.js")}
   -->        
</#macro>
        
<#macro allClassGroups>
    <section>
        <h2>Macro allClassGroups from widget-browse.ftl</h2>

        <ul>
        <#list vclassGroupList as group>
            <li><a href="${urls.base}/${currentPage}?classgroupUri=${group.publicName?url}">${group.publicName}</a></li>
        </#list>
        </ul>
    </section>
</#macro>

<#macro classGroup>
<section>
     <#--<h2>Macro classGroup from widget-browse.ftl</h2>  
     <div>
       There are ${classes?size} classes in classGroup ${classGroup.publicName}.  
       Only classes with instances are included.
     </div>-->
 
    <nav role="navigation">
        <ul id="foaf-person-childClasses">
            <#list classes as class>
            <li><a href="${urls.base}/${currentPage}?classgroupUri=${classGroup.publicName?url}&vclassUri=${class.uri?url}">${class.name}<span class="count-classes"> ${class.individualCount}</span></a></li>
            </#list>
        </ul>
    </nav>
 
    <#--<ul>
    <#list classes as class>
        <li><a href="${urls.base}/${currentPage}?classgroupUri=${classGroup.publicName?url}&vclassUri=${class.uri?url}">${class.name}</a> ${class.individualCount}</li> 
    </#list>
    </ul>-->
</section>

</#macro>

<#macro vclass>
    <section>
        <h2>vclass ${class.name} from ${classGroup.publicName}</h2>
        This has classGroup, classes, individualsInClass and class. 
         
        <ul>
            <#list individualsInClass as ind>
                <li><a href="${urls.base}/individual?uri=${ind.uri?url}">${ind.name}</a></li>
            </#list>
        </section>
</#macro>

<#macro vclassAlpha>
    <section role="region">
        <h2>vclassAlpha</h2> 
    </section>
</#macro>
