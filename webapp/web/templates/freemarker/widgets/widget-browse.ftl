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
    <li><a href="${urls.base}${urlMapping}?classgroupUri=${group.uri?url}">${group.publicName}</a></li>
 </#list>
 </ul>
</section>

</#macro>

<#macro classGroup>
<section>
 <h2>Macro classGroup from widget-browse.ftl</h2>  
 <div>
   There are ${classes?size} classes in classGroup ${classGroup.publicName}.  
   Only classes with instances are included.
 </div>
 <ul>
 <#list classes as class>
    <li><a href="${urls.base}${urlMapping}?classgroupUri=${classGroup.uri?url}&vclassUri=${class.uri?url}">${class.name}</a></li> 
 </#list>
 </ul> 
</section>

</#macro>

<#macro vclass>
<section>
 <h2>vclass ${class.name} from ${classGroup.publicName}</h2>
  This has classGroup, classes, and class.  It doesn't yet have a list of individuals in the class.
</section>
</#macro>

<#macro vclassAlpha>
<section role="region">
 <h2>vclassAlpha</h2> 
</section>
</#macro>
