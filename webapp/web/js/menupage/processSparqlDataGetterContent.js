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
	}
		
		
};