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
	this.templateHtml = parseTemplate(dataContainerElement);

	this.displayItemData = function() {
		$(".proxyInfoElement", dataContainerElement).remove();
		
		for (i = 0; i < self.itemData.length; i++) {
			self.itemData[i].element().appendTo(dataContainerElement);
		}
	}

	var getItemData = function() {
		return self.itemData;	
	}

	this.removeItem = function(info) {
		var idx = self.itemData.indexOf(info);
		if (idx != -1) {
			self.itemData.splice(idx, 1);
		}
		self.displayItemData();
	}

	this.addItemData = function(selection) {
		var imageUrl = contextInfo.defaultImageUrl;
		if (selection.imageUrl) {
			imageUrl = contextInfo.baseUrl + selection.imageUrl;
		}
		
		var classLabel = selection.classLabel ? selection.classLabel : "";
		
		var info = new itemElement(self.templateHtml, selection.uri, selection.label, classLabel, 
				imageUrl, self.removeItem);
        self.itemData.unshift(info);
        self.displayItemData();
        self.getAdditionalData(self, info, selection.externalAuthId)
	}

	this.getAdditionalData = function(parent, info, externalAuthId) {
		// For the plain vanilla panel, this need not do anything. For the 
		// proxy panel, this will be replaced by a function that does another 
		// AJAX call to get the classLabel and imageUrl.
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
			data.push(new itemElement(self.templateHtml, uri, label, classLabel, imageUrl, self.removeItem));
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
		var autocompleteInfo = new proxyAutocomplete(parms, excludedUris, getItemData, self.addItemData, updateStatus)
	    autoCompleteField.autocomplete(autocompleteInfo);
	}
	setupAutoCompleteFields();

	self.displayItemData();
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
    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
	+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
	+ "\n"
	+ "SELECT DISTINCT ?uri ?label ?classLabel ?imageUrl \n"
	+ "WHERE { \n"
	+ "    %typesUnion% \n"
	+ "    ?uri rdfs:label ?label ; \n"
	+ "    FILTER (REGEX(str(?label), '^%term%', 'i')) \n"
	+ "} \n"
	+ "ORDER BY ASC(?label) \n"
	+ "LIMIT 25 \n";

var profileMoreInfoQuery = ""
	+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
	+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
	+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
	+ "PREFIX vpublic: <http://vitro.mannlib.cornell.edu/ns/vitro/public#> \n"
	+ "\n"
	+ "SELECT DISTINCT ?classLabel ?imageUrl \n"
	+ "WHERE { \n"
	+ "    OPTIONAL { \n" 
	+ "       <%uri%> vitro:mostSpecificType ?type. \n"
	+ "       ?type rdfs:label ?classLabel  \n"
	+ "       }  \n"
	+ "   OPTIONAL { \n" 
	+ "       <%uri%> vpublic:mainImage ?imageUri. \n"
	+ "       ?imageUri vpublic:thumbnailImage ?thumbUri. \n"
	+ "       ?thumbUri vpublic:downloadLocation ?thumbstreamUri. \n"
	+ "       ?thumbstreamUri vpublic:directDownloadUrl ?imageUrl. \n"
	+ "       }  \n"
	+ "} \n"
	+ "ORDER BY ASC(?label) \n"
	+ "LIMIT 25 \n";

var proxyQuery = ""
    + "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n"
	+ "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n"
	+ "\n"
	+ "SELECT DISTINCT ?uri ?label ?externalAuthId \n"
	+ "WHERE { \n"
	+ "    ?uri a auth:UserAccount ; \n"
	+ "            auth:firstName ?firstName ; \n"
	+ "            auth:lastName ?lastName . \n"
	+ "    LET ( ?label := fn:concat(?lastName, ', ', ?firstName) )"
	+ "    OPTIONAL { ?uri auth:externalAuthId ?externalAuthId } \n"
	+ "    FILTER (REGEX(?label, '^%term%', 'i')) \n"
	+ "} \n"
	+ "ORDER BY ASC(?lastName) ASC(?firstName) \n"
	+ "LIMIT 25 \n";

var proxyMoreInfoQuery = ""
	+ "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n"
	+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
	+ "PREFIX p.1: <http://vitro.mannlib.cornell.edu/ns/vitro/public#> \n"
	+ " \n"
	+ "SELECT ?uri ?classLabel ?imageUrl \n"
	+ "WHERE \n"
	+ "{ \n"
	+ "    ?uri <%matchingProperty%> '%externalAuthId%'. \n"
	+ " \n"
	+ "    OPTIONAL { \n" 
	+ "       ?uri vitro:mostSpecificType ?type. \n"
	+ "       ?type rdfs:label ?classLabel  \n"
	+ "       }  \n"
	+ " \n"
	+ "   OPTIONAL { \n" 
	+ "       ?uri p.1:mainImage ?imageUri. \n"
	+ "       ?imageUri p.1:thumbnailImage ?thumbUri. \n"
	+ "       ?thumbUri p.1:downloadLocation ?thumbstreamUri. \n"
	+ "       ?thumbstreamUri p.1:directDownloadUrl ?imageUrl. \n"
	+ "       }  \n"
	+ "} \n"
	+ "LIMIT 1 \n";

/*
 * This function will allow a proxy panel to execute another query for each proxy. 
 */
var getAdditionalProxyInfo = function(parent, info, externalAuthId) {
    $.ajax({
        url: proxyContextInfo.sparqlQueryUrl,
        dataType: 'json',
        data: {
        	query: proxyMoreInfoQuery.replace(/%matchingProperty%/g, proxyContextInfo.matchingProperty)
        	                    .replace(/%externalAuthId%/g, externalAuthId)
        },
        complete: function(xhr, status) {
            var results = $.parseJSON(xhr.responseText);
            var parsed = sparqlUtils.parseSparqlResults(results);
            if (parsed.length > 0) {
                if ("classLabel" in parsed[0]) {
                    info.classLabel = parsed[0].classLabel;
                }
                if ("imageUrl" in parsed[0]) {
                	info.imageUrl = proxyContextInfo.baseUrl + parsed[0].imageUrl;
                }
                parent.displayItemData();
            }
        }
    });
}

/*
 * The profileTypes context string must have one or more type URIs, separated by commas.
 */
var applyProfileTypes = function(rawQuery) {
	var typeClause = '';
	var types = proxyContextInfo.profileTypes.split(',');

    for (var i = 0; i < types.length; i++) {
	    typeClause += '{ ?uri rdf:type <' + types[i].trim() + '> }';
    	if (i + 1 < types.length) {
	    	typeClause += ' UNION ';
	    } else {
		    typeClause += ' .';
    	}
	}
    return rawQuery.replace(/%typesUnion%/g, typeClause);
}

/*
 * This function will allow a profile panel to execute another query for each profile. 
 */
var getAdditionalProfileInfo = function(parent, info) {
    $.ajax({
        url: proxyContextInfo.sparqlQueryUrl,
        dataType: 'json',
        data: {
        	query: profileMoreInfoQuery.replace(/%uri%/g, info.uri)
        },
        complete: function(xhr, status) {
            var results = $.parseJSON(xhr.responseText);
            var parsed = sparqlUtils.parseSparqlResults(results);
            if (parsed.length > 0) {
                if ("classLabel" in parsed[0]) {
                    info.classLabel = parsed[0].classLabel;
                }
                if ("imageUrl" in parsed[0]) {
                	info.imageUrl = proxyContextInfo.baseUrl + parsed[0].imageUrl;
                }
                parent.displayItemData();
            }
        }
    });
}

/*
 * Execute this when the page loads.
 */
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

	/* If we don't support this form in this browser, just stop here. */
	if (disableFormInUnsupportedBrowsers()) {
		return;
	}

	/* 
	 * For each proxyProfilesPanel, modify the profile query to restrict it
	 * to the permitted types, then create a plain vanilla panel using the 
	 * profile query against the main model. 
	 */
	$("div[name='proxyProfilesPanel']").each(function(i) {
		var query = applyProfileTypes(profileQuery);
		var context = {
			excludedUris: [],
			baseUrl: proxyContextInfo.baseUrl,
			sparqlQueryUrl: proxyContextInfo.sparqlQueryUrl,
			defaultImageUrl: proxyContextInfo.defaultImageUrl,
			query: query,
			model: ''
		}
		var pip = new proxyItemsPanel(this, context);
		pip.getAdditionalData = getAdditionalProfileInfo;
		this["proxyItemsPanel"] = pip;
	});
	
	/* 
	 * For each proxyProxiesPanel, we start with a plain panel using the proxy 
	 * query against the user accounts model. Then we augment it with a method 
	 * that will fetch more info from the main model for each proxy. 
	 */
	$("div[name='proxyProxiesPanel']").each(function(i) {
		var context = {
            excludedUris: [],
			baseUrl: proxyContextInfo.baseUrl,
			sparqlQueryUrl: proxyContextInfo.sparqlQueryUrl,
			defaultImageUrl: proxyContextInfo.defaultImageUrl,
			query: proxyQuery,
			model: 'userAccounts'
		}
		var pip = new proxyItemsPanel(this, context);
		pip.getAdditionalData = getAdditionalProxyInfo;
		this["proxyItemsPanel"] = pip;
	});
});
