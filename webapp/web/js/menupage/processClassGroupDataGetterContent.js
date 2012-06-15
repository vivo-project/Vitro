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
		//query model should also be an input, ensure class group URI is saved as URI and not string
		var returnObject = {classGroup:classGroup, dataGetterClass:this.dataGetterClass};
		return returnObject;
	},
	//For an existing set of content where form is already set, fill in the values 
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		var classGroupValue = existingContentObject["classGroup"];
		pageContentSection.find("select[name='selectClassGroup']").val(classGroupValue);
	},
	//For the label of the content section for editing, need to add additional value
	retrieveAdditionalLabelText:function(existingContentObject) {
		//Right now return empty but can hook this into a hashmap with labels and uris
		//set up in browse class group
		return "";
	}
		
		
		
} 