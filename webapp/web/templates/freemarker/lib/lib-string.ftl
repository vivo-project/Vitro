<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros and functions for string manipulation -->

<#function camelCase str>
    <#return str?capitalize?replace(" ", "")?uncap_first>
</#function>

<#function unCamelCase str>
    <#local str = str?replace("([a-z])([A-Z])", "$1 $2", "r")>
    <#local words = str?split(" ")>
    <#local out = "">
    <#list words as word>
        <#local out = out + " " + word?uncap_first>
    </#list>
    <#return out?trim>
</#function>