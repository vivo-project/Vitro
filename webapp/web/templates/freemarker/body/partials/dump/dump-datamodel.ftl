<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping the template data model -->

<div class="dump datamodel">

    <h4>Template data model dump</h6>
    
    <h5>Variables</h5>
    
    <ul>
        <#list models?keys as key>
            <li><@dump var="${key}" dataModelDump=true /></li> 
        </#list>
    </ul>
    
    <h5>Directives</h5>
    <ul>
        <#list directives as directive>
            <li>${directive}</li>
        </#list> 
    </ul> 
      
</div>