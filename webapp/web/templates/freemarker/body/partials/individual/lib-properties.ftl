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

<#-- Some properties usually display without a label. But if there's an add link, 
we need to also show the property label. If no label is specified, the property
name will be used as the label. -->
<#macro addLinkWithLabel property showEditingLinks label="${property.name?capitalize}">
    <#local addLink><@addLink property showEditingLinks /></#local>
    <#if addLink?has_content>
        <h3>${label} ${addLink}</h3> 
    </#if>
</#macro>

<#macro addLink property showEditingLinks>
    <#if showEditingLinks>
        <#local url = property.addUrl>
        <#if url?has_content>
            <a href="${url}" title="add relationship"><img class="add-individual" src="${urls.images}/individual/addIcon.gif" alt="add" /></a>
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
        <a href="${url}" title="change this relationship"><img class="edit-individual" src="${urls.images}/individual/editIcon.gif" alt="edit" /></a>
    </#if>
</#macro>

<#macro deleteLink statement> 
    <#local url = statement.deleteUrl>
    <#if url?has_content>
        <a href="${url}" title="delete this relationship"><img  class="delete-individual" src="${urls.images}/individual/deleteIcon.gif" alt="delete" /></a>
    </#if>
</#macro>

<#-- Macros for specific properties -->

<#-- Vitro namespace links 

     Currently the page displays the vitro namespace links properties. Future versions 
     will use the vivo core ontology links property, eliminating the need for special handling.
     
     Note that this macro has a side-effect in the calls to propertyGroups.getPropertyAndRemoveFromList().
-->
<#macro vitroLinks propertyGroups showEditLinks linkListClass="individual-urls">
    <#local vitroNs = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#">
    <#local primaryLink = propertyGroups.getPropertyAndRemoveFromList("${vitroNs}primaryLink")!>   
    <#local additionalLinks = propertyGroups.getPropertyAndRemoveFromList("${vitroNs}additionalLink")!>    

    <#if (primaryLink?has_content || additionalLinks?has_content)> <#-- true when the property is in the list, even if not populated (when editing) -->
        <nav role="navigation">
            <@addLinkWithLabel primaryLink showEditLinks "Primary Web Page" />
            <#if primaryLink.statements?has_content> <#-- if there are any statements -->
                <ul class="${linkListClass}" id="links-primary" role="list">
                    <@objectPropertyList primaryLink.statements primaryLink.template showEditLinks />
                </ul>
            </#if>
            <@addLinkWithLabel additionalLinks showEditLinks "Additional Web Pages" />
            <#if additionalLinks.statements?has_content> <#-- if there are any statements -->
                <ul class="${linkListClass}" id="links-additional" role="list">            
                    <@objectPropertyList additionalLinks.statements additionalLinks.template showEditLinks />           
                </ul>
            </#if>
        </nav>
    </#if>
</#macro>

<#-- Main image links -->
<#macro imageLinks individual propertyGroups showEditLinks placeholderImage="">
    <#assign mainImage = propertyGroups.getPropertyAndRemoveFromList("${namespaces.vitroPublic}mainImage")!>    
    <#assign thumbUrl = individual.thumbUrl!>  
    <#-- Don't assume that if the mainImage property is populated, there is a thumbnail image (though that is the general case).
         If there's a mainImage statement but no thumbnail image, treat it as if there is no image. --> 
    <#if (mainImage.statements)?has_content && thumbUrl?has_content>    
        <a href="${individual.imageUrl}"><img class="individual-photo2" src="${thumbUrl}" title="click to view larger image" alt="${individual.name}" width="115" /></a>            
        <@p.editingLinks mainImage.statements[0] showEditLinks /> 
    <#else>
        <@p.addLinkWithLabel mainImage showEditLinks "Photo" /> 
        <#if placeholderImage?has_content>
            <img class="individual-photo2" src="${dummyImage}" title = "no image" alt="placeholder image" width="115" /> 
        </#if>                                                      
    </#if>

</#macro>