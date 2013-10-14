/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$.extend(this, i18nStringsSolrIndividuals);

//Process sparql data getter and provide a json object with the necessary information
var processSolrDataGetterContent = {
	dataGetterClass:null,
	//can use this if expect to initialize from elsewhere
	initProcessor:function(dataGetterClass) {
		this.dataGetterClass =dataGetterClass;
		
	},
	
	processPageContentSection:function(pageContentSection) {
		
		var variableValue = pageContentSection.find("input[name='saveToVar']").val();
		var vclassUriValue = pageContentSection.find("select[name='vclassUri']").val();
		

		//query model should also be an input
		//set query model to query model here - vitro:contentDisplayModel
		var returnObject = {saveToVar:variableValue, vclassUri:vclassUriValue, dataGetterClass:this.dataGetterClass};
		return returnObject;
	},
	//For an existing set of content where form is already set, fill in the values 
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		var saveToVarValue = existingContentObject["saveToVar"];
		var vclassUriValue = existingContentObject["vclassUri"];
		
		
		//Now find and set value
		pageContentSection.find("input[name='saveToVar']").val(saveToVarValue);
		//set value of solr query
		pageContentSection.find("select[name='vclassUri']").val(vclassUriValue);
		
	},
	//For the label of the content section for editing, need to add additional value
	retrieveContentLabel:function() {
		return i18nStringsSolrIndividuals.solrIndividuals;
	},
	//For the label of the content section for editing, need to add additional value
	retrieveAdditionalLabelText:function(existingContentObject) {
		var saveToVarValue = existingContentObject["saveToVar"];
		return saveToVarValue;
	},
    //Validation on form submit: Check to see that class group has been selected 
    validateFormSubmission: function(pageContentSection, pageContentSectionLabel) {
    	var validationError = "";
    	//Check that vclassuri and saveToVar have been input
    	var variableValue = pageContentSection.find("input[name='saveToVar']").val();
    	if(variableValue == "") {
    		validationError += pageContentSectionLabel + ": " + i18nStringsSolrIndividuals.supplyQueryVariable + " <br />"
    	}
    	if(processSolrDataGetterContent.stringHasSingleQuote(variableValue)) {
    		validationError += pageContentSectionLabel + ": " + i18nStringsSolrIndividuals.noApostrophes + " <br />";
    	}
    	if(processSolrDataGetterContent.stringHasDoubleQuote(variableValue)) {
    		validationError += pageContentSectionLabel + ": " + i18nStringsSolrIndividuals.noDoubleQuotes + " <br />";
    	}
    	
    	//validation for solr individuals
    	
		var vclassUriValue = pageContentSection.find("select[name='vclassUri']").val();
		if(vclassUriValue == "") {
			validationError += pageContentSectionLabel + ": " + i18nStringsSolrIndividuals.selectClass + " <br />";
		}
    	return validationError;
    },
    encodeQuotes:function(inputStr) {
    	return inputStr.replace(/'/g, '&#39;').replace(/"/g, '&quot;');
    },
    //For the variable name, no single quote should be allowed
    //This can be extended for other special characters
    stringHasSingleQuote:function(inputStr) {
    	return(inputStr.indexOf("'") != -1);
    },
    stringHasDoubleQuote:function(inputStr) {
    	return(inputStr.indexOf("\"") != -1);
    },
    replaceEncodedWithEscapedQuotes: function(inputStr) {

    	return inputStr.replace(/&#39;/g, "\'").replace(/&quot;/g, "\"");
    }
		
		
};