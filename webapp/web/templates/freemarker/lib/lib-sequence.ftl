<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for working with Freemarker sequences -->

<#-- Macro join: join array elements with the specified glue string. -->

<#macro join arr glue=", "><#compress>
    <#assign count = 0>
    <#-- This is VERY ugly: adding any formatting here inserts extra spaces into the output, even with the compress 
    directive. The compress directive is also necessary, though. Seems counter to the documentation at
    http://freemarker.org/docs/dgui_misc_whitespace.html -->
    <#list arr as el><#if el?has_content><#if (count > 0)>${glue}</#if>${el}<#assign count = count+1></#if></#list>
</#compress></#macro>

