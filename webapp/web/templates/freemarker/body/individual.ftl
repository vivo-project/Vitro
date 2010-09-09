<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for individual profile page -->

<div id="personWrap">
    <#if editStatus.showAdminPanel>
        <#include "individual-adminPanel.ftl">
    </#if>
    
    <div class="contents entity <#if editStatus.showEditLinks>editing</#if>">
        <div id="labelAndMoniker">
            
            <#if relatedSubject??>
                <h2>${relatedSubject.relatingPredicateDomainPublic} for ${relatedSubject.name}</h2>
                <p><a href="${relatedSubject.url}">&larr; return to ${relatedSubject.name}</a></p>                
            <#else>
                <#-- Label -->
                <div class="datatypePropertyValue" id="label">    
                    <div class="statementWrap">
                        <h2>${individual.name}</h2>
                        <#if editStatus.showEditLinks>                        
                        </#if>
                    </div>
                </div>
                
                <#-- Moniker -->
                <#if individual.moniker?has_content>
                    <div class="datatypeProperties">
                        <div class="datatypePropertyValue" id="moniker">
                            <div class="statementWrap">
                                <em class="moniker">${individual.moniker}</em>               
                            </div>
                        </div>
                    </div>                    
                </#if>
            </#if>
        </div> <!-- labelAndMoniker -->

       <#include "individual-sparklineVisualization.ftl">

    </div> <!-- #contents -->

</div> <!-- #personWrap -->


${stylesheets.addFromTheme("/entity.css")}
                           
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