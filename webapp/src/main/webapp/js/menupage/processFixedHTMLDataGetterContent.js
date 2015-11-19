/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$.extend(this, i18nStringsFixedHtml);
//Process sparql data getter and provide a json object with the necessary information
var processFixedHTMLDataGetterContent = {
	dataGetterClass:null,
	//can use this if expect to initialize from elsewhere
	initProcessor:function(dataGetterClass) {
		this.dataGetterClass =dataGetterClass;
	},
	//requires variable and text area
	processPageContentSection:function(pageContentSection) {
		var saveToVarValue = pageContentSection.find("input[name='saveToVar']").val();
		var htmlValue = pageContentSection.find("textarea[name='htmlValue']").val();
	    //JSON parsing on the server side does not handle single quotes, as it appears it thinks the string has 
	    //ended.  Different data getter types may handle apostrophes/single quotes differently
		//In this case, this is HTML so it simply html Encodes any apostrophes
		htmlValue = processFixedHTMLDataGetterContent.encodeQuotes(htmlValue);
		var returnObject = {saveToVar:saveToVarValue, htmlValue:htmlValue, dataGetterClass:this.dataGetterClass};
		return returnObject;
	},
	//For an existing set of content where form is already set, fill in the values 
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		var saveToVarValue = existingContentObject["saveToVar"];
		var htmlValue = existingContentObject["htmlValue"];
		//In displaying the html value for the edit field, replace the encoded quotes with regular quotes
		htmlValue = processFixedHTMLDataGetterContent.replaceEncodedWithEscapedQuotes(htmlValue);
		//Now find and set value
		pageContentSection.find("input[name='saveToVar']").val(saveToVarValue);
		pageContentSection.find("textarea[name='htmlValue']").val(htmlValue);
	},
	//For the label of the content section for editing, need to add additional value
	retrieveContentLabel:function() {
		return i18nStringsFixedHtml.fixedHtml;
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
    		validationError += pageContentSectionLabel + ": " + i18nStringsFixedHtml.supplyVariableName + " <br />";
    	}
    	if(processFixedHTMLDataGetterContent.stringHasSingleQuote(variableValue)) {
    		validationError += pageContentSectionLabel + ": " + i18nStringsFixedHtml.noApostrophes + " <br />";
    	}
    	if(processFixedHTMLDataGetterContent.stringHasDoubleQuote(variableValue)) {
    		validationError += pageContentSectionLabel + ": " + i18nStringsFixedHtml.noDoubleQuotes + " <br />";
    	}
		var htmlValue = pageContentSection.find("textarea[name='htmlValue']").val();
		if(htmlValue == "") {
			validationError += pageContentSectionLabel + ": " + i18nStringsFixedHtml.supplyHtml + " <br />";
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
		
		
}