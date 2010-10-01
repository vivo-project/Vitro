<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for individual profile page -->

<#import "lib-property.ftl" as p>
<#import "lib-list.ftl" as l>

<#assign editingClass>
    <#if editStatus.showEditLinks>editing<#else></#if>
</#assign>

<div id="personWrap">
    <#if editStatus.showAdminPanel>
        <#include "individual-adminPanel.ftl">
    </#if>
    
    <div class="contents entity ${editingClass}">
        <div id="labelAndMoniker">
            
            <#if relatedSubject??>
                <h2>${relatedSubject.relatingPredicateDomainPublic} for ${relatedSubject.name}</h2>
                <p><a href="${relatedSubject.url}">&larr; return to ${relatedSubject.name}</a></p>                
            <#else>
                <#-- Label -->
                <@p.dataPropWrapper id="label">
                    <h2>${individual.name}</h2>
                </@p.dataPropWrapper>
                
                <#-- Moniker -->
                <#if individual.moniker?has_content>
                    <@p.dataPropsWrapper id="moniker">
                        <em class="moniker">${individual.moniker}</em>
                    </@p.dataPropsWrapper>                   
                </#if>
            </#if>
        </div> <!-- labelAndMoniker -->

        <#include "individual-sparklineVisualization.ftl">
       
        <#-- Thumbnail -->
        <div id="dprop-vitro-image" class="propsItem ${editingClass}"> 
            <#if individual.thumbUrl??>
                <@p.dataPropsWrapper id="thumbnail">
                    <a class="image" href="${individual.imageUrl}">
                        <img src="${individual.thumbUrl}" 
                             title="click to view larger image" 
                             alt="${individual.name}" width="115" />
                    </a>
                </@p.dataPropsWrapper> 
            <#elseif individual.person>
                <@p.dataPropsWrapper id="thumbnail">
                    <img src="${urls.images}/dummyImages/person.thumbnail.jpg"
                         title = "no image" alt="placeholder image" width="115" />                                                 
                </@p.dataPropsWrapper>             
            </#if>
        </div>
        
        <#-- Links -->
        <#if individual.links?has_content>
            <div id="dprop-vitro-links" class="propsItem ${editingClass}">
                <ul class="externalLinks properties">
                    <@l.firstLastList>
                        <#list individual.links as link>                        
                            <li>
                                <span class="statementWrap">
                                    <a class="externalLink" href="${link.url}">${link.anchor}</a>
                                </span>
                            </li>                                                        
                        </#list>
                    </@l.firstLastList>
                </ul>
            </div>
        </#if>
       
        <#-- Keywords -->
        <#if individual.keywords?has_content>
            <p id="keywords">Keywords: ${individual.keywordString}</p>
        </#if>
    </div> <!-- #contents -->

</div> <!-- #personWrap -->

${stylesheets.addFromTheme("/css/entity.css")}
                           
<#-- RY Figure out which of these scripts really need to go into the head, and which are needed at all (e.g., tinyMCE??) -->
${headScripts.add("/js/jquery.js",
                  "/js/jquery_plugins/getUrlParam.js",
                  "/js/jquery_plugins/colorAnimations.js",
                  "/js/propertyGroupSwitcher.js",
                  "/js/jquery_plugins/jquery.form.js",
                  "/js/tiny_mce/tiny_mce.js", 
                  "/js/controls.js",
                  "http://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22visualization%22%2C%22version%22%3A%221%22%2C%22packages%22%3A%5B%22areachart%22%2C%22imagesparkline%22%5D%7D%5D%7D",
                  "/js/toggle.js")}
                  
${scripts.add("/js/imageUpload/imageUploadUtils.js")}