<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#include "menupage-checkForData.ftl">

<#if !noData>
    <section id="menupage-intro" role="region">
        <h3>${page.title}</h3>
        
        <#-- This will allow us to keep menupage.ftl generic and keep vivo-specific extensions in VIVO -->
        <#if visualizationLink??>
            ${visualizationLink}
        </#if>
    </section>
    
    <#include "menupage-browse.ftl">
    
    ${stylesheets.add("/css/menupage/menupage.css")}
    
    <#include "menupage-scripts.ftl">
<#else>
    ${noDataNotification}
</#if>