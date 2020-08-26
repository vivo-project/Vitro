<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- This is included by identity.ftl  -->
<#if selectLocale??>
<li><ul class="language-dropdown">  <li id="language-menu"><a id="lang-link" href="#" title="${i18n().select_a_language}">${i18n().select_a_language}</a><ul class="sub_menu">
    <#list selectLocale.locales as locale>

            <li <#if locale.selected>status="selected"</#if>>
                	<a href="${selectLocale.selectLocaleUrl}?selection=${locale.code}" title="${i18n().select_locale} -- ${locale.label}">${locale.label?capitalize}<#if locale.country?has_content> (${locale.country})</#if></a>
            </li>
    </#list>
    </ul>
</li></ul></li>
</#if>

<#--
 * selectLocale
 * -- selectLocaleUrl
 * -- locales [list of locales with country]
 *    -- [locale with country]
 *       -- code
 *       -- label (tooltip displayed in original locale, not current locale)
 *       -- country (displayed in original locale, not current locale)
 *       -- selected (boolean)
-->
<script type="text/javascript">
var i18nStringsLangMenu = {
    selectLanguage: "${i18n().select_a_language?js_string}"
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/languageMenuUtils.js"></script>')}
