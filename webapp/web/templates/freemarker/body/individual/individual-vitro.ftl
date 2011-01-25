<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default individual profile page template -->

<section id="individual-intro" class="vcard" role="region">
    
    <#-- Image -->
    <@p.imageLinks individual=individual 
                   propertyGroups=propertyGroups 
                   namespaces=namespaces 
                   editable=editable 
                   showPlaceholder="with_add_link" 
                   placeholder="${urls.images}/placeholders/person.thumbnail.jpg" />
    
    <section id="individual-info" role="region">
        <#if individual.showAdminPanel>
               <#include "individual-adminPanel.ftl">
        </#if>
        
        <header>
            <#if relatedSubject??>
                <h2>${relatedSubject.relatingPredicateDomainPublic} for ${relatedSubject.name}</h2>
                <p><a href="${relatedSubject.url}">&larr; return to ${relatedSubject.name}</a></p>                
            <#else>                
                <h1 class="fn">
                    <#-- Label -->
                    <@p.label individual editable />
                        
                    <#-- Moniker -->
                    <#if individual.moniker?has_content>
                        <span class="preferred-title">${individual.moniker}</span>                  
                    </#if>
                </h1>
            </#if>
        </header>
        
        <nav role="navigation">
            <ul id ="individual-tools" role="list">
                <#assign uriUrl = individual.uri>
                <#if uriUrl??>
                    <li role="listitem"><a title="Individual uri" href="${uriUrl}"><img class="middle" src="${urls.images}/individual/uriIcon.gif" alt="uri icon" /></a></li>
                </#if>
                
                <#assign rdfUrl = individual.rdfUrl>
                <#if rdfUrl??>
                    <li role="listitem"><a title="View this individual in RDF format" class="icon-rdf" href="${rdfUrl}">RDF</a></li>
                </#if>
            </ul>
        </nav>
                
        <#-- Links -->
        <@p.vitroLinks propertyGroups namespaces editable  />

    <#if individualProductExtension??>
        ${individualProductExtension}
    <#else>
            </section> <!-- individual-info -->
        </section> <!-- individual-intro -->
    </#if>

<#assign nameForOtherGroup = "other"> <#-- used by both individual-propertyGroupMenu.ftl and individual-properties.ftl -->

<#-- Property group menu -->
<#include "individual-propertyGroupMenu.ftl">

<#-- Ontology properties -->
<#include "individual-properties.ftl">

${stylesheets.add("/css/individual/individual.css")}
                           
<#-- RY Figure out which of these scripts really need to go into the head, and which are needed at all (e.g., tinyMCE??) -->
${headScripts.add("/js/jquery_plugins/getURLParam.js",                  
                  "/js/jquery_plugins/colorAnimations.js",
                  "/js/jquery_plugins/jquery.form.js",
                  "/js/tiny_mce/tiny_mce.js", 
                  "/js/controls.js",
                  "/js/toggle.js")}
                  
${scripts.add("/js/imageUpload/imageUploadUtils.js")}