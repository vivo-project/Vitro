<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
  How can this done with images instead of buttons containing images?
  Why don't the "alt" values show as tooltips?"
  What was the right way to do this?
 -->

<#-- This is included by identity.ftl --> 
<#if selectLocale??>
    <li>
      <form method="get" action="${selectLocale.selectLocaleUrl}" >
        <#list selectLocale.locales as locale>
          <button type="submit" name="selection" value="${locale.code}">
            <img src="${locale.imageUrl}" height="15" align="middle" alt="${locale.label}"/>
          </button>
          <#if locale_has_next>|</#if>
        </#list>
      </form>
    </li>
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