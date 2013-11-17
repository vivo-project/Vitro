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
	    document.getElementById("developer.enabled").onchange = updateDisabledFields
	    document.getElementById("developer.loggingRDFService.enable").onchange = updateDisabledFields
	}
	
	function updateDisabledFields() {
		var developerEnabled = document.getElementById("developer.enabled").checked;
		document.getElementById("developer.defeatFreemarkerCache").disabled = !developerEnabled;
		document.getElementById("developer.insertFreemarkerDelimiters").disabled = !developerEnabled;
		document.getElementById("developer.i18n.defeatCache").disabled = !developerEnabled;
		document.getElementById("developer.i18n.logStringRequests").disabled = !developerEnabled;
		document.getElementById("developer.loggingRDFService.enable").disabled = !developerEnabled;
	
		var rdfServiceEnabled = developerEnabled && document.getElementById("developer.loggingRDFService.enable").checked;
		document.getElementById("developer.loggingRDFService.stackTrace").disabled = !rdfServiceEnabled;
		document.getElementById("developer.loggingRDFService.restriction").disabled = !rdfServiceEnabled;
	}

	function collectFormData() {
		var data = new Object();
		data["developer.panelOpen"] = false;
		getCheckbox("developer.enabled", data);
		getCheckbox("developer.defeatFreemarkerCache", data);
		getCheckbox("developer.insertFreemarkerDelimiters", data);
		getCheckbox("developer.i18n.defeatCache", data);
		getCheckbox("developer.i18n.logStringRequests", data);
		getCheckbox("developer.loggingRDFService.enable", data);
		getCheckbox("developer.loggingRDFService.stackTrace", data);
		getText("developer.loggingRDFService.restriction", data);
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
