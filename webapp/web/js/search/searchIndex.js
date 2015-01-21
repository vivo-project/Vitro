/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
    Functions for use by searchIndex.ftl
*/

function updateSearchIndexerStatus() {
    $.ajax({
        url: searchIndexerStatusUrl,
        dataType: "html",
        complete: function(xhr, status) {
            if (xhr.status == 200) {
                updatePanelContents(xhr.responseText);
                setTimeout(updateSearchIndexerStatus,5000);
            } else {
                displayErrorMessage(xhr.status + " " + xhr.statusText);
            }
        }
    });
}

function updatePanelContents(contents) {
	document.getElementById("searchIndexerStatus").innerHTML = contents;
}

function displayErrorMessage(message) {
	document.getElementById("searchIndexerError").innerHTML = "<h3>" + message + "</h3>";
}


$(document).ready(updateSearchIndexerStatus());
