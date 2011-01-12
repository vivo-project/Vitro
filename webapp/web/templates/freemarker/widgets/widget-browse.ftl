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
    <section id="browse" role="region">
        <h4>Browse</h4>
        
        <ul id="browse-classgroups" role="list">
        <#list vclassGroupList as group>
            <li role="listitem"><a href="${urls.base}/${currentPage}?classgroupUri=${group.uri?url}">${group.publicName} <span class="count-classes">(n)</span></a></li>
        </#list>
        </ul>
    </section>
</#macro>

<#macro classGroup>
    <section id="browse" role="region">
    <h4>Browse</h4>
          
     <div>
       There are ${classes?size} classes in classGroup ${classGroup.publicName}.
       There are ${classGroup.individualCount} individuals in the class group.  
       Classes with and without instances are included.
     </div>-
 
    <nav role="navigation">
        <ul id="foaf-person-childClasses">
            <#list classes as class>
            <li><a href="${urls.base}/${currentPage}?classgroupUri=${classGroup.uri?url}&vclassUri=${class.uri?url}">${class.name}<span class="count-classes"> ${class.individualCount}</span></a></li>
            </#list>
        </ul>
    </nav>
 
</section>

</#macro>

<#macro vclass>
    <section id="browse" role="region">
    <h4>Browse</h4>    
        <div>
            vclass ${class.name} from ${classGroup.publicName}
            This has classGroup, classes, individualsInClass and class.
        </div> 
         
        <ul>
            <#list individualsInClass as ind>
                <li><a href="${urls.base}/individual?uri=${ind.uri?url}">${ind.name}</a></li>
            </#list>
        </section>
</#macro>

<#macro vclassAlpha>
    <section id="browse" role="region">
    <h4>Browse</h4>     
        <div>vclassAlpha is not yet implemented.</div> 
    </section>
</#macro>
