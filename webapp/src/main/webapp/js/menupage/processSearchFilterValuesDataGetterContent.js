/* $This file is distributed under the terms of the license in LICENSE$ */

$.extend(this, i18nStringsBrowseSearchFilters);
//Process sparql data getter and provide a json object with the necessary information
//Depending on what is included here, a different type of data getter might be used
var processSearchFilterValuesDataGetterContent = {
	dataGetterClass:null,
	//can use this if expect to initialize from elsewhere
	initProcessor:function(dataGetterClassInput) {
		this.dataGetterClass = dataGetterClassInput;
	},

	processPageContentSection:function(pageContentSection) {
		var searchFilter = pageContentSection.find("select[name='selectSearchFilter']").val();
		//query model should also be an input, ensure class group URI is saved as URI and not string
		var returnObject = {filterUri:searchFilter, dataGetterClass:this.dataGetterClass};
		return returnObject;
	},
	//For an existing set of content where form is already set, fill in the values
	populatePageContentSection:function(existingContentObject, pageContentSection) {
		let searchFilterValue = existingContentObject["searchFilterUri"];
		pageContentSection.find("select[name='selectSearchFilter']").val(searchFilterValue);
	},

	retrieveContentLabel:function() {
		return i18nStringsBrowseSearchFilters.browseSearchFilter;
	},
	retrieveAdditionalLabelText:function(existingContentObject) {
		var label = "";
		var filterName = existingContentObject["searchFilterName"];
		if(filterName != null) {
			label = filterName;
		}
		return label;
	},
    //Validation on form submit: Check to see that filter has been selected
    validateFormSubmission: function(pageContentSection, pageContentSectionLabel) {
    	var validationError = "";
    	 if (pageContentSection.find('select[name="selectSearchFilter"]').val() =='-1') {
             validationError += pageContentSectionLabel + ": " + i18nStringsBrowseSearchFilters.supplySearchFilterValues + " <br />";
         }
    	 return validationError;
    }
}
