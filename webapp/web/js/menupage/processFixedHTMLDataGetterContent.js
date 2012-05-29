/* $This file is distributed under the terms of the license in /doc/license.txt$ */

//Process sparql data getter and provide a json object with the necessary information
var processFixedHTMLDataGetterContent = {
	dataGetterClass:null,
	//can use this if expect to initialize from elsewhere
	initProcessor:function(dataGetterClass) {
		this.dataGetterClass =dataGetterClass;
	},
	//requires variable and text area
	processPageContentSection:function(pageContentSection) {
		var variableValue = pageContentSection.find("input[name='variable']").val();
		var queryValue = pageContentSection.find("textarea[name='textArea']").val();
		//query model should also be an input
		var returnObject = {saveToVar:variableValue, query:queryValue, dataGetterClass:this.dataGetterClass, queryModel:"vitro:contextDisplayModel"};
		return returnObject;
	}
		
		
}