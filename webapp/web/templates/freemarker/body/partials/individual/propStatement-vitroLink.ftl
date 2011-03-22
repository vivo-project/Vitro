<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for vitro:primaryLink and vitro:additionalLink -->

<#assign linkText>
    <#if statement.anchor??>${statement.anchor}
    <#else>${statement.linkName} (no anchor text provided for link)
    </#if>    
</#assign>

<#if statement.url??>
    <a href="${statement.url}">${linkText}</a> 
<#else>
    ${linkText} (no url provided for link)    
</#if>