<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying list of user accounts -->


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

<h3>Manage proxy self editors & profiles</h3>

<#if message??>
    <section class="account-feedback" role="region">
        <#if message.success?? > 
            <p>The operation was successful.</p>
        </#if>
    
        <#if message.failure?? >
            <p> The operation was unsuccessful. Full details can be found in the system log.</p>
        </#if>
    </section>
</#if>

<h4 class="grey">Relate proxy self editors and profiles</h4>

<section class="proxy-profile">

    <form action="${formUrls.create}" method="POST">
    <fieldset class="proxy">
        <legend>Select proxies</legend>
        
        <div name="proxyProxiesPanel">
            <input type="text" name="proxySelectorAC" class="acSelector" size="35" value="Select an existing last name" role="input" />
            <p class="search-status"><span name='proxySelectorSearchStatus' moreCharsText='type more characters' noMatchText='no match'>&nbsp;</span></p>
        
            <#-- Magic div that holds all of the proxy data and the template that shows how to display it. -->
            <div name="proxyData">
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
                            %label% | <span class="class-label">%classLabel%</span>
                            <br />
                            <a class='remove-proxy' href="." templatePart="remove">Remove selection</a>
                            <input type="hidden" name="proxyUri" value="%uri%" >
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
    </fieldset>
    

    <fieldset class="profile">
      <legend>Select profiles</legend>  
      
      <div name="proxyProfilesPanel">
          <input type="text" name="proxySelectorAC" class="acSelector" size="35" value="Select an existing last name">
          <p class="search-status"><span name='proxySelectorSearchStatus' moreCharsText='type more characters' noMatchText='no match'>&nbsp;</span></p>

            <#-- Magic div thst holds all of the proxy data and the template that shows how to display it. -->
            <div name="proxyData">
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
                <input type="hidden" name="profileUri" templatePart="uriField" value="%uri%" >
            </div>
        </div>
    </div>

</fieldset>

<p><input class="submit pos-submit" type="submit" name="createRelationship" value="Save" /></p>
    </form>
</section>

<section id="search-proxy" role="region">
    <form action="${formUrls.list}" method="POST">
        <h4>Proxy self editors</h4>
            <input type="text" name="searchTerm" role="input" />
            <input class="submit" type="submit" name="searchBytProxy" value="Search" role="button" /> | 
 
            <#if page.previous??>
                <a href="${formUrls.list}?pageIndex=${page.previous}&searchTerm=${searchTerm}">Previous</a>
            </#if>
            ${page.current} of ${page.last}
            <#if page.next??>
                <a href="${formUrls.list}?pageIndex=${page.next}&searchTerm=${searchTerm}">Next</a>
            </#if>
    </form>
</section>

<#-- --------------------------------->


<#list relationships as r>
<section class="proxy-profile bg-proxy-profile">
    <form action="${formUrls.edit}" method="POST">
        <fieldset class="proxy">
            <#assign p = r.proxyInfos[0]>
            <table class="proxy-item">
                <tr>
                    <td>
                        <img class="photo-profile" width="120" src="${p.imageUrl}" alt="${p.label}">
                    </td>
                    <td class="proxyInfoElement proxy-info">
                        ${p.label} | <span class="class-label">${p.classLabel}</span>
                        <br>
                        <input type="hidden" value="${p.uri}" name="proxyUri">
                    </td>
                </tr>
            </table>
                <#--><a href="${formUrls.edit}?deleteProxy=Delete%20proxy">Delete proxy</a>-->
                <input class="submit" type="submit" name="deleteProxy" value="Delete proxy" />
        </fieldset>  
        
        <fieldset class="profile">
            <legend>Add profile</legend>
            
            <div name="proxyProfilesPanel">
                <input type="text" name="proxySelectorAC" class="acSelector" size="35" value="Select an existing last name">
                <p class="search-status">
                    <span name='proxySelectorSearchStatus' 
                          moreCharsText='type more characters' 
                          noMatchText='no match'>&nbsp;</span>
                </p>

                <p class="selected-editors">Selected profiles:</p>
    
                <#-- Magic div that holds all of the proxy data and the template that shows how to display it. -->
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
                        <input type="hidden" name="profileUri" templatePart="uriField" value="%uri%" >
                    </div>
                </div>
            </div>
            <input class="submit" type="submit" name="modifyProfileList" value="Save changes to profiles" />
        </fieldset> 
</form>

</section>
</#list>





<script type="text/javascript">
var proxyContextInfo = {
    baseUrl: '${urls.base}',
    sparqlQueryUrl: '${formUrls.sparqlQueryAjax}',
    defaultImageUrl: '${formUrls.defaultImageUrl}',
    matchingProperty: '${matchingProperty}',
    profileTypes: '${profileTypes}'
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/proxyUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/sparqlUtils.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyCommon.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyItemsPanel.js"></script>')}   
