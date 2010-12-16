<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for working with Freemarker sequences -->

<#-- Macro join: join array elements with the specified glue string. -->

<#macro join arr glue=", ">
    <#assign count = 0>
    <#-- Freemarker is very finicky about whitespace here. The compress directives and formatting 
    here work; do not alter them. -->
    <#list arr as el>
        <#if el?has_content>
            <#compress>
            <#if (count > 0)>${glue}</#if>
            ${el}
            </#compress>
            <#assign count = count+1>
        </#if>
    </#list>
</#macro>

