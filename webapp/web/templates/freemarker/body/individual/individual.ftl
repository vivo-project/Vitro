<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for individual profile page -->

<#import "lib-property.ftl" as p>
<#import "lib-list.ftl" as l>

<#assign editingClass>
    <#if editStatus.showEditLinks>editing<#else></#if>
</#assign>

<#if editStatus.showAdminPanel>
    <#include "individual-adminPanel.ftl">
</#if>

<#-- Manolo, please do not remove this line. Thanks! -->
<#assign propertyGroups = individual.propertyList>
    
<section id="individual-intro-person" class="vcard">
    <div id="individual-intro-left-content"> 
        <#-- Thumbnail -->
            <#if individual.thumbUrl??>
                <a href="${individual.imageUrl}"><img class="individual-photo2" src="${individual.thumbUrl}" title="click to view larger image" alt="${individual.name}" width="115" /></a>
            <#elseif individual.person>
                <img class="individual-photo2" src="${urls.images}/dummyImages/person.thumbnail.jpg" title = "no image" alt="placeholder image" width="115" />                                                        
            </#if>
        
        <nav>
            <ul id ="individual-tools-people">
                <li><a class="picto-font  picto-uri" href="#">j</a></li>
                <li><a class="picto-font  picto-pdf" href="#">F</a></li>
                <li><a class="picto-font  picto-share" href="#">R</a></li>
                <li><a class="icon-rdf" href="#">RDF</a></li>
            </ul>
        </nav>
        
        <a class="email" href="#"><span class ="picto-font  picto-email">M</span> email@cornell.edu</a> <a class="tel" href="#"><img class ="icon-phone" src="${urls.images}/individual/phone-icon.gif" />555 567 7878</a>
        
        <#-- Links -->
        <nav>
            <ul id ="individual-urls-people">
            <#if individual.links?has_content>
                <@l.firstLastList>
                    <#list individual.links as link>                        
                        <li><a href="${link.url}">${link.anchor}</a></li>
                        </#list>
                </@l.firstLastList>
            </#if>              
            </ul>
        </nav>
    </div>
    
    <div id="individual-intro-right-content"><!-- mb863 get rid off div-->
        <header>
            <#if relatedSubject??>
                <h2>${relatedSubject.relatingPredicateDomainPublic} for ${relatedSubject.name}</h2>
                <p><a href="${relatedSubject.url}">&larr; return to ${relatedSubject.name}</a></p>                
            <#else>
                <#-- Label -->
                    <h1 class="fn">${individual.name}
                        
                <#-- Moniker -->
                <#if individual.moniker?has_content>
                        <span class="preferred-title">${individual.moniker}</span>                  
                </#if>
                    </h1>
            </#if>
               
            <h2>Current Positions</h2>
            
            <ul id ="individual-positions">
                <li><a href="#">Consectetur adipiscing elit, sed est erat.</a></li>
                <li><a href="#">Mauris posuere dui quis massa.</a></li>
            </ul>
        </header>
        
        <p class="individual-overview">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed est erat, tristique non bibendum eu, mollis non est. Cras vehicula velit quis elit porta vel molestie tellus blandit. Donec eget magna dolor. Phasellus faucibus mollis lorem at dapibus. Sed ultricies lobortis mauris in volutpat. Cras mattis neque ut sapien pellentesque fringilla. Mauris posuere dui quis massa mattis id mollis nibh accumsan.  [+]</p>
        
        <h2>Roles</h2>

        <ul id ="individual-roles">
            <li><a href="#">Researcher (5)</a></li>
            <li><a href="#">Principal Investigator (3)</a></li>
            <li><a href="#">Teacher (2)</a></li>
        </ul>
    </div>
</section>

<section id="publications-visualization">
    <section id="sparklines-publications">
         <#include "individual-sparklineVisualization.ftl">
         
        <header><img src="${urls.home}/images/individual/sparkline.gif" />
            <h3><span class="grey">2</span> publications <span class="publication-year-range grey">within the last 10 years</span></h3>
        </header>
        
        <p><a class="all-vivo-publications" href="#">All VIVO publications & co-author network.</a></p>
    </section>
    
    <section id="co-authors">
        <header>
            <h3><span class="grey">10 </span>Co-Authors</h3>
        </header>
        
        <ul>
            <li><a href="#"><img class="co-author" src="${urls.images}/individual/Bacall.jpg" /></a></li>
            <li><a href="#"><img class="co-author" src="${urls.images}/individual/Bogart.jpg" /></a></li>
            <li><a href="#"><img class="co-author" src="${urls.images}/individual/Gable.jpg" /></a></li>
            <li><a href="#"><img class="co-author" src="${urls.images}/individual/Grant.jpg" /></a></li>
            <li><a href="#"><img class="co-author" src="${urls.images}/individual/Leigh.jpg" /></a></li>
            <li><a href="#"><img class="co-author" src="${urls.images}/individual/Welles.jpg" /></a></li>
        </ul>
        
        <p class="view-all-coauthors"><a class="view-all-style" href="#">View All <span class="pictos-arrow-10">4</span></a></p>
    </section>
</section>

<#-- Ontology properties -->
<#include "individual-properties.ftl">

<#-- Keywords -->
<#if individual.keywords?has_content>
    <p id="keywords">Keywords: ${individual.keywordString}</p>
</#if>

${stylesheets.addFromTheme("/css/entity.css")}
                           
<#-- RY Figure out which of these scripts really need to go into the head, and which are needed at all (e.g., tinyMCE??) -->
${headScripts.add("/js/jquery_plugins/getUrlParam.js",                  
                  "/js/jquery_plugins/colorAnimations.js",
                  "/js/propertyGroupSwitcher.js",
                  "/js/jquery_plugins/jquery.form.js",
                  "/js/tiny_mce/tiny_mce.js", 
                  "/js/controls.js",
                  "http://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22visualization%22%2C%22version%22%3A%221%22%2C%22packages%22%3A%5B%22areachart%22%2C%22imagesparkline%22%5D%7D%5D%7D",
                  "/js/toggle.js")}
                  
${scripts.add("/js/imageUpload/imageUploadUtils.js")}