<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default individual profile page template -->

<section id="individual-intro" class="vcard" role="region">
    <#-- Image -->
    <#assign individualImage>
        <@p.image individual=individual 
                  propertyGroups=propertyGroups 
                  namespaces=namespaces 
                  editable=editable 
                  showPlaceholder="with_add_link" 
                  placeholder="${urls.images}/placeholders/thumbnail.jpg" />
    </#assign>
    
    <#if ( individualImage?contains('<img class="individual-photo"') )>
        <#assign infoClass = 'class="withThumb"'/>
    </#if>
    
    <div id="photo-wrapper">${individualImage}</div>
    
    <section id="individual-info" ${infoClass!} role="region">
        <#include "individual-adminPanel.ftl">
        
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
                <li role="listitem"><img title="${individual.uri}" class="middle" src="${urls.images}/individual/uriIcon.gif" alt="uri icon" /></li>
                
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

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/getURLParam.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/colorAnimations.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.form.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/controls.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/toggle.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/imageUpload/imageUploadUtils.js"></script>')}
