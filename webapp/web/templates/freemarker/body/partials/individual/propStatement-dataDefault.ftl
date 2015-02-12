<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- VIVO-specific default data property statement template. 
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->

<@showStatement statement />

<#macro showStatement statement>
    <#assign theValue = statement.value />
    <#if theValue?contains("<ul>") >
        <#assign theValue = theValue?replace("<ul>","<ul class='tinyMCEDisc'>") />
    </#if>
    <#if theValue?contains("<ol>") >
        <#assign theValue = theValue?replace("<ol>","<ol class='tinyMCENumeric'>") />
    </#if>
    <#if theValue?contains("<p>") >
        <#assign theValue = theValue?replace("<p>","<p style='margin-bottom:.6em'>") />
    </#if>
    ${theValue}
</#macro>





