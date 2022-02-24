<#-- $This file is distributed under the terms of the license in LICENSE$ -->

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
