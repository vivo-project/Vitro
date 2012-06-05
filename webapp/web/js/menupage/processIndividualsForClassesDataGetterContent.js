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
	}	
		
}