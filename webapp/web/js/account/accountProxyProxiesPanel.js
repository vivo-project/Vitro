/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function proxyProxiesPanel(p)  {
	var sparqlQueryUrl = '../ajax/sparqlQuery';
	var matchingProperty = "http://vivoweb.org/ontology/core#scopusId"
	var urlContext = 'http://localhost:8080/vivo'
		
	var query = ""
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
	
	var moreInfoQuery = ""
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

	var self = this;
	
	var removeProxyInfo = function(info) {
		self.removeProxyInfo(info)
	}
	
	this.disableFormInUnsupportedBrowsers = function() {
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
	
	this.parseProxyTemplate = function() {
		var templateDiv = $("div[name='template']", this.proxyDataDiv)
		this.templateHtml = templateDiv.html();
		templateDiv.remove();
	};
	
	this.removeProxyInfo = function(info) {
		var idx = self.proxyData.indexOf(info);
		if (idx != -1) {
			self.proxyData.splice(idx, 1);
		}
		self.displayProxyData();
	}
	
	this.parseProxyData = function() {
		var datas = $("div[name='data']", this.proxyDataDiv)
		
		this.proxyData = []
		for (i = 0; i < datas.length; i++) {
			var data = datas[i];
			var uri = $("p[name='uri']", data).text();
			var label = $("p[name='label']", data).text();
			var classLabel = $("p[name='classLabel']", data).text();
			var imageUrl = $("p[name='imageUrl']", data).text();
			this.proxyData.push(new proxyInfoElement(this.templateHtml, uri, label, classLabel, imageUrl, removeProxyInfo));
		}
	}

	this.displayProxyData = function() {
		$("div[name='proxyInfoElement']", this.proxyDataDiv).remove();
		
		for (i = 0; i < this.proxyData.length; i++) {
			this.proxyData[i].element().appendTo(this.proxyDataDiv);
		}
	}
	
	if (this.disableFormInUnsupportedBrowsers()) {
		return;
	}
	
	this.getAdditionalInfo = function(info, externalAuthId) {
        $.ajax({
            url: sparqlQueryUrl,
            dataType: 'json',
            data: {
            	query: moreInfoQuery.replace("%matchingProperty%", matchingProperty).replace("%externalAuthId%", externalAuthId)
            },
            complete: function(xhr, status) {
                var results = $.parseJSON(xhr.responseText);
                var parsed = sparqlUtils.parseSparqlResults(results);
                if (parsed.length > 0) {
	                if ("classLabel" in parsed[0]) {
	                    info.classLabel = parsed[0].classLabel;
	                }
	                if ("imageUrl" in parsed[0]) {
	                	info.imageUrl = urlContext + parsed[0].imageUrl;
	                }
	                self.displayProxyData();
	            }
            }
        });
	}

	this.panel = p;
	this.proxyDataDiv = $("div[name='proxyData']", this.panel).first();
	this.addAutoCompleteField = $("input[name='proxySelectorAC']", this.panel).first();
	this.searchStatusField = $("span[name='proxySelectorSearchStatus']", this.panel).first();
	
	this.parseProxyTemplate();
	this.parseProxyData();
	this.displayProxyData();
	
	this.getProxyInfos = function() {
		return self.proxyData;	
	}
	
	this.addProxyInfo = function(selection) {
		var info = new proxyInfoElement(self.templateHtml, selection.uri, selection.label, "", "", removeProxyInfo)
        self.proxyData.unshift(info);
        self.getAdditionalInfo(info, selection.externalAuthId)
        self.displayProxyData();
	}
	
	
	this.setupAutoCompleteFields = function() {
		var parms = {
		    query: query, 
		    model: "userAccounts",
		    url: sparqlQueryUrl
		    };
		var reportSearchStatus = new searchStatusField(this.searchStatusField, 3).setText;
	    this.addAutoCompleteField.autocomplete(new proxyAutocomplete(parms, this.getProxyInfos, this.addProxyInfo, reportSearchStatus));
	}

	this.setupAutoCompleteFields();
}

function searchStatusField(element, minLength) {
	var emptyText = element.text();
	var moreCharsText = element.attr('moreCharsText');
	var noMatchText = element.attr('noMatchText');

	this.setText = function(searchTermLength, numberOfResults) {
		if (numberOfResults > 0) {
			element.text = '';
		} else if (searchTermLength == 0) {
			element.text(emptyText);
		} else if (searchTermLength < minLength) {
			element.text(moreCharsText);
		} else {
			element.text(noMatchText);
		}
	}
}

$(document).ready(function() {
	$("div[name='proxyProxiesPanel']").each(function(i) {
		var ppp = new proxyProxiesPanel(this);
		this["ppp"]=ppp;
	});
});
