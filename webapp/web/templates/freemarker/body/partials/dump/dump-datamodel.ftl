<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping the template data model -->

<div class="dump datamodel">

    <h3>Data Model Dump for Template <em>${containingTemplate}</em></h3>
    
    <h4>VARIABLES</h4>
    
    <ul>
        <#list models?keys as key>
            <li><@dump var="${key}" dataModelDump=true /></li> 
        </#list>
    </ul>
    
    <h4>DIRECTIVES</h4>
    
    <ul>
        <#list directives?keys as directive>
            <li>${directives[directive]}</li>
        </#list> 
    </ul> 
      
</div>