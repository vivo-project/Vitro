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

<#macro addLink property showEditingLinks>
    <#if showEditingLinks>
        <#local url = property.addUrl>
        <#if url?has_content>
            <a href="${url}"><img class="add-individual" src="${urls.images}/individual/addIcon.gif" alt="add relationship" /></a>
        </#if>
    </#if>
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
        <a href="${url}"><img class="edit-individual" src="${urls.images}/individual/editIcon.gif" alt="change this relationship" /></a>
    </#if>
</#macro>

<#macro deleteLink statement> 
    <#local url = statement.deleteUrl>
    <#if url?has_content>
        <a href="${url}"><img  class="delete-individual" src="${urls.images}/individual/deleteIcon.gif" alt="delete this relationship" /></a>
    </#if>
</#macro>