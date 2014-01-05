/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function DeveloperPanel(developerAjaxUrl) {
	this.setupDeveloperPanel = updateDeveloperPanel;
	
	function updateDeveloperPanel() {
	    $.ajax({
	        url: developerAjaxUrl,
	        dataType: "json",
	        data: collectFormData(),
	        complete: function(xhr, status) {
	        	updatePanelContents(xhr.responseText);
	        	if (document.getElementById("developerPanelSaveButton")) {
	        		initializeTabs();
	        		addBehaviorToElements();
	        		updateDisabledFields();
	        	}
	        }
	    });
	}
		
	function updatePanelContents(contents) {
		document.getElementById("developerPanel").innerHTML = contents;
	}
	
    function initializeTabs() {
        $("#developerTabs").tabs();
    }
    
	function addBehaviorToElements() {
		$( "#developerPanelClickMe" ).click(openPanel);
	    $( "#developerPanelSaveButton" ).click(updateDeveloperPanel);
	    $( "#developerPanelBody [type=checkbox]" ).change(updateDisabledFields);
	}
	
	function openPanel() {
		$( "#developerPanelClickText" ).hide();
		$( "#developerPanelBody" ).css( "display", "block" );
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

	function collectFormData() {
		var data = new Object();
		$( "#developerPanelBody [type=checkbox]" ).each(function(i, element){
				data[element.id] = element.checked;
			});
		$( "#developerPanelBody [type=text]" ).each(function(i, element){
				data[element.id] = element.value;
			});
		return data;
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

	new DeveloperPanel(developerAjaxUrl).setupDeveloperPanel();	
}); 

