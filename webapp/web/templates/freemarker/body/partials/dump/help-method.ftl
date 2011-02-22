<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying directive help -->

<div class="directive">
    <p><strong>Method name:</strong> ${name}</p> 
    
    <#if returnValue??>
        <p><strong>Return value:</strong> ${returnValue}</p>
        
        <#if comments??>
            <p><strong>Comments:</strong> ${comments}</p>     
        </#if>      
        
        <p><strong>Parameters:</strong>
        <#if params??>
            </p>
            <ol>
                <#list params as param>
                    <li>${param}</li>   
                </#list>
            </ol>
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
        <p>No help available for this method.</p>
    </#if>
</div>