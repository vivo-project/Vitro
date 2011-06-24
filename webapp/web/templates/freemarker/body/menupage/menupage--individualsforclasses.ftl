<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#include "menupage-checkForData.ftl">
<#--Not including data check because vclasses don't appear to return entity counts on their own -->
<#if !noData>
    <section id="menupage-intro" class="people" role="region">
        <h2>${page.title}</h2>
    </section>
    
    <#include "menupage-individualsforclasses-browse.ftl">
    
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}
    
    <#include "menupage-scripts.ftl">
    
	 ${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/browseByVClasses.js"></script>')}
<#else>
  <#  ${noDataNotification} >
</#if>