<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dump directives -->

<#-- Styles here are temporary; use stylesheets.add() once that's working (see below) -->
<style>
div.dump {
    margin-bottom: .5em;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc; 
    padding-top: .75em;
    padding-bottom: .75em;
}

.dump ul ul {
    margin-left: 1.5em;
}

.dump ul li {
    list-style: none;
}

.dump ul li p {
    margin-bottom: .25em;  
}

.dump ul.sequence li.list_item {
    margin-bottom: 1.25em;
}
</style>

<#--
<#import "lib-dump.ftl" as dumper>
<pre><@dumper.dump dump /></pre> 
-->

<div class="dump">
    <h3>${title}</h3>
    
    <@doDump dump />
</div>

<#macro doDump dump>
    <#if dump?keys?has_content>
        <ul>
            <#list dump?keys as key>
                <li>
                    <#local dumpVal = dump[key] />
                    <p><strong>Variable name:</strong> ${key}</p>
                    
                    <@doMap dumpVal />
                </li>       
            </#list>
        </ul> 
    </#if>
</#macro>

<#macro doMap map>
    <#if map.type?has_content>
        <p><strong>Type:</strong> ${map.type}</p>
        
        <#if map.dateType?has_content>
            <p><strong>Date type:</strong> ${map.dateType}</p>
        </#if>
    </#if>   
    
    <#if map.value?has_content>
        <p>
            <strong>Value:</strong>
            <#local value = map.value>
            <#if value?is_string || value?is_number>${value}
            <#elseif value?is_boolean || value?is_number>${value?string}
            <#elseif value?is_date>${value?string("EEEE, MMMM dd, yyyy hh:mm:ss a zzz")}
            <#elseif value?is_sequence><@doSequence value />
            </#if>
        </p>
    </#if>
</#macro>

<#macro doSequence seq>
    <#if seq?has_content>
        <ul class="sequence">
            <#list seq as item>
                <li class="list_item">Item ${item_index}: <@doMap item /></li>
            </#list>
        </ul>
    </#if>
</#macro>

<#-- This will work after we move stylesheets to Configuration sharedVariables 
${stylesheets.add('<link rel="stylesheet" href="/css/fmdump.css">')}
-->
