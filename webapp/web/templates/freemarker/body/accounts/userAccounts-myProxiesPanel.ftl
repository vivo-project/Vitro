<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />',
                   '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

<div name="proxyProxiesPanel" style="border: solid; padding: 5px; float: right;">
    Proxy self editors
    <br><br>
    <p>Add proxy:</p>
    <p><input type="text" name="proxySelectorAC" class="acSelector" size="35"></p>
    <p><span name='proxySelectorSearchStatus' moreCharsText='type more characters' noMatchText='no match'>type here</span></p>
    <br><br>
    <p>Selected proxies:</p>

	<#-- Magic div thst holds all of the proxy data and the template that shows how to display it. -->
    <div name="proxyData">
	    <#list proxies as proxy>
	        <div name="data"" style="display: none">
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
                        <img width="90" alt="%label%" src="%imageUrl%">
                    </td>
                    <td>
                        <div>
                        %label% | %classLabel%
                        </div>
                        <div>
                            <a href="." templatePart="remove">remove</a>
                            <input type="hidden" name="proxyUri" value="%uri%" >
                            </div>
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

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/sparqlUtils.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyCommon.js"></script>',   
              '<script type="text/javascript" src="${urls.base}/js/account/accountProxyProxiesPanel.js"></script>')}   
