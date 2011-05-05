<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for vitro:primaryLink and vitro:additionalLink -->

<@showLink statement />

<#-- Use a macro to keep variable assignments local; otherwise the values carry over to the
     next statement -->
<#macro showLink statement>
    <#if statement.anchor??>
        <#local linkText = statement.anchor>
        <#local attr = "rel=\"${statement.property} ${namespaces.vitro}linkAnchor\"">
    <#else>
        <#local linkText = "${statement.linkName} (no anchor text provided for link)">
        <#local attr = "property=\"${statement.property}\"">
    </#if>

    <#if statement.url??>
        <a href="${statement.url}" ${attr}>${linkText}</a> 
    <#else>
        <a href="${profileUrl(statement.link)}">${linkText}</a> (no url provided for link)    
    </#if>
</#macro>

