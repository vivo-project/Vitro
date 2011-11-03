<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying list of user accounts -->


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

<p>
    <#if page.previous??>
        <a href="${formUrls.list}?pageIndex='${page.previous}'">Previous</a>
    </#if>
    ${page.current} of ${page.last}
    <#if page.next??>
        <a href="${formUrls.list}?pageIndex='${page.next}'">Next</a>
    </#if>
</p> 

<#list relationships as r>
    <form action="${formUrls.edit} method="POST">
    	<table style="width: 100%;">
    		<tr>
    		    <td style="border: thin solid black; width: 50%;">
                    <#assign p = r.proxyInfos[0]>
                    <table><tr>
                        <td>
                            <img class="photo-profile" width="90" src="${p.imageUrl}" alt="${p.label}">
                        </td>
                        <td class="proxyInfoElement">
                            ${p.label}} |
                            <span class="class-label">${p.classLabel}</span>
                            <br>
                            <input type="hidden" value="${p.uri}" name="proxyUri">
                        </td>
                    </tr></table>

                    <input type="submit" name="deleteProxy" value="Delete this proxy" />
    		    </td>

    		    <td style="border: thin solid black">
        		    <div id="edit-myProxy" name="proxyProfilesPanel">
                        <p>Add profile</p>
                        <p><input type="text" name="proxySelectorAC" class="acSelector" size="35" value="Select an existing last name"></p>
                        <p class="search-status">
                            <span name='proxySelectorSearchStatus' 
                                  moreCharsText='type more characters' 
                                  noMatchText='no match'>&nbsp;</span>
                        </p>

                        <p class="selected-editors">Selected profiles:</p>
            
                        <#-- Magic div thst holds all of the proxy data and the template that shows how to display it. -->
                        <div name="proxyData">
                            <#list r.profileInfos as p>
                                <div name="data" style="display: none">
                                    <p name="uri">${p.uri}</p>
                                    <p name="label">${p.label}</p>
                                    <p name="classLabel">${p.classLabel}</p>
                                    <p name="imageUrl">${p.imageUrl}</p>
                                </div>
                            </#list>

                            <#-- 
                                Each proxy will be shown using the HTML inside this element.
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
                                            %label% | <span class="class-label">%classLabel%</span>
                                            <br />
                                            <a class='remove-proxy' href="." templatePart="remove">Remove selection</a>
                                        </td>
                                    </tr>
                                </table>
                                <input type="hidden" name="proxyUri" value="%uri%" >
                            </div>
                        </div>
                    </div>
                    <input type="submit" name="modifyProfileList" value="Save changes to profiles" />
    		    </td>
    		</tr>
        </table>
    </form>
</#list>

<script type="text/javascript">
var proxyContextInfo = {
    baseUrl: '${urls.base}',
    sparqlQueryUrl: '${formUrls.sparqlQueryAjax}',
    defaultImageUrl: '${formUrls.defaultImageUrl}',
    matchingProperty: '${matchingProperty}',
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/proxyUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/sparqlUtils.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyCommon.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyItemsPanel.js"></script>')}   
