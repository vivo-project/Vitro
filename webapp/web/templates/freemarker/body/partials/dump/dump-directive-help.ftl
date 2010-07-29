<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping directive help -->

<p><strong>Usage:</strong> ${usage!}</p>

<#if comments??>
    <p><strong>Comments:</strong> ${comments}</p>     
</#if>      

<h6>Parameters:</h6>
<#if params?? && params?keys?has_content>
    <ul>
        <#list params?keys as param>
            <li><strong>${param}:</strong> ${params[param]}</li>   
        </#list>
    </ul>
<#else>
    <p>none</p>
</#if>

<h6>Examples:</h6>
<#if examples??>
    <ul>
        <#list examples as example>
            <li>${example}</li>
        </#list>   
    </ul>
</#if>