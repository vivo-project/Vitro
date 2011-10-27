/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function proxyProxiesPanel(p)  {
	var self = this;
	
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
	
	this.parseProxyData = function() {
		var datas = $("div[name='data']", this.proxyDataDiv)
		
		this.proxyData = []
		for (i = 0; i < datas.length; i++) {
			var data = datas[i];
			var uri = $("p[name='uri']", data).text();
			var label = $("p[name='label']", data).text();
			var classLabel = $("p[name='classLabel']", data).text();
			var imageUrl = $("p[name='imageUrl']", data).text();
			this.proxyData.push(new proxyInfoElement(this.templateHtml, uri, label, classLabel, imageUrl, true));
		}
	}

	this.displayProxyData = function() {
		$("div[name='proxyInfoElement']", this.proxyDataDiv).remove();
		
		for (i = 0; i < this.proxyData.length; i++) {
			this.proxyData[i].element().appendTo(this.proxyDataDiv);
		}
	}
	
	this.setupAutoCompleteFields = function() {
	    this.addAutoCompleteField.autocomplete(new proxyAutocomplete(this));
	}
	
	if (this.disableFormInUnsupportedBrowsers()) {
		return;
	}

	this.panel = p;
	this.proxyDataDiv = $("div[name='proxyData']", this.panel).first();
	this.addAutoCompleteField = $("input[name='proxySelectorAC']", this.panel).first();
	
	this.parseProxyTemplate();
	this.parseProxyData();
	this.displayProxyData();
	this.setupAutoCompleteFields();
}

function proxyAutocomplete(parent) {
	var cache = [];
	
	var query = "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n"
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
	
	var filterResults = function(parsed, data) {
		var filtered = [];
		for (var p = 0; p < parsed.length; p++) {
			var dupe = false;
			for (var d = 0; d < data.length; d++) {
				if (data[d].uri == parsed[p].uri) {
					dupe = true;
					break;
				}
			}
			if (!dupe) {
				filtered.push(parsed[p]);
			}
		}
		return filtered;
	}
	
    this.minLength = 3,
    
    this.source = function(request, response) {
        if (request.term in cache) {
        	var filtered = filterResults(cache[request.term], parent.proxyData);
            response(filtered);
            return;
        }
        $.ajax({
            url: '../ajax/sparqlQuery',
            dataType: 'json',
            data: {
                query: query.replace("%term%", request.term),
                model: "userAccounts"
            }, 
            complete: function(xhr, status) {
                var results = $.parseJSON(xhr.responseText);
                var parsed = sparqlUtils.parseSparqlResults(results); 
                cache[request.term] = parsed; 
                var filtered = filterResults(parsed, parent.proxyData);
                response(filtered);
            }
        });
    }
    
    this.select = function(event, ui) {
        parent.proxyData.unshift(new proxyInfoElement(parent.templateHtml, ui.item.uri, ui.item.label, "", "", false));
        parent.displayProxyData();
        event.preventDefault();
        event.target.value = '';
	}
    
}

$(document).ready(function() {
	$("div[name='proxyProxiesPanel']").each(function(i) {
		var ppp = new proxyProxiesPanel(this);
		this["ppp"]=ppp;
	});
});
