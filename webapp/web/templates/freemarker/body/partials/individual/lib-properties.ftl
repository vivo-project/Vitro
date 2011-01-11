<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-----------------------------------------------------------------------------
    Macros for generating property lists
------------------------------------------------------------------------------>

<#macro dataPropertyList statements showEditingLinks>
    <#list statements as statement>
        <@propertyListItem statement showEditingLinks>${statement.value}</@propertyListItem>
    </#list> 
</#macro>

<#macro collatedObjectPropertyList property showEditingLinks>
    <#assign subclasses = property.subclasses>
    <#list subclasses?keys as subclass>
        <li class="subclass">
            <h3>${subclass?lower_case}</h3>
            <ul class="subclass-property-list">
                <@objectPropertyList subclasses[subclass] property.template showEditingLinks /> 
            </ul>
        </li>
    </#list>
</#macro>

<#macro simpleObjectPropertyList property showEditingLinks>
    <@objectPropertyList property.statements "propStatement-simple.ftl" showEditingLinks />
</#macro>

<#macro objectPropertyList statements template showEditingLinks>
    <#list statements as statement>
        <@propertyListItem statement showEditingLinks><#include "${template}"></@propertyListItem>
    </#list>
</#macro>

<#macro propertyListItem statement showEditingLinks>
    <li role="listitem">    
        <#nested>
        <@editingLinks statement showEditingLinks />
    </li>
</#macro>

<#macro editingLinks statement showEditingLinks>
    <#if showEditingLinks>
        <@editLink statement />
        <@deleteLink statement />
    </#if>
</#macro>

<#macro editLink statement>
    <#local url = statement.editUrl>
    <#if url?has_content>
        <a href="${url}">edit</a>
    </#if>
</#macro>

<#macro deleteLink statement> 
    <#local url = statement.deleteUrl>
    <#if url?has_content>
        <a href="${url}">delete</a>
    </#if>
</#macro>