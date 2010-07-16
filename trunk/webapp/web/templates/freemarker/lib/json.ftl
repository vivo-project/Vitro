<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for json output -->

<#macro array data>
[ 
<#if data??>
    <#list data as obj>   
        ${obj.json}<#if obj_has_next>,</#if>
    </#list>
</#if>
]
</#macro>