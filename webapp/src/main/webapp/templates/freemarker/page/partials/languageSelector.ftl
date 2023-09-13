<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- This is included by identity.ftl  -->
<#if selectLocale??>
<li><ul class="language-dropdown">  <li id="language-menu"><a id="lang-link" href="#" title="${i18n().select_a_language}">${i18n().select_a_language}</a><ul class="sub_menu">
    <#list selectLocale.locales as locale>

            <li <#if locale.selected>status="selected"</#if>>
                	<a href="${selectLocale.selectLocaleUrl}?selection=${locale.code}" title="${i18n().select_locale} -- ${locale.label}">${locale.label?capitalize}<#if locale.country?has_content> (${locale.country})</#if><#if locale.institution?has_content> - ${locale.institution}</#if></a>
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
 *       -- institution (abbreviation)
 *       -- selected (boolean)
-->
<script type="text/javascript">
var i18nStringsLangMenu = {
    selectLanguage: "${i18n().select_a_language?js_string}"
};

function getSelectedLanguageHref() {
    var languageMenu = document.getElementById("language-menu");
    if (languageMenu) {
        var listItems = languageMenu.querySelectorAll("li");

        for (var i = 0; i < listItems.length; i++) {
            if (listItems[i].getAttribute("status") === "selected") {
                var selectedLanguageAnchor = listItems[i].querySelector("a");
                if (selectedLanguageAnchor) {
                    var selectedLanguageHref = selectedLanguageAnchor.getAttribute("href");
                    return selectedLanguageHref;
                }
            }
        }
    }
    return "#";
}

function parseLanguageFromPageURL() {
    var currentURL = window.location.href;
    var langParamRegex = /lang=([^&]+)/;
    var match = currentURL.match(langParamRegex);

    if (match) {
        var langValue = match[1];

        if(getSelectedLanguageHref().endsWith(langValue)) {
            return;
        }

        var languageLinks = document.querySelectorAll('a[href*="selectLocale?selection=' + langValue + '"]');
        if (languageLinks.length > 0) {
            languageLinks[0].click();
        }
    }
}

function updateLangParamIfExists(langValue) {
    var currentURL = window.location.href;
    var langParamRegex = /lang=([^&]+)/;
    var match = currentURL.match(langParamRegex);

    if (match) {
        var updatedURL = currentURL.replace(langParamRegex, 'lang=' + langValue);
        window.history.replaceState({}, document.title, updatedURL);
    }
}

function handleLanguageLinkClick(event, langValue) {
    updateLangParamIfExists(langValue);
}

window.onload = parseLanguageFromPageURL;

var languageLinks = document.querySelectorAll('a[href*="selectLocale?selection="]');
languageLinks.forEach(function(link) {
    var langValue = link.getAttribute('href').split('selectLocale?selection=')[1];
    link.addEventListener('click', function(event) {
        handleLanguageLinkClick(event, langValue);
    });
});

</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/languageMenuUtils.js"></script>')}
