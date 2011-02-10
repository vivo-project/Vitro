<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#include "menupage-checkForData.ftl">

<#if !noData>
    <section id="menupage-intro" role="region">
        <h2>${page.title}</h2>
    </section>
    
    <#include "menupage-browse.ftl">
    
    ${stylesheets.add("/css/menupage/menupage.css")}
    
    <#include "menupage-scripts.ftl">
<#else>
    ${noDataNotification}
</#if>