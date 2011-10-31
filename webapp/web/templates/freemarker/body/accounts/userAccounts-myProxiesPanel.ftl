<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />',
                   '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

<div id="edit-myProxy" name="proxyProxiesPanel">
    <h4>Who can edit my profile</h4>
    
    <p>Add profile editor</p>
    
    <p><input type="text" name="proxySelectorAC" class="acSelector" size="35" value="Select an existing last name"></p>
    <p class="search-status"><span name='proxySelectorSearchStatus' moreCharsText='type more characters' noMatchText='no match'>&nbsp;</span></p>

    <p class="selected-editors">Selected editors:</p>
    <#-- Magic div thst holds all of the proxy data and the template that shows how to display it. -->
    <div name="proxyData">
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
            <table>
                <tr>
                    <td>
                        <img class="photo-profile" width="90" alt="%label%" src="%imageUrl%">
                    </td>
                    <td class="proxy-info">
                        %label% | %classLabel%
                        <br /><a class='remove-proxy' href="." templatePart="remove">Remove selection</a>
                        <input type="hidden" name="proxyUri" value="%uri%" >
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>

<script type="text/javascript">
var proxyMechanism = {
    baseUrl: '${urls.base}',
    sparqlQueryUrl: '${formUrls.sparqlQueryAjax}',
    matchingProperty: '${matchingProperty}',
    myAccountUri: '${myAccountUri}'
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/proxyUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/sparqlUtils.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyCommon.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyProxiesPanel.js"></script>')}   
