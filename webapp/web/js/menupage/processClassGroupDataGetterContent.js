/* $This file is distributed under the terms of the license in /doc/license.txt$ */

//Process sparql data getter and provide a json object with the necessary information
//Depending on what is included here, a different type of data getter might be used
var processClassGroupDataGetterContent = {
	dataGetterClass:null,
	//can use this if expect to initialize from elsewhere
	initProcessor:function(dataGetterClassInput) {
		this.dataGetterClass = dataGetterClassInput;
	},
	//Do we need a separate content type for each of the others?
	processPageContentSection:function(pageContentSection) {
		//Will look at classes etc. 
		var classGroup = pageContentSection.find("select[name='selectClassGroup']").val();
		//query model should also be an input
		var returnObject = {classGroup:classGroup, dataGetterClass:this.dataGetterClass};
		return returnObject;
	}
		
		
} 