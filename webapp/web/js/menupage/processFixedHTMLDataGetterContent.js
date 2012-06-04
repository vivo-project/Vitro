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
		var saveToVarValue = pageContentSection.find("input[name='saveToVar']").val();
		var htmlValue = pageContentSection.find("textarea[name='htmlValue']").val();
		//query model should also be an input
		var returnObject = {saveToVar:saveToVarValue, htmlValue:htmlValue, dataGetterClass:this.dataGetterClass};
		return returnObject;
	}
		
		
}