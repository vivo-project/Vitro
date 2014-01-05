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
	        		initializeTabs();
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
	    document.getElementById("developer_enabled").onchange = updateDisabledFields
	    document.getElementById("developer_loggingRDFService_enable").onchange = updateDisabledFields
	}
	
	function updateDisabledFields() {
		var developerEnabled = document.getElementById("developer_enabled").checked;
		document.getElementById("developer_permitAnonymousControl").disabled = !developerEnabled;
		document.getElementById("developer_defeatFreemarkerCache").disabled = !developerEnabled;
		document.getElementById("developer_insertFreemarkerDelimiters").disabled = !developerEnabled;
		document.getElementById("developer_pageContents_logCustomListView").disabled = !developerEnabled;
		document.getElementById("developer_pageContents_logCustomShortView").disabled = !developerEnabled;
		document.getElementById("developer_i18n_defeatCache").disabled = !developerEnabled;
		document.getElementById("developer_i18n_logStringRequests").disabled = !developerEnabled;
		document.getElementById("developer_loggingRDFService_enable").disabled = !developerEnabled;
	
		var rdfServiceEnabled = developerEnabled && document.getElementById("developer_loggingRDFService_enable").checked;
		document.getElementById("developer_loggingRDFService_stackTrace").disabled = !rdfServiceEnabled;
		document.getElementById("developer_loggingRDFService_queryRestriction").disabled = !rdfServiceEnabled;
		document.getElementById("developer_loggingRDFService_stackRestriction").disabled = !rdfServiceEnabled;
	}

    function initializeTabs() {
        $("#developerTabs").tabs();
    }
    
	function collectFormData() {
		var data = new Object();
		getCheckbox("developer_enabled", data);
		getCheckbox("developer_permitAnonymousControl", data);
		getCheckbox("developer_defeatFreemarkerCache", data);
		getCheckbox("developer_insertFreemarkerDelimiters", data);
		getCheckbox("developer_pageContents_logCustomListView", data);
		getCheckbox("developer_pageContents_logCustomShortView", data);
		getCheckbox("developer_i18n_defeatCache", data);
		getCheckbox("developer_i18n_logStringRequests", data);
		getCheckbox("developer_loggingRDFService_enable", data);
		getCheckbox("developer_loggingRDFService_stackTrace", data);
		getText("developer_loggingRDFService_queryRestriction", data);
		getText("developer_loggingRDFService_stackRestriction", data);
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
 * Relies on the global variables for the AJAX URL and the CSS files.
 */
$(document).ready(function() {   
	$.each(developerCssLinks, function(index, value){
        var cssLink = $("<link rel='stylesheet' type='text/css' href='" + value + "'>");
        $("head").append(cssLink); 
    });	

	new DeveloperPanel(developerAjaxUrl).setupDeveloperPanel({});	
}); 

