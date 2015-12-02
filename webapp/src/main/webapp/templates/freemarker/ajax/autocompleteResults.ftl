<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for autocomplete results. -->

<#--
<#import "lib-json.ftl" as json>
<@json.array results />
-->

[ 
<#if results??>
    <#list results as result>
        <#-- result.jsonLabel and result.jsonUri are already quoted -->
        { "label": ${result.jsonLabel}, "uri": ${result.jsonUri} }<#if result_has_next>,</#if>       
    </#list>
</#if>
]
