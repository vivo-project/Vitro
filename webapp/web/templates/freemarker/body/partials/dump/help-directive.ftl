<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying directive help -->

<div class="directive">
    <p><strong>Directive name:</strong> ${name}</p> 
    
    <#if effect??>
        <p><strong>Effect:</strong> ${effect}</p>
        
        <#if comments??>
            <p><strong>Comments:</strong> ${comments}</p>     
        </#if>      
        
        <p><strong>Parameters: </strong>
        <#if params?? && params?keys?has_content>
            </p>
            <ul>
                <#list params?keys as param>
                    <li><strong>${param}:</strong> ${params[param]}</li>   
                </#list>
            </ul>
        <#else>
            none</p>
        </#if>
        <br />
        
        <p><strong>Examples:</strong></p>
        <#if examples??>
            <ul>
                <#list examples as example>
                    <li>${example}</li>
                </#list>   
            </ul>
        </#if>
        
    <#else>
        <p>No help available for this directive.</p>
    </#if>
</div>