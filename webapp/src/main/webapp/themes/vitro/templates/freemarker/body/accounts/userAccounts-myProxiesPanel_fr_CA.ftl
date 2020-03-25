<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

<#assign strings = i18n() />

<section id="edit-myProxy" name="proxyProxiesPanel" role="region">
    <h4>${strings.who_can_edit_profile}</h4>

    <label for="addProfileEditor">${strings.add_profile_editor}</label>
    <input id="addProfileEditor" type="text" name="proxySelectorAC" class="acSelector" size="35"
            value="${strings.select_existing_last_name}" role="input" />
    <span><img class="loading-profileMyAccoount hidden" src="${urls.images}/indicatorWhite.gif" /></span>

    <p class="search-status">
        <span name='proxySelectorSearchStatus' moreCharsText='${strings.type_more_characters}' noMatchText='${strings.no_match}'>&nbsp;</span>
    </p>
    <p name="excludeUri" style="display: none">${myAccountUri}<p>
    <p class="selected-editors">${strings.selected_editors}:</p>

    <#-- Magic ul that holds all of the proxy data and the template that shows how to display it. -->
    <ul name="proxyData" role="navigation">
        <#list proxies as proxy>
            <div name="data" style="display: none">
                <p name="uri">${proxy.uri}</p>
                <p name="label">${proxy.label}</p>
                <p name="classLabel">${proxy.classLabel}</p>
                <p name="imageUrl">${proxy.imageUrl}</p>
            </div>
        </#list>

        <#--
            Each proxy will be shown using the HTML inside this div.
            It must contain at least:
              -- a link with templatePart="remove"
              -- a hidden input field with templatePart="uriField"
        -->
        <div name="template" style="display: none">
            <li role="listitem">
                <img class="photo-profile" width="90" alt="%label%" src="%imageUrl%">

                <p class="proxy-info">%label% | <span class="class-label">%classLabel%</span>
                    <br />
                    <a class='remove-proxy' href="." templatePart="remove" title="${strings.remove_selection_title}">${strings.remove_selection}</a>

                    <input type="hidden" name="proxyUri" value="%uri%" role="input" />
                </p>
            </li>
        </div>
    </ul>
</section>

<script type="text/javascript">
var proxyContextInfo = {
    baseUrl: '${urls.base}',
    ajaxUrl: '${formUrls.proxyAjax}'
};
var i18nStrings = {
    selectEditorAndProfile: "${i18n().select_editor_and_profile}"
};
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />',
                   '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.12.1.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/proxyUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyCommon.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyItemsPanel.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}
