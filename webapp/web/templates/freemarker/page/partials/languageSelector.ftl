<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
  How can this done with images instead of buttons containing images?
  Why don't the "alt" values show as tooltips?"
  What was the right way to do this?
 -->

<#-- This is included by identity.ftl  --> 
<#if selectLocale??>
<li><ul class="language-dropdown">  <li id="language-menu"><a id="lang-link" href="#" title="user">${i18n().select_a_language}</a><ul class="sub_menu">   
    <#list selectLocale.locales as locale>
        
            <li <#if locale.selected>status="selected"</#if>>
                	<a href="${selectLocale.selectLocaleUrl}?selection=${locale.code}" title="${i18n().select_locale} -- ${locale.label}"><img src="${locale.imageUrl}" title="${i18n().select_locale} -- ${locale.label}" height="15" style="vertical-align:middle" alt="${locale.label}"/></a>
            </li>
    </#list>
    </ul>
</li></ul></li>
</#if>

<#-- 
 * selectLocale
 * -- selectLocaleUrl
 * -- locales [list of maps]
 *    -- [map]
 *       -- code
 *       -- label (tooltip relative to the current Locale)
 *       -- imageUrl
 *       -- selected (boolean)
-->
<script type="text/javascript">
var i18nStringsLangMenu = {
    selectLanguage: "${i18n().select_a_language}"
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/languageMenuUtils.js"></script>')}
