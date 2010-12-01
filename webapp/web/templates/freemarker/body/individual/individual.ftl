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

        <#-- <#include "individual-properties.ftl"> -->

        <#-- Keywords -->
        <#if individual.keywords?has_content>
            <p id="keywords">Keywords: ${individual.keywordString}</p>
        </#if>
    </div> <!-- #contents -->

</div> <!-- #personWrap -->
<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<section id="individual-intro-person" class="vcard">
    <div id="individual-intro-left-content"> 
        <#-- Thumbnail -->
        <#--<div id="dprop-vitro-image" class="propsItem ${editingClass}">-->
            <#if individual.thumbUrl??>
                <#--<@p.dataPropsWrapper id="thumbnail">-->
                    <a href="${individual.imageUrl}"><img class="individual-photo2" src="${individual.thumbUrl}" title="click to view larger image" alt="${individual.name}" width="115" /></a>
                 <#--</@p.dataPropsWrapper>-->
            <#elseif individual.person>
                 <#--<@p.dataPropsWrapper id="thumbnail">-->
                    <img class="individual-photo2" src="${urls.images}/dummyImages/person.thumbnail.jpg" title = "no image" alt="placeholder image" width="115" />                                                 
                 <#--</@p.dataPropsWrapper>-->         
            </#if>
        <#--</div>-->
        
        <nav>
            <ul id ="individual-tools-people">
                <li><a class="picto-font  picto-uri" href="#">j</a></li>
                <li><a class="picto-font  picto-pdf" href="#">F</a></li>
                <li><a class="picto-font  picto-share" href="#">R</a></li>
                <li><a class="icon-rdf" href="#">RDF</a></li>
            </ul>
        </nav>
        
        <a class="email" href="#"><span class ="picto-font  picto-email">M</span> email@cornell.edu</a> <a class="tel" href="#"><img class ="icon-phone" src="${urls.images}/individual/phone-icon.gif" />555 567 7878</a>
        
        <nav>
            <ul id ="individual-urls-people">
                <li><a href="#">&lt;core:PrimaryURLLink&gt;</a></li>
                <li><a href="#">&lt;core:URLLink&gt;</a></li>
                <li><a href="#">&lt;core:URLLink&gt;</a></li>
                <li><a href="#">&lt;core:URLLink&gt;</a></li>
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
                <#--<@p.dataPropWrapper id="label">-->
                    <h1 class="fn">${individual.name}
                <#--</@p.dataPropWrapper>-->

                <#-- Moniker -->
                <#if individual.moniker?has_content>
                    <#--<@p.dataPropsWrapper id="moniker">-->
                        <span class="preferred-title">${individual.moniker}</span>
                    <#--</@p.dataPropsWrapper>-->                   
                </#if>
                    </h1>
            </#if>
               
            <h2>Current Positions</h2>
            
            <ul id ="individual-positions">
                <li><a href="#">Dancing in heaven with other famous people</a></li>
                <li><a href="#">Sabbatic year for ever</a></li>
            </ul>
        </header>
        
        <p class="individual-overview">Born Margarita Carmen Cansino in Brooklyn, New York City, she was the daughter of flamenco dancer Eduardo Cansino, Sr., who was himself a Sephardic Jewish Spaniard from Castilleja de la Cuesta (Seville), and Ziegfeld girl Volga Hayworth who was of Irish and English descent. She was raised as a Roman Catholic. Her father wanted her to become a dancer while her mother hoped she would become an actress. Her grandfather, Antonio Cansino [+]</p>
        
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