/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
 * ----------------------------------------------------------------------------
 * proxyItemsPanel
 * ----------------------------------------------------------------------------
 * Display an AJAX-enabled list of proxy-related items (either proxies or 
 * profiles). 
 * 
 * The list may start out with a population of items. items may be added by 
 * selecting them in the auto-complete box. Items may be removed by clicking
 * the "remove" link next to that item.
 * 
 * A hidden field will hold the URI for each item, so when the form is submitted,
 * the controller can determine the list of items.
 * ----------------------------------------------------------------------------
 * You provide:
 *   p -- the DOM element that contains the template and the data. 
 *         It also contains the autocomplete field.
 * ----------------------------------------------------------------------------
 */
function proxyItemsPanel(panel, contextInfo)  {
	var self = this;

	this.itemData = [];

	var excludedUris = contextInfo.excludedUris;
	var dataContainerElement = $("[name='proxyData']", panel).first();
	var autoCompleteField = $("input[name='proxySelectorAC']", panel).first();
	var searchStatusField = $("span[name='proxySelectorSearchStatus']", panel).first();

	var parseTemplate = function(dataContainer) {
		var templateDiv = $("div[name='template']", dataContainer)
		var templateHtml = templateDiv.html();
		templateDiv.remove();
		return templateHtml;
	};
	var templateHtml = parseTemplate(dataContainerElement);

	var displayItemData = function() {
		$(".proxyInfoElement", dataContainerElement).remove();
		
		for (i = 0; i < self.itemData.length; i++) {
			self.itemData[i].element().appendTo(dataContainerElement);
		}
	}

	var getItemData = function() {
		return self.itemData;	
	}

	/* callback function */
	var addItemData = function(selection) {
		var imageUrl = contextInfo.defaultImageUrl;
		if (selection.imageUrl) {
			imageUrl = contextInfo.baseUrl + selection.imageUrl;
		}
		
		var info = new itemElement(templateHtml, selection.uri, selection.label, selection.classLabel, 
				imageUrl, removeItem);
        self.itemData.unshift(info);
        displayItemData();
	}

	var removeItem = function(info) {
		var idx = self.itemData.indexOf(info);
		if (idx != -1) {
			self.itemData.splice(idx, 1);
		}
		displayItemData();
	}

	var parseOriginalData = function() {
		var dataDivs = $("div[name='data']", dataContainerElement)
		var data = [];
		for (i = 0; i < dataDivs.length; i++) {
			var dd = dataDivs[i];
			var uri = $("p[name='uri']", dd).text();
			var label = $("p[name='label']", dd).text();
			var classLabel = $("p[name='classLabel']", dd).text();
			var imageUrl = $("p[name='imageUrl']", dd).text();
			data.push(new itemElement(templateHtml, uri, label, classLabel, imageUrl, removeItem));
		}
		return data;
	}
	this.itemData = parseOriginalData();

	var setupAutoCompleteFields = function() {
		var parms = {
		    query: contextInfo.query, 
		    model: contextInfo.model,
		    url: contextInfo.sparqlQueryUrl
		    };
		var updateStatus = new statusFieldUpdater(searchStatusField, 3).setText;
		var autocompleteInfo = new proxyAutocomplete(parms, excludedUris, getItemData, addItemData, updateStatus)
	    autoCompleteField.autocomplete(autocompleteInfo);
	}
	setupAutoCompleteFields();

	displayItemData();
}

function statusFieldUpdater(element, minLength) {
	var emptyText = element.text();
	var moreCharsText = element.attr('moreCharsText');
	var noMatchText = element.attr('noMatchText');

	this.setText = function(searchTermLength, numberOfResults) {
		if (numberOfResults > 0) {
			element.text('');
		} else if (searchTermLength == 0) {
			element.text(emptyText);
		} else if (searchTermLength < minLength) {
			element.text(moreCharsText);
		} else {
			element.text(noMatchText);
		}
	}
}

var profileQuery = ""
    + "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n"
	+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
    + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
	+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
	+ "PREFIX vpublic: <http://vitro.mannlib.cornell.edu/ns/vitro/public#> \n"
	+ "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n"
	+ "\n"
	+ "SELECT DISTINCT ?uri ?label ?classLabel ?imageUrl \n"
	+ "WHERE { \n"
	+ "    ?uri a foaf:Person ; \n"
	+ "            rdfs:label ?label ; \n"
	+ "    OPTIONAL { \n" 
	+ "       ?uri vitro:mostSpecificType ?type. \n"
	+ "       ?type rdfs:label ?classLabel  \n"
	+ "       }  \n"
	+ "   OPTIONAL { \n" 
	+ "       ?uri vpublic:mainImage ?imageUri. \n"
	+ "       ?imageUri vpublic:thumbnailImage ?thumbUri. \n"
	+ "       ?thumbUri vpublic:downloadLocation ?thumbstreamUri. \n"
	+ "       ?thumbstreamUri vpublic:directDownloadUrl ?imageUrl. \n"
	+ "       }  \n"
	+ "    FILTER (REGEX(str(?label), '^%term%', 'i')) \n"
	+ "} \n"
	+ "ORDER BY ASC(?label) \n"
	+ "LIMIT 25 \n";


$(document).ready(function() {
	var disableFormInUnsupportedBrowsers = function() {
		var disableWrapper = $('#ie67DisableWrapper');

		// Check for unsupported browsers only if the element exists on the page
		if (disableWrapper.length) {
			if (vitro.browserUtils.isIELessThan8()) {
				disableWrapper.show();
				$('.noIE67').hide();
				return true;
			}
		}
		return false;
	};

	if (disableFormInUnsupportedBrowsers()) {
		return;
	}

	$("div[name='proxyProfilesPanel']").each(function(i) {
		var context = {
			excludedUris: [],
			baseUrl: proxyContextInfo.baseUrl,
			sparqlQueryUrl: proxyContextInfo.sparqlQueryUrl,
			defaultImageUrl: proxyContextInfo.defaultImageUrl,
			query: profileQuery,
			model: ''
		}
		var ppp = new proxyItemsPanel(this, context);
		this["ppp"]=ppp;
	});
});
