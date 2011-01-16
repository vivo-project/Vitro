<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#include "menupage-checkForData.ftl">

<#if (!noData)>
    <section id="intro-menupage" role="region">
        <h3>${page.title}</h3>
    
        <section id="content-generic-class" role="region">
            <#-- This will allow us to keep menupage.ftl generic and keep vivo-specific extensions in VIVO -->
            <#if visualizationLink??>
                ${visualizationLink}
            </#if>
        
            <h4>Visual Graph</h4>
    
            <#include "menupage-vClassesInClassgroup.ftl">
    
            <section id="generic-class-graph" role="region">
                <img src="${urls.images}/menupage/visual-graph-generic-class.jpg" width="500" height="283" alt ="" />
            </section>
        </section>
    </section>

    <#include "menupage-browse.ftl">

    ${stylesheets.add("/css/menupage/menupage.css")}

    <#include "menupage-scripts.ftl">
<#else>
    ${noDataNotification}
</#if>