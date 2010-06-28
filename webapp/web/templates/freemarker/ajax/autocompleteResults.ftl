<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for autocomplete results. -->

<#--
<#import "/lib/json.ftl" as json>
<@json.array results />
-->

[ 
<#if results??>
    <#list results as result>
        { "label": "${result.label}", "uri": "${result.uri}" }<#if result_has_next>,</#if>       
    </#list>
</#if>
]
