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

.dump ul li.item {
    margin-bottom: 1.25em;
}

.dump ul li.item .value { 
    margin-left: 1.5em;
}
</style>

<div class="dump">
    <h3>${title}</h3>
    
    <@doDump dump />
</div>

<#macro doDump dump>
    <#if dump?keys?has_content>
        <ul>
            <#list dump?keys as key>
                <li>
                    <p><strong>Variable name:</strong> ${key}</p>                   
                    <@doMap dump[key] />
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
    
    <#if map.type == "Directive" || map.type == "Method">
        <@doHelp map.help! />
    <#else>
        <@doValue map.type map.value! />
    </#if>
    
</#macro>

<#macro doValue type value="">
    <div class="values">
        <#if value??>
            <#if value?is_sequence><@doSequenceValue value type/>
            <#elseif value?is_hash_ex><@doMapValue value />            
            <#else><@doScalarValue type value />
            </#if>
       </#if>             
    </div>
</#macro>

<#macro doSequenceValue seq type>
    <strong>Values:</strong>
    <#if seq?has_content>
        <ul class="sequence">
            <#list seq as item>
                <li class="item">
                    <#if type == "Sequence">
                        Item ${item_index}: 
                        <@valueDiv item />
                    <#else><@doMap item />
                    </#if>
                    
                </li>
            </#list>
        </ul>
     <#else>no values
     </#if>
</#macro>

<#macro doMapValue map>
    <strong>Values:</strong>
    <#if map?has_content>
        <ul class="map">
            <#list map?keys as key>
                <li class="item">
                    ${key} => <@valueDiv map[key] />
                </li>
            </#list>
        </ul>
    <#else>no values
    </#if>
</#macro>

<#macro doScalarValue type value>
    <strong>Value:</strong>
    
    <#if value?is_string || value?is_number>${value}
    <#elseif value?is_boolean>${value?string}
    <#elseif value?is_date>${value?string("EEEE, MMMM dd, yyyy hh:mm:ss a zzz")}
    <#else>no value
    </#if>    
</#macro>

<#macro doHelp help="">
    <#if help?has_content>
        <ul class="help">
            <#list help?keys as key>
                <li>
                    <p><strong>${key}</strong></p>
                    <#--<@valueDiv help[key] />--> 
                </li>
            </#list>        
        </ul>
    </#if>
</#macro>

<#macro valueDiv value>
    <div class="value"><@doMap value /></div>
</#macro>

<#-- This will work after we move stylesheets to Configuration sharedVariables 
${stylesheets.add('<link rel="stylesheet" href="/css/fmdump.css">')}
-->
