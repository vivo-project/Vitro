<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for links on individual profile 

     Currently the page displays the vitro namespace links properties. Future versions 
     will use the vivo core ontology links property, eliminating the need for special handling.
-->

<#assign vitroNs = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#">
<#assign primaryLink = propertyGroups.getPropertyAndRemoveFromList("${vitroNs}primaryLink")!>   
<#assign additionalLinks = propertyGroups.getPropertyAndRemoveFromList("${vitroNs}additionalLink")!>    
<#assign linkListClass = linkListClass!"individual-urls">

<#if (primaryLink?has_content || additionalLinks?has_content)> <#-- true when the property is in the list, even if not populated (when editing) -->
    <nav role="navigation">
        <@p.addLinkWithLabel primaryLink editing "Primary Web Page" />
        <#if primaryLink.statements?has_content> <#-- if there are any statements -->
            <ul class="${linkListClass}" id="links-primary" role="list">
                <@p.objectPropertyList primaryLink.statements primaryLink.template editing />
            </ul>
        </#if>
        <@p.addLinkWithLabel additionalLinks editing "Additional Web Pages" />
        <#if additionalLinks.statements?has_content> <#-- if there are any statements -->
            <ul class="${linkListClass}" id="links-additional" role="list">            
                <@p.objectPropertyList additionalLinks.statements additionalLinks.template editing />           
            </ul>
        </#if>
    </nav>
</#if>