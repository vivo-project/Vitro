<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for display of individual properties -->

<#-- 
    Macro: dataPropWrapper
    
    Wrap a dataproperty in the appropriate divs
    
    Usage: 
        <@dataPropWrapper id="myId" editStatus=true>
            <#nested>
        </@dataPropWrapper>
-->

<#macro dataPropWrapper id editStatus=false>
    <div class="datatypePropertyValue" id="${id}">
        <div class="statementWrap">
            <#nested>
        </div>
    </div>
</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: dataPropsWrapper
    
    Wrap a dataproperty in the appropriate divs
    
    Usage: 
        <@dataPropsWrapper id="myId" editStatus=true>
            <#nested>
        </@dataPropsWrapper>
-->

<#macro dataPropsWrapper id editStatus=false>
    <div class="datatypeProperties">
        <@dataPropWrapper id=id editStatus=editStatus>
            <#nested>
        </@dataPropWrapper>
    </div>
</#macro>
