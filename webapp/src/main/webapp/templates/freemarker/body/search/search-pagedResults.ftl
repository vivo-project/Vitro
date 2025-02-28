<#-- $This file is distributed under the terms of the license in LICENSE$ -->

${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/nouislider.css"/>')}
${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/search-results.css"/>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/nouislider.min.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/wNumb.min.js"></script>')}

<#include "search-lib.ftl">

<script>
	let searchFormId = "search-form";
	let urlsBase = "${urls.base}";
	if (window.location.toString().indexOf("?") == -1){
		var queryText = 'querytext=${querytext?js_string}';
	} else {
		var queryText = window.location.toString().split("?")[1];
	}
</script>

<@searchForm  />

<div class="contentsBrowseGroup">

    <@printPagingLinks />
    <#-- Search results -->
    <ul class="searchhits">
        <#list individuals as individual>
            <li>
                <@shortView uri=individual.uri viewContext="search" />
            </li>
        </#list>
    </ul>

    <@printPagingLinks />
    <br />
</div> 

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/webjars/jquery-ui/jquery-ui.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/search.css" />',
                  '<link rel="stylesheet" type="text/css" href="${urls.base}/css/jquery_plugins/qtip/jquery.qtip.min.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/jquery-ui/jquery-ui.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip.min.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>'
                  )}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/searchDownload.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/search/search_results.js"></script>')}

