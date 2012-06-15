/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var processIndividualsForClassesDataGetterContent = {
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
		//Get classes selected
		var classesSelected = [];
		pageContentSection.find("input[name='classInClassGroup']:checked").each(function(){
			//Need to make sure that the class is also saved as a URI
			classesSelected.push($(this).val());
		});
		var returnObject = {classGroup:classGroup, classesSelectedInClassGroup:classesSelected, dataGetterClass:this.dataGetterClass};
		return returnObject;
	},
	//For an existing set of content where form is already set, fill in the values 
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		var classGroupValue = existingContentObject["classGroup"];
		var classesSelected = existingContenetObject["classesSelectedInClassGroup"];
		var numberSelected = classesSelected.length;
		var i;
		for(i = 0; i < numberSelected; i++) {
			var classSelected = classesSelected[i];
			pageContentSection.find("input[name='classInClassGroup'][value='" + classSelected + "']").attr("checked", "checked");
		}
		pageContentSection.find("select[name='selectClassGroup']").val(classGroupValue);
	},
	//For the label of the content section for editing, need to add additional value
	retrieveAdditionalLabelText:function(existingContentObject) {
		//Right now return empty but can hook this into a hashmap with labels and uris
		//set up in browse class group
		return "";
	}
		
}