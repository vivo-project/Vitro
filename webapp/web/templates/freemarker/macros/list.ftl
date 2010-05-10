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

-->
<#macro makeList delim=",">
    <#assign text>
        <#nested>
    </#assign>
    <#-- Strip out a list-final delimiter, else (unlike many languages) it results in an empty final array item. -->
    <#assign text = text?replace("${delim}$", "", "r")>

    <#assign arr = text?split(delim)>
    <#list arr as item>
        <#-- The loop variable cannot have its value modified, so we use a new variable. -->
        <#assign newItem = item>
        <#-- rjy7 we're not accounting for the prior existence of an assigned class. Should parse the item for that. -->
        <#assign classVal = "">
        <#if newItem == arr?first>
            <#assign classVal = "${classVal} first">
        </#if>
        <#if newItem == arr?last>
            <#assign classVal = "${classVal} last">
        </#if>
        <#if classVal != "">       
            <#-- replace first instance only, in case it contains nested li tags -->     
            <#assign newItem = newItem?replace("<li", "<li class=\"${classVal}\"", "f")>
        </#if>
        ${newItem}
    </#list>
</#macro>

