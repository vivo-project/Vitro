<#-- 
    Macro: firstLastList
    
    Output a sequence of <li> elements, adding classes "first" and "last" to first and last list elements, respectively. 
    Especially useful when the list elements are generated conditionally, to avoid complex tests for the presence/absence
    of other list elements in order to assign these classes.
    
    Input should be a series of <li> elements separated by some delimiter. Default delimiter value is ",".
    
    Tolerates a delimiter following the last <li> element.
    
    Usage:
        <@firstLastList>
            <li>apples</li>,
            <li>bananas</li>,
            <li>oranges</li>
        <@firstLastList>
        
        <@firstLastList delim="??">
            <li>apples, oranges</li>??
            <li>bananas, lemons</li>??
            <li>grapefruit, limes</li>
        <@firstLastList>

    RY Consider rewriting in Java. Probably designers won't want to modify this.
-->
<#macro firstLastList delim=",">
    <#assign text>
        <#nested>
    </#assign>

    <#-- Strip out a list-final delimiter, else (unlike most languages) it results in an empty final array item. -->
    <#assign text = text?replace("${delim}\\s*$", "", "r")>

    <#assign items = text?split(delim)>
    
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

<#----------------------------------------------------------------------------->


