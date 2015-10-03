/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
    Functions for use by searchIndex.ftl
*/

function updateReasonerHistory() {
    $.ajax({
        url: reasonerStatusUrl,
        dataType: "html",
        complete: function(xhr, status) {
            if (xhr.status == 200) {
                updatePanelContents(xhr.responseText);
                setTimeout(updateReasonerHistory,5000);
            } else {
                displayErrorMessage(xhr.status + " " + xhr.statusText);
            }
        }
    });
}

function updatePanelContents(contents) {
	document.getElementById("reasonerHistory").innerHTML = contents;
}

$(document).ready(updateReasonerHistory());
