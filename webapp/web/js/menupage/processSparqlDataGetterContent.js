/* $This file is distributed under the terms of the license in /doc/license.txt$ */

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
		var queryModelValue = existingContentObject["queryModel"];
		//Now find and set value
		pageContentSection.find("input[name='saveToVar']").val(saveToVarValue);
		pageContentSection.find("textarea[name='query']").val(queryValue);
		pageContentSection.find("input[name='queryModel']").val(queryModelValue);
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
    		validationError += pageContentSectionLabel + ": You must supply a variable to save query results. <br />"
    	}
		var queryValue = pageContentSection.find("textarea[name='query']").val();
		if(queryValue == "") {
			validationError += pageContentSectionLabel + ": You must supply a Sparql query. <br />";
		}
    	return validationError;
    }
		
		
};