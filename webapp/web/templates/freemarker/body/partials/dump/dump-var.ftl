<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping a template variable -->
 
<div class="var"> 
    <p><strong>Variable name: ${var}</strong></p>
    <#if value??>
        <p><strong>Type:</strong> ${type}</p>
        <p><strong>Value:</strong> ${value}</p>    
        
        <#-- Template model objects -->
        <#if properties?has_content>
            <p><strong>Properties:</strong></p>
            <ul>
                <#list properties?keys as property>
                    <li>${property}: ${properties[property]?html}</li>
                </#list>
            </ul>
        </#if>
        
        <#if methods?has_content>
            <p><strong>Methods:</strong></p>
            <ul>
                <#list methods as method>
                    <li>${method}</li>
                </#list>
            </ul>
        </#if>  
          
    <#else>
        <p><strong>Value:</strong> null</p>  
    </#if>   
</div>

