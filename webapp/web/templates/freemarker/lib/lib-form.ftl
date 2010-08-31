<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for form controls -->

<#-- 
    Macro: optionGroups
    
    Output a sequence of option groups with options.
    
    Input is a map of option groups to lists of Option objects.
    
    Usage: <@optionGroups groups=myOptionGroups />
-->
<#macro optionGroups groups>
    <#list groups?keys as group>
        <optgroup label="${group}">
            <@options opts=groups[group] />
        </optgroup>
    </#list>
</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: options
    
    Output a sequence of options.
    
    Input is a list of Option objects.
    
    Usage: <@options opts=myOptions />
-->
<#macro options opts>
    <#list opts as opt>
        <option value="${opt.value}"<#if opt.selected> selected="selected"</#if>>${opt.body}</option> 
    </#list>
</#macro>

<#---------------------------------------------------------------------------->