<#macro makeList text>
    <#list text?split(",") as item>
        <#if item.last>
            <#assign item = item?replace("<li", "<li class=\"last\"")>
            ${item}
        </#if>
    </#list>
</#macro>

