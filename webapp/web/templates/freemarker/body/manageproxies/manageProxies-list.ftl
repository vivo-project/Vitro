<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying list of user accounts -->


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

<h3>Manage profile editing</h3>

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

<section id="error-alert" role="alert" class="hidden">
    <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
    <p></p>
</section>

<h4 class="grey">Relate profile editors and profiles <img src="${urls.images}/iconInfo.png" alt="info icon" title="The editors you select on the left hand side will have the ability to edit the VIVO profiles you select on the right hand side. You can select multiple editors and multiple profiles, but you must select a minimum of 1 each." /></h4>

<section class="proxy-profile">

    <form id="add-relation" action="${formUrls.create}" method="POST">
    <fieldset class="proxy">
        <legend>Select editors</legend>
        
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
                            <img class="photo-profile" width="60" alt="%label%" src="%imageUrl%">
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

 <#if page.last != 0>
<h4>Profile editors</h4>
<section id="search-proxy" role="region">
    <form action="${formUrls.list}" method="POST">
        
            <input type="text" name="searchTerm" role="input" />
            <input class="submit" type="submit" name="searchByProxy" value="Search" role="button" /> 
           
                <#if page.previous??>
                   | <a href="${formUrls.list}?pageIndex=${page.previous}&searchTerm=${searchTerm}">Previous</a>
                </#if>
                ${page.current} of ${page.last}
                <#if page.next??>
                    <a href="${formUrls.list}?pageIndex=${page.next}&searchTerm=${searchTerm}">Next</a>
                </#if>
        
                <#if searchTerm?has_content>
                	<p>
                		Search results for "<span class="blue">${searchTerm}</span>" | 
                		<a href="${formUrls.list}">View all profile editors</a>
                	</p>
                </#if>
            
    </form>
</section>
</#if>
<#-- --------------------------------->


<#list relationships as r>
<section class="proxy-profile list-proxy-profile">
    
    <form class="edit-proxy-profiles" action="${formUrls.edit}" method="POST">
        <fieldset class="proxy">
            <#assign p = r.proxyInfos[0]>
            <table class="proxy-item">
                <tr>
                    <td>
                        <img class="photo-profile" width="90" src="${p.imageUrl}" alt="${p.label}">
                    </td>
                    <td class="proxyInfoElement proxy-info">
                        ${p.label} | <span class="class-label">${p.classLabel}</span>
                        <br>
                        <a class="remove-proxyUri" href="${formUrls.edit}?proxyUri=${p.uri}&deleteProxy=Delete proxy">Delete profile editor</a>
                        <input type="hidden" value="${p.uri}" name="proxyUri">
                    </td>
                </tr>
            </table>
                 
                
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
                                    <img class="photo-profile" width="60" alt="%label%" src="%imageUrl%">
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
