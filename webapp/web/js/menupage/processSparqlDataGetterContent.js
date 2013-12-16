/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$.extend(this, i18nStringsSparqlQuery);

//Process sparql data getter and provide a json object with the necessary information
var processSparqlDataGetterContent = {
	dataGetterClass:null,
	//can use this if expect to initialize from elsewhere
	initProcessor:function(dataGetterClass) {
		this.dataGetterClass =dataGetterClass;
	},
	processPageContentSection:function(pageContentSection) {
		
		var variableValue = pageContentSection.find("input[name='saveToVar']").val();
		var queryValue = pageContentSection.find("textarea[name='query']").val();
		queryValue = processSparqlDataGetterContent.encodeQuotes(queryValue);
		var queryModel = pageContentSection.find("input[name='queryModel']").val();

		//query model should also be an input
		//set query model to query model here - vitro:contentDisplayModel
		var returnObject = {saveToVar:variableValue, query:queryValue, dataGetterClass:this.dataGetterClass, queryModel:queryModel};
		return returnObject;
	},
	//For an existing set of content where form is already set, fill in the values 
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		var saveToVarValue = existingContentObject["saveToVar"];
		var queryValue = existingContentObject["query"];
		//replace any encoded quotes with escaped quotes that will show up as quotes in the textarea
		queryValue = processSparqlDataGetterContent.replaceEncodedWithEscapedQuotes(queryValue);
		var queryModelValue = existingContentObject["queryModel"];
		
		
		//Now find and set value
		pageContentSection.find("input[name='saveToVar']").val(saveToVarValue);
		pageContentSection.find("textarea[name='query']").val(queryValue);
		pageContentSection.find("input[name='queryModel']").val(queryModelValue);
	},
	//For the label of the content section for editing, need to add additional value
	retrieveContentLabel:function() {
		return i18nStringsSparqlQuery.sparqlResults;
	},
	//For the label of the content section for editing, need to add additional value
	retrieveAdditionalLabelText:function(existingContentObject) {
		var saveToVarValue = existingContentObject["saveToVar"];
		return saveToVarValue;
	},
    //Validation on form submit: Check to see that class group has been selected 
    validateFormSubmission: function(pageContentSection, pageContentSectionLabel) {
    	var validationError = "";
    	//Check that query and saveToVar have been input
    	var variableValue = pageContentSection.find("input[name='saveToVar']").val();
    	if(variableValue == "") {
    		validationError += pageContentSectionLabel + ": " + i18nStringsSparqlQuery.supplyQueryVariable + " <br />"
    	}
    	if(processSparqlDataGetterContent.stringHasSingleQuote(variableValue)) {
    		validationError += pageContentSectionLabel + ": " + i18nStringsSparqlQuery.noApostrophes + " <br />";
    	}
    	if(processSparqlDataGetterContent.stringHasDoubleQuote(variableValue)) {
    		validationError += pageContentSectionLabel + ": " + i18nStringsSparqlQuery.noDoubleQuotes + " <br />";
    	}
    	//Check that query  model does not have single or double quotes within it
    	//Uncomment this/adapt this when we actually allow display the query model input
    	/*
    	var queryModelValue = pageContentSection.find("input[name='queryModel']").val();
    	if(processSparqlDataGetterContent.stringHasSingleQuote(queryModelValue)) {
    		validationError += pageContentSectionLabel + ": The query model should not have an apostrophe . <br />";

    	}
    	if(processSparqlDataGetterContent.stringHasDoubleQuote(queryModelValue)) {
    		validationError += pageContentSectionLabel + ": The query model should not have a double quote . <br />";
	
    	}*/
    	
		var queryValue = pageContentSection.find("textarea[name='query']").val();
		if(queryValue == "") {
			validationError += pageContentSectionLabel + ": " + i18nStringsSparqlQuery.supplyQuery + " <br />";
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