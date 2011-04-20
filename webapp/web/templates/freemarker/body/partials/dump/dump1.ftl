<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dump directives -->

<#-- Styles here are temporary; move to css file once stylesheets.add() works -->
<style>
ul.dump {
    padding-top: .75em;
    padding-bottom: .75em;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc; 
    margin-bottom: .5em;
}

ul.dump li p {
    margin-bottom: .5em;
}
</style>

<#-- <pre><@dumper.dump dump /></pre> -->

<#if dump?keys?has_content>
    <ul class="dump">
        <#list dump?keys as key>
            <li>
                <#assign value = dump[key] />
                <p><strong>Variable name:</strong> ${key}</p>
                <#if value.type??><p><strong>Type:</strong> ${value.type}</p></#if>   
                <#-- <p><strong>Value:</strong> ${value.value}</p> -->
                <#-- What to do here depends on time. Test either ${var.type} or ${var.value} -->
                <#-- <p><strong>Value:</strong> ${var.value}</p> -->
            </li>       
        </#list>
    </ul> 
</#if>

<#-- This will work after we move stylesheets to Configuration sharedVariables 
${stylesheets.add('<link rel="stylesheet" href="/css/fmdump.css">')}
-->