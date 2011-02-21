<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping a template variable -->
 
<div class="var"> 
    <p><strong>Variable name: <em>${var}</em></strong></p>
    <#if value??>
        <p><strong>Type:</strong> ${type}</p>
        <div><strong>Value:</strong> ${value}</div>        
    <#else>
        <p>Variable is undefined in the data model</p>
    </#if>   
</div>

