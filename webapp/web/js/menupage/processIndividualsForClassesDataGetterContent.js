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
		var classesSelected = pageContentSection.find("input[name='classInClassGroup']:checked").val();
		var returnObject = {classGroup:classGroup, classesSelected:classesSelected, dataGetterClass:this.dataGetterClass};
		return returnObject;
	}	
		
}