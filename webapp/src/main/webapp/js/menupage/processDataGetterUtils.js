/* $This file is distributed under the terms of the license in LICENSE$ */

//This class is responsible for the product-specific form processing/content selection that might be possible
//Overrides the usual behavior of selecting the specific JavaScript class needed to convert the form inputs
//into a JSON object for submission based on the page content type

//This will need to be overridden or extended, what have you.. in VIVO
var processDataGetterUtils = {
		dataGetterProcessorMap:{"browseClassGroup": processClassGroupDataGetterContent,
								"searchFilterValues": processSearchFilterValuesDataGetterContent,
								"sparqlQuery": processSparqlDataGetterContent,
								"fixedHtml":processFixedHTMLDataGetterContent,
								"individualsForClasses":processIndividualsForClassesDataGetterContent,
								"searchIndividuals":processSearchDataGetterContent},
	    selectDataGetterType:function(pageContentSection) {
			var contentType = pageContentSection.attr("contentType");
			//The form can provide "browse class group" as content type but need to check
			//whether this is in fact individuals for classes instead
			if(contentType == "browseClassGroup") {
				//Is ALL NOT selected and there are other classes, pick one
				//this SHOULD be an array
				var allClassesSelected = pageContentSection.find("input[name='allSelected']:checked");
				//If all NOT selected then need to pick a different content type
				if(allClassesSelected.length == 0) {
					contentType = "individualsForClasses";
				}
			}

			return contentType;
	    },
	    isRelatedToBrowseClassGroup:function(contentType) {
	    	return (contentType == "browseClassGroup" || contentType == "individualsForClasses");
	    },
	    getContentTypeForCloning:function(contentType) {
	    	if(contentType == "browseClassGroup" || contentType == "individualsForClasses") {
	    		return "browseClassGroup";
	    	}
	    	return contentType;
	    }
};
