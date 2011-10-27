/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function proxyProxiesPanel(p)  {
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
	
	if (this.disableFormInUnsupportedBrowsers()) {
		return;
	}

	this.panel = p;
	this.proxyDataDiv = $("div[name='proxyData']", this.panel).first();
	this.addAutoCompleteField = $("input[name='proxySelectorAC']", this.panel).first();
	
	this.parseProxyTemplate();
	this.parseProxyData();
	this.displayProxyData();
	
	this.getProxyInfos = function() {
		return self.proxyData;	
	}
	
	this.addProxyInfo = function(uri, label, junk1, junk2) {
        self.proxyData.unshift(new proxyInfoElement(self.templateHtml, uri, label, "", "", false));
        self.displayProxyData();
	}
	
	this.setupAutoCompleteFields = function() {
		var parms = {
		    query: query, 
		    model: "userAccounts",
		    url: '../ajax/sparqlQuery'
		    };
	    this.addAutoCompleteField.autocomplete(new proxyAutocomplete(parms, this.getProxyInfos, this.addProxyInfo));
	}

	this.setupAutoCompleteFields();
}

$(document).ready(function() {
	$("div[name='proxyProxiesPanel']").each(function(i) {
		var ppp = new proxyProxiesPanel(this);
		this["ppp"]=ppp;
	});
});
