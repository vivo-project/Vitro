<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#import "lib-list.ftl" as l>

<footer role="contentinfo">
    <#include "copyright.ftl">

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
