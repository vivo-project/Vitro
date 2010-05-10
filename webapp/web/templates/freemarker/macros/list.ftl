<#-- 
    Output a sequence of <li> elements, adding classes "first" and "last" to first and last list elements, respectively. 
    It is helpful when the list elements are generated conditionally, to avoid complex tests for the presence/absence
    of other list elements in order to assign these classes.
    
    Input should be a series of <li> elements separated by some delimiter. Default delimiter value is ",".
    
    Tolerates a delimiter following the last <li> element.
    
    Sample invocations:
        <@makeList>
            <li>apples</li>,
            <li>bananas</li>,
            <li>oranges</li>
        <@makeList>
        
        <@makeList delim="??">
            <li>apples, oranges</li>??
            <li>bananas, lemons</li>??
            <li>grapefruit, limes</li>
        <@makeList>

    RY Consider rewriting in Java. Probably designers won't want to modify this.
-->
<#macro makeList delim=",">
    <#assign text>
        <#nested>
    </#assign>
    <#-- Strip out a list-final delimiter, else (unlike many languages) it results in an empty final array item. -->
    <#assign text = text?replace("${delim}$", "", "r")>

    <#assign arr = text?split(delim)>
    <#assign lastIndex = arr?size-1>
    
    <#list arr as item>
        <#-- A FreeMarker loop variable cannot have its value modified, so we use a new variable. -->
        <#assign newItem = item>

        <#assign classVal = "">
        
        <#-- Keep any class value already assigned -->
        <#assign currentClass = newItem?matches("<li (class=[\'\"](.*?)[\'\"])")>
        <#list currentClass as m>
            <#assign classVal = m?groups[2]>
            <#assign newItem = newItem?replace(m?groups[1], "")>
        </#list>

        <#-- Test indices, rather than comparing content, on the off chance
        that there are two list items with the same content. -->
        <#-- <#if item == arr?first> -->
        <#if item_index == 0> 
            <#assign classVal = "${classVal} first">
        </#if>
        <#-- <#if item == arr?last> -->
        <#if item_index == lastIndex>       
            <#assign classVal = "${classVal} last">
        </#if>
        <#if classVal != ""> 
            <#assign classVal = classVal?replace("^ ", "", "r")>      
            <#-- Replace first instance only, in case it contains nested li tags. -->     
            <#assign newItem = newItem?replace("<li", "<li class=\"${classVal}\"", "f")>
        </#if>
        ${newItem}
    </#list>
</#macro>

<#----------------------------------------------------------------------------->