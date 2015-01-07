/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
    Functions for use by searchIndex.ftl
*/

function updateSearchIndexerStatus() {
    $.ajax({
        url: searchIndexerStatusUrl,
        dataType: "html",
        complete: function(xhr, status) {
        	updatePanelContents(xhr.responseText);
        	setTimeout(updateSearchIndexerStatus,5000);
        }
    });
}

function updatePanelContents(contents) {
	document.getElementById("searchIndexerStatus").innerHTML = contents;
}

$(document).ready(updateSearchIndexerStatus());
