<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
  How can this done with images instead of buttons containing images?
  Why don't the "alt" values show as tooltips?"
  What was the right way to do this?
 -->

<#-- This is included by identity.ftl --> 
<#if selectLocale??>    
    <#list selectLocale.locales as locale>
        <li>
            <a href="${selectLocale.selectLocaleUrl}?selection=${locale.code}" title="${i18n().select_locale} -- ${locale.label}">
                <img src="${locale.imageUrl}" height="15" style="vertical-align:middle" alt="${locale.label}"/>
            </a>
        </li>
    </#list>
</#if>

<#-- 
 * selectLocale
 * -- selectLocaleUrl
 * -- locales [list of maps]
 *    -- [map]
 *       -- code
 *       -- label (tooltip relative to the current Locale)
 *       -- imageUrl
-->