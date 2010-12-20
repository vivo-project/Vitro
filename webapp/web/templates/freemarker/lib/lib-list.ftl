<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for generating html lists -->

<#-- 
    Macro: firstLastList

    Output a sequence of <li> elements, adding classes "first" and "last" to first and last list elements, respectively. 
    Especially useful when the list elements are generated conditionally, to avoid complex tests for the presence/absence
    of other list elements in order to assign these classes.
    
    Input should be a series of <li> elements. It is currently not supported for these <li> elements to contain nested
    <li> elements. An <li> element may span multiple lines.
    
    Usage:
        <@firstLastList />
            <li>apples</li>
            <li>bananas</li>
            <li>oranges</li>
        </@firstLastList />

    RY Consider rewriting in Java. Probably designers won't want to modify this. That would allow us to support
    nested <li> elements.
           
-->
<#macro firstLastList>
    <#assign text>
        <#nested>
    </#assign>
    
    <@processListItems text?matches("<li>.*?</li>", "s") />

</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: firstLastListNested
    
    Output a sequence of <li> elements, adding classes "first" and "last" to first and last list elements, respectively. 
    Especially useful when the list elements are generated conditionally, to avoid complex tests for the presence/absence
    of other list elements in order to assign these classes.
    
    Input should be a series of <li> elements separated by some delimiter. Default delimiter value is ",".
    
    Tolerates a delimiter following the last <li> element.
    
    Unlike firstLastList, this macro can process <li> elements that contain other <li> elements, because the delimiters
    indicate how to split the text.
    
    Usage:
        <@firstLastListNested />
            <li>apples</li>,
            <li>bananas</li>,
            <li>oranges</li>
        </@firstLastListNested />
        
        <@firstLastListNested delim="??" />
            <li>apples, oranges</li>??
            <li>bananas, lemons</li>??
            <li>grapefruit, limes</li>
        </@firstLastListNested />
        
        <@firstLastListNested delim="??" />
            <li>Books
                <ul>
                    <li>Persuasion</li>
                    <li>Sense and Sensibility</li>
                </ul>
            </li>,
            <li>Magazines
                <ul>
                    <li>The New Yorker</li>
                    <li>Time</li>
                </ul>
            </li>
        </@firstLastListNested />

    RY Consider rewriting in Java. Probably designers won't want to modify this.
-->
<#macro firstLastListNested delim=",">
    <#assign text>
        <#nested>
    </#assign>

    <#-- Strip out a list-final delimiter, else (unlike most languages) it 
    results in an empty final array item. -->
    <#assign text = text?replace("${delim}\\s*$", "", "r")>

    <@processListItems text?split(delim) />

</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: processListItems 

     Given a list of <li> elements, adds class "first" to the first item and
     "last" to the last item. The <li> elements must have already been put into
     a list; this macro does not handle the raw text. Can be called from 
     firstLastList

-->
<#macro processListItems items>
    <#list items as item>
        <#-- A FreeMarker loop variable cannot have its value modified, so we use a new variable. -->
        <#assign newItem = item?trim>

        <#assign classVal = "">
        
        <#-- Keep any class value already assigned -->
        <#assign currentClass = newItem?matches("^<li [^>]*(class=[\'\"](.*?)[\'\"])")>
        <#list currentClass as m>
            <#assign classVal = m?groups[2]>
            <#assign newItem = newItem?replace(m?groups[1], "")>
        </#list>

        <#if item_index == 0> 
            <#assign classVal = "${classVal} first">
        </#if>
        <#if !item_has_next>       
            <#assign classVal = "${classVal} last">
        </#if>
        
        <#if classVal != ""> 
            <#assign classVal = classVal?replace("^ ", "", "r")>      
            <#-- Replace first instance only, in case the item contains nested li tags. -->     
            <#assign newItem = newItem?replace("<li", "<li class=\"${classVal}\"", "f")>
        </#if>
        ${newItem}
    </#list>
</#macro>


