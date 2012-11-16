<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#if !labelCount??>
    <#assign labelCount = 0 >
</#if>

<#-- Default individual profile page template -->
<#--@dumpAll /-->
<section id="individual-intro" class="vcard" role="region">
    <#-- Image -->
    <#assign individualImage>
        <@p.image individual=individual 
                  propertyGroups=propertyGroups 
                  namespaces=namespaces 
                  editable=editable 
                  showPlaceholder="with_add_link" />
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
                <p><a href="${relatedSubject.url}" title="return to subject">&larr; return to ${relatedSubject.name}</a></p>                
            <#else>                
                <h1 class="fn">
                    <#-- Label -->
                    <@p.label individual editable labelCount />

                    <#--  Most-specific types -->
                    <@p.mostSpecificTypes individual /><img id="uriIcon" title="${individual.uri}" class="middle" src="${urls.images}/individual/uriIcon.gif" alt="uri icon"/>
                </h1>
            </#if>
        </header>
                
    <#if individualProductExtension??>
        ${individualProductExtension}
    <#else>
            </section> <!-- individual-info -->
        </section> <!-- individual-intro -->
    </#if>

<#assign nameForOtherGroup = "other"> <#-- used by both individual-propertyGroupMenu.ftl and individual-properties.ftl -->

<#-- Property group menu -->
<#-- With release 1.6 the property group is no longer used. The include statement
     remains in the event a particular VIVO site still wants to use it with the new
     collapsible groups.
     
     <#include "individual-propertyGroupMenu.ftl">
-->

<#-- Ontology properties -->
<#include "individual-properties.ftl">
<script>
    var individualLocalName = "${individual.localName}";
</script>

<#assign rdfUrl = individual.rdfUrl>

<#if rdfUrl??>
    <script>
        var individualRdfUrl = '${rdfUrl}';
    </script>
</#if>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip-1.0.0-rc3.min.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/imageUpload/imageUploadUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/individual/individualUriRdf.js"></script>')}
