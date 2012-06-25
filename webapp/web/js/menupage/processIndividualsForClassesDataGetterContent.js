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
	//For the label of the content section for editing, need to add additional value
	retrieveContentLabel:function() {
		return processClassGroupDataGetterContent.retrieveContentLabel();
	},
	//For an existing set of content where form is already set, fill in the values 
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		//select class group in dropdown and append the classes within that class group
		processClassGroupDataGetterContent.populatePageContentSection(existingContentObject, pageContentSection);
		var classesSelected = existingContentObject["classesSelectedInClassGroup"];
		var numberSelected = classesSelected.length;
		var i;
		//Uncheck all since default is checked
		pageContentSection.find("input[name='classInClassGroup']").removeAttr("checked");
		for(i = 0; i < numberSelected; i++) {
			var classSelected = classesSelected[i];
			pageContentSection.find("input[name='classInClassGroup'][value='" + classSelected + "']").attr("checked", "checked");
		}
	},
	//For the label of the content section for editing, need to add additional value
	retrieveAdditionalLabelText:function(existingContentObject) {
		return processClassGroupDataGetterContent.retrieveAdditionalLabelText(existingContentObject);
	},
	 //Validation on form submit: Check to see that class group has been selected 
    validateFormSubmission: function(pageContentSection, pageContentSectionLabel) {
    	return processClassGroupDataGetterContent.validateFormSubmission(pageContentSection, pageContentSectionLabel);
    }
		
}