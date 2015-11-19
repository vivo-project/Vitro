<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#import "lib-list.ftl" as l>

<div id="footer">

    <#if urls.bannerImage??>
        <img class="footerLogo" src="${urls.bannerImage}" alt="${siteTagline!}" />
    </#if>
    
    <div class="footerLinks">
        <ul class="otherNav">  
            <@l.firstLastList> 
                <#include "subMenuLinks.ftl">
            </@l.firstLastList>
        </ul>
    </div>

    <#include "copyright.ftl">

    ${i18n().all_rights_reserved} <a href="${urls.termsOfUse}" title="${i18n().terms_of_use}">${i18n().terms_of_use}</a>

    <#include "version.ftl">
</div>
