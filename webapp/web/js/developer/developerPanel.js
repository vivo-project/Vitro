/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function DeveloperPanel(developerAjaxUrl) {
	this.setupDeveloperPanel = updateDeveloperPanel;
	
	function updateDeveloperPanel(data) {
	    $.ajax({
	        url: developerAjaxUrl,
	        dataType: "html",
	        data: data,
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
	    $( "#developerPanelSaveButton" ).click(saveSettings);
	    $( "#developerPanelBody input:checkbox" ).change(updateDisabledFields);
	}
	
	function openPanel() {
		$( "#developerPanelClickText" ).hide();
		$( "#developerPanelBody" ).show();
	}
	
	function saveSettings() {
		$( "#developerPanelBody" ).hide();
		updateDeveloperPanel(collectFormData());
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
		document.getElementById("developer_searchIndex_enable").disabled = !developerEnabled;
		document.getElementById("developer_searchIndex_logIndexingBreakdownTimings").disabled = !developerEnabled;
		document.getElementById("developer_searchIndex_suppressModelChangeListener").disabled = !developerEnabled;
		document.getElementById("developer_searchDeletions_enable").disabled = !developerEnabled;
		document.getElementById("developer_searchEngine_enable").disabled = !developerEnabled;
		document.getElementById("developer_authorization_logDecisions_enable").disabled = !developerEnabled;
	
		var rdfServiceEnabled = developerEnabled && document.getElementById("developer_loggingRDFService_enable").checked;
		document.getElementById("developer_loggingRDFService_stackTrace").disabled = !rdfServiceEnabled;
		document.getElementById("developer_loggingRDFService_queryRestriction").disabled = !rdfServiceEnabled;
		document.getElementById("developer_loggingRDFService_stackRestriction").disabled = !rdfServiceEnabled;
	
		var searchIndexEnabled = developerEnabled && document.getElementById("developer_searchIndex_enable").checked;
		document.getElementById("developer_searchIndex_showDocuments").disabled = !searchIndexEnabled;
		document.getElementById("developer_searchIndex_uriOrNameRestriction").disabled = !searchIndexEnabled;
		document.getElementById("developer_searchIndex_documentRestriction").disabled = !searchIndexEnabled;
		
		var searchEngineEnabled = developerEnabled && document.getElementById("developer_searchEngine_enable").checked;
		document.getElementById("developer_searchEngine_addStackTrace").disabled = !searchEngineEnabled;
		document.getElementById("developer_searchEngine_addResults").disabled = !searchEngineEnabled;
		document.getElementById("developer_searchEngine_queryRestriction").disabled = !searchEngineEnabled;
		document.getElementById("developer_searchEngine_stackRestriction").disabled = !searchEngineEnabled;
		
		var authLoggingEnabled = developerEnabled && document.getElementById("developer_authorization_logDecisions_enable").checked;
		document.getElementById("developer_authorization_logDecisions_skipInconclusive").disabled = !authLoggingEnabled;
		document.getElementById("developer_authorization_logDecisions_addIdentifiers").disabled = !authLoggingEnabled;
		document.getElementById("developer_authorization_logDecisions_actionRestriction").disabled = !authLoggingEnabled;
		document.getElementById("developer_authorization_logDecisions_policyRestriction").disabled = !authLoggingEnabled;
		document.getElementById("developer_authorization_logDecisions_userRestriction").disabled = !authLoggingEnabled;
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

