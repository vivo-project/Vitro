<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

</div> <!-- #wrapper-content -->

<footer role="contentinfo">
    <p class="copyright">
        <#if copyright??>
            <small>&copy;${copyright.year?c}
            <#if copyright.url??>
                <a href="${copyright.url}" title="${i18n().copyright}">${copyright.text}</a>
            <#else>
                ${copyright.text}
            </#if>
             | <a class="terms" href="${urls.termsOfUse}" title="${i18n().terms_of_use}">${i18n().terms_of_use}</a></small> | 
        </#if>
        ${i18n().powered_by} <a class="powered-by-vitro" href="http://vitro.sourceforge.net"><strong>Vitro</strong></a>
        <#if user.hasRevisionInfoAccess>
             | ${i18n().version} <a href="${version.moreInfoUrl}" title="${i18n().version}">${version.label}</a>
        </#if>
    </p>
    
    <nav role="navigation">
        <ul id="footer-nav" role="list">
            <li role="listitem"><a href="${urls.about}" title="${i18n().about}">${i18n().about}</a></li>
            <#if urls.contact??>
                <li role="listitem"><a href="${urls.contact}" title="${i18n().contact_us}">${i18n().contact_us}</a></li>
            </#if> 
            <li role="listitem"><a href="http://www.vivoweb.org/support" target="blank" title="${i18n().support}">${i18n().support}</a></li>
        </ul>
    </nav>
</footer>

<#include "scripts.ftl">