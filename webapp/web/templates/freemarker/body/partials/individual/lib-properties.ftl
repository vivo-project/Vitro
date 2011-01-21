<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-----------------------------------------------------------------------------
    Macros for generating property lists
------------------------------------------------------------------------------>

<#macro dataPropertyList property editable>
    <#list property.statements as statement>
        <@propertyListItem property statement editable>${statement.value}</@propertyListItem>
    </#list> 
</#macro>

<#macro collatedObjectPropertyList property editable>
    <#assign subclasses = property.subclasses>
    <#list subclasses?keys as subclass>
        <li class="subclass">
            <h3>${subclass?lower_case}</h3>
            <ul class="subclass-property-list">
                <@objectPropertyList property subclasses[subclass] property.template editable /> 
            </ul>
        </li>
    </#list>
</#macro>

<#macro simpleObjectPropertyList property editable>
    <@objectPropertyList property property.statements "propStatement-simple.ftl" editable />
</#macro>

<#macro objectPropertyList property statements template editable>
    <#list statements as statement>
        <@propertyListItem property statement editable><#include "${template}"></@propertyListItem>
    </#list>
</#macro>

<#-- Some properties usually display without a label. But if there's an add link, 
we need to also show the property label. If no label is specified, the property
name will be used as the label. -->
<#macro addLinkWithLabel property editable label="${property.name?capitalize}">
    <#local addLink><@addLink property editable /></#local>
    <#if addLink?has_content>
        <h3 id="${property.localName}">${label} ${addLink}</h3> 
    </#if>
</#macro>

<#macro addLink property editable>
    <#if editable>
        <#local url = property.addUrl>
        <#if url?has_content>
            <a class="add-${property.localName}" href="${url}" title="add entry"><img class="add-individual" src="${urls.images}/individual/addIcon.gif" alt="add" /></a>
        </#if>
    </#if>
</#macro>

<#macro propertyListItem property statement editable>
    <li role="listitem">    
        <#nested>        
        <@editingLinks "${property.localName}" statement editable />
    </li>
</#macro>

<#macro editingLinks propertyLocalName statement editable>
    <#if editable>
        <@editLink propertyLocalName statement />
        <@deleteLink propertyLocalName statement />
    </#if>
</#macro>

<#macro editLink propertyLocalName statement>
    <#local url = statement.editUrl>
    <#if url?has_content>
        <a class="edit-${propertyLocalName}" href="${url}" title="edit this entry"><img class="edit-individual" src="${urls.images}/individual/editIcon.gif" alt="edit" /></a>
    </#if>
</#macro>

<#macro deleteLink propertyLocalName statement> 
    <#local url = statement.deleteUrl>
    <#if url?has_content>
        <a class="delete-${propertyLocalName}" href="${url}" title="delete this entry"><img  class="delete-individual" src="${urls.images}/individual/deleteIcon.gif" alt="delete" /></a>
    </#if>
</#macro>

<#-- Macros for specific properties -->

<#-- Vitro namespace links 

     Currently the page displays the vitro namespace links properties. Future versions 
     will use the vivo core ontology links property, eliminating the need for special handling.
     
     Note that this macro has a side-effect in the calls to propertyGroups.getPropertyAndRemoveFromList().
-->
<#macro vitroLinks propertyGroups namespaces editable linkListClass="individual-urls">
    <#local primaryLink = propertyGroups.getPropertyAndRemoveFromList("${namespaces.vitro}primaryLink")!>   
    <#local additionalLinks = propertyGroups.getPropertyAndRemoveFromList("${namespaces.vitro}additionalLink")!>    

    <#if (primaryLink?has_content || additionalLinks?has_content)> <#-- true when the property is in the list, even if not populated (when editing) -->
        <nav role="navigation">
            <@addLinkWithLabel primaryLink editable "Primary Web Page" />
            <#if primaryLink.statements?has_content> <#-- if there are any statements -->
                <ul class="${linkListClass}" id="links-primary" role="list">
                    <@objectPropertyList primaryLink primaryLink.statements primaryLink.template editable />
                </ul>
            </#if>
            <@addLinkWithLabel additionalLinks editable "Additional Web Pages" />
            <#if additionalLinks.statements?has_content> <#-- if there are any statements -->
                <ul class="${linkListClass}" id="links-additional" role="list">            
                    <@objectPropertyList additionalLinks additionalLinks.statements additionalLinks.template editable />           
                </ul>
            </#if>
        </nav>
    </#if>
</#macro>

<#-- Main image links -->
<#macro imageLinks individual propertyGroups namespaces editable placeholderImage="">
    <#local mainImage = propertyGroups.getPropertyAndRemoveFromList("${namespaces.vitroPublic}mainImage")!>    
    <#local thumbUrl = individual.thumbUrl!>  
    <#-- Don't assume that if the mainImage property is populated, there is a thumbnail image (though that is the general case).
         If there's a mainImage statement but no thumbnail image, treat it as if there is no image. --> 
    <#if (mainImage.statements)?has_content && thumbUrl?has_content>    
        <a href="${individual.imageUrl}"><img class="individual-photo" src="${thumbUrl}" title="click to view larger image" alt="${individual.name}" width="160" /></a>            
        <@p.editingLinks "${mainImage.localName}" mainImage.statements[0] editable /> 
    <#else>
        <@p.addLinkWithLabel mainImage editable "Photo" /> 
        <#if placeholderImage?has_content>
            <img class="individual-photo" src="${placeholderImage}" title = "no image" alt="placeholder image" width="160" /> 
        </#if>                                                      
    </#if>
</#macro>

<#-- Label -->
<#macro label individual editable>
    <#local label = individual.nameStatement>
    ${label.value}
    <@p.editingLinks "label" label editable /> 
</#macro>