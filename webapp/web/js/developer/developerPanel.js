/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function DeveloperPanel(developerAjaxUrl) {
	this.setupDeveloperPanel = updateDeveloperPanel;
	
	function updateDeveloperPanel(data) {
	    $.ajax({
	        url: developerAjaxUrl,
	        dataType: "json",
	        data: data,
	        complete: function(xhr, status) {
	        	updatePanelContents(xhr.responseText);
	        	if (document.getElementById("developerPanelSaveButton")) {
	        		enablePanelOpener();
	        		addBehaviorToElements();
	        		updateDisabledFields();
	        	}
	        }
	    });
	}
		
	function updatePanelContents(contents) {
		document.getElementById("developerPanel").innerHTML = contents;
	}
	
	function enablePanelOpener() {
		document.getElementById("developerPanelClickMe").onclick = function() {
					document.getElementById("developerPanelClickText").style.display = "none";
					document.getElementById("developerPanelBody").style.display = "block";
				};
	}
	
	function addBehaviorToElements() {
	    document.getElementById("developerPanelSaveButton").onclick = function() {
			updateDeveloperPanel(collectFormData());
	    }
	    document.getElementById("developerEnabled").onchange = updateDisabledFields
	    document.getElementById("developerLoggingRDFServiceEnable").onchange = updateDisabledFields
	}
	
	function updateDisabledFields() {
		var developerEnabled = document.getElementById("developerEnabled").checked;
		document.getElementById("developerDefeatFreemarkerCache").disabled = !developerEnabled;
		document.getElementById("developerInsertFreemarkerDelimiters").disabled = !developerEnabled;
		document.getElementById("developerPageContentsLogCustomListView").disabled = !developerEnabled;
		document.getElementById("developerPageContentsLogCustomShortView").disabled = !developerEnabled;
		document.getElementById("developerI18nDefeatCache").disabled = !developerEnabled;
		document.getElementById("developerI18nLogStringRequests").disabled = !developerEnabled;
		document.getElementById("developerLoggingRDFServiceEnable").disabled = !developerEnabled;
	
		var rdfServiceEnabled = developerEnabled && document.getElementById("developerLoggingRDFServiceEnable").checked;
		document.getElementById("developerLoggingRDFServiceStackTrace").disabled = !rdfServiceEnabled;
		document.getElementById("developerLoggingRDFServiceQueryRestriction").disabled = !rdfServiceEnabled;
		document.getElementById("developerLoggingRDFServiceStackRestriction").disabled = !rdfServiceEnabled;
	}

	function collectFormData() {
		var data = new Object();
		getCheckbox("developerEnabled", data);
		getCheckbox("developerDefeatFreemarkerCache", data);
		getCheckbox("developerInsertFreemarkerDelimiters", data);
		getCheckbox("developerPageContentsLogCustomListView", data);
		getCheckbox("developerPageContentsLogCustomShortView", data);
		getCheckbox("developerI18nDefeatCache", data);
		getCheckbox("developerI18nLogStringRequests", data);
		getCheckbox("developerLoggingRDFServiceEnable", data);
		getCheckbox("developerLoggingRDFServiceStackTrace", data);
		getText("developerLoggingRDFServiceQueryRestriction", data);
		getText("developerLoggingRDFServiceStackRestriction", data);
		return data;
	}

	function getCheckbox(key, dest) {
		dest[key] = document.getElementById(key).checked;
	}
	
	function getText(key, dest) {
		dest[key] = document.getElementById(key).value;
	}
}	

/*
 * Relies on the global variable for the AJAX URL.
 */
$(document).ready(function() {   
	new DeveloperPanel(developerAjaxUrl).setupDeveloperPanel({});	
}); 

