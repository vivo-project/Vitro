<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for form controls -->

<#-- 
    Macro: optionGroups
    
    Output: a sequence of option groups with options.
    
    Input: a map of option groups to lists of Option objects.
    
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
    
    Output: a sequence of options.
    
    Input: a list of Option objects.
    
    Usage: <@options opts=myOptions />
-->
<#macro options opts>
    <#list opts as opt>
        <option value="${opt.value}"<#if opt.selected> selected="selected"</#if>>${opt.body}</option> 
    </#list>
</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: hiddenInputs
    
    Output hidden inputs from a map of names to values.
    
    Input: a map of strings (names) to strings (values). May be null.
    
    Usage: <@hiddenInputs inputs />
-->
<#macro hiddenInputs inputs="">
    <#if inputs?has_content>
        <#list inputs?keys as name>
            <input type="hidden" name="${name}" value="${inputs[name]}" />
        </#list>
    </#if>
</#macro>