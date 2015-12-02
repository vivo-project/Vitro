/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$.extend(this, i18nStringsBrowseGroups);
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
		//For now, will utilize the function in pageManagementUtils
		//But should move over any class group specific event handlers etc. into this javascript section
		//Get 'results' from content object
		var results = existingContentObject["results"];
		if(results != null) {
			processClassGroupDataGetterContent.displayClassesForClassGroup(results, pageContentSection);
			//Bind event handlers
			processClassGroupDataGetterContent.bindEventHandlers(pageContentSection);
			//Show hidden class
		}
	},
	//For the label of the content section for editing, need to add additional value
	retrieveContentLabel:function() {
		return i18nStringsBrowseGroups.browseClassGroup;
	},
	retrieveAdditionalLabelText:function(existingContentObject) {
		var label = "";
		var results = existingContentObject["results"];
		if(results != null && results["classGroupName"] != null) {
			label = results["classGroupName"];
		}
		return label;
	},
	//this is copied from pageManagementUtils but eventually
	//we should move all event bindings etc. specific to a content type into its own
	//processing methods
	//TODO: Refine so no references to pageManagementUtils required
	//Page content section specific methods
	displayClassesForClassGroup:function(results, pageContentSection) {
        if ( results.classes.length == 0 ) {
            
        } else {
           var contentNumber = pageContentSection.attr("contentNumber");
        	var classesForClassGroup = pageContentSection.find('section[name="classesInSelectedGroup"]');
        	
            //retrieve classes for class group and display with all selected
            var selectedClassesList = classesForClassGroup.children('ul[name="selectedClasses"]');
            
            selectedClassesList.empty();
            var newId = "allSelected" + contentNumber;
            selectedClassesList.append('<li class="ui-state-default"> <input type="checkbox" name="allSelected" id="' + contentNumber + '" value="all" checked="checked" /> <label class="inline" for="All"> ' + i18nStringsBrowseGroups.allCapitalized + '</label> </li>');
            
            $.each(results.classes, function(i, item) {
                var thisClass = results.classes[i];
                var thisClassName = thisClass.name;
                //For the class group, ALL classes should be selected
                appendHtml = ' <li class="ui-state-default">' + 
                        '<input type="checkbox" checked="checked" name="classInClassGroup" value="' + thisClass.URI + '" />' +  
                       '<label class="inline" for="' + thisClassName + '"> ' + thisClassName + '</label>' + 
                        '</li>';
                selectedClassesList.append(appendHtml);
            });
            
            //Need a way of handling this without it being in the internal class data getter
            var displayInternalMessage = pageContentSection.find('label[for="display-internalClass"] em');
            if(displayInternalMessage != null) {
            	displayInternalMessage.filter(":first").html(results.classGroupName);
            }
            //This is an EXISTING selection, so value should not be empty
    	 	classesForClassGroup.removeClass('hidden');
            if ( $("div#leftSide").height() < $("div#rightSide").height() ) {
                $("div#leftSide").css("height",$("div#rightSide").height() + "px");
            }          
	  	     
        }
    },
	//Toggle class selection already deals with names but want to attach that event
	//handler to THIS new section
	 toggleClassSelection: function(pageContentSection) {
        // Check/unckeck all classes for selection
        pageContentSection.find('input:checkbox[name=allSelected]').click(function(){
             if ( this.checked ) {
             // if checked, select all the checkboxes for this particular section
            $(this).closest("ul").find('input:checkbox[name=classInClassGroup]').attr('checked','checked');
             //$('input:checkbox[name=classInClassGroup]').attr('checked','checked');

             } else {
             // if not checked, deselect all the checkboxes
                 $(this).closest("ul").find('input:checkbox[name=classInClassGroup]').removeAttr('checked');

              // $('input:checkbox[name=classInClassGroup]').removeAttr('checked');
             }
        });

        pageContentSection.find('input:checkbox[name=classInClassGroup]').click(function(){
            $(this).closest("ul").find('input:checkbox[name=allSelected]').removeAttr('checked');
        });
    },
    bindEventHandlers:function(pageContentSection) {
        processClassGroupDataGetterContent.toggleClassSelection(pageContentSection);

    	var selectClassGroupDropdown =  pageContentSection.find("select[name='selectClassGroup']");
    	selectClassGroupDropdown.change(function(e, el) {
             processClassGroupDataGetterContent.chooseClassGroup(pageContentSection);
         });
    },
    chooseClassGroup: function(pageContentSection) {        
    	var selectClassGroupDropdown =  pageContentSection.find("select[name='selectClassGroup']");
        var url = "dataservice?getVClassesForVClassGroup=1&classgroupUri=";
        var vclassUri = selectClassGroupDropdown.val();
        url += encodeURIComponent(vclassUri);
        //Get the page content section
        //Make ajax call to retrieve vclasses
        $.getJSON(url, function(results) {
        	//Moved the function to processClassGroupDataGetterContent
        	//Should probably remove this entire method and copy there
        	processClassGroupDataGetterContent.displayClassesForClassGroup(results, pageContentSection);
        });
    },
    //Validation on form submit: Check to see that class group has been selected 
    validateFormSubmission: function(pageContentSection, pageContentSectionLabel) {
    	var validationError = "";
    	 if (pageContentSection.find('select[name="selectClassGroup"]').val() =='-1') {
             validationError += pageContentSectionLabel + ": " + i18nStringsBrowseGroups.supplyClassGroup + " <br />"; 
         } else {
             //class group has been selected, make sure there is at least one class selected
             var allSelected = pageContentSection.find('input[name="allSelected"]:checked').length;
             var noClassesSelected = pageContentSection.find('input[name="classInClassGroup"]:checked').length;
             if (allSelected == 0 && noClassesSelected == 0) {
                 //at least one class should be selected
                 validationError +=  pageContentSectionLabel + ": " + i18nStringsBrowseGroups.selectClasses + "<br />";
             }
         }
    	 return validationError;
    }
	   
		
		
} 