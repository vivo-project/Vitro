<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--
    Template for the page that displays the status of the Search Indexer.
    Most of it is provided by the AJAX call.
-->

<h2>${i18n().search_index_status}</h2>

<div id="searchIndexerError" /></div>

<div id="searchIndexerStatus">
	Search Indexer Status
</div>

<script>
    searchIndexerStatusUrl = '${statusUrl}'
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/search/searchIndex.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/search/searchIndex.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/webjars/jquery-ui/jquery-ui.min.js"></script>')}
