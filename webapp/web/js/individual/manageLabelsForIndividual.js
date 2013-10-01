/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var manageLabels = {

    /* *** Initial page setup *** */
   
    onLoad: function() {
    
            this.mixIn();    
            this.initObjects();
            this.initPage();
      
            var selectedRadio;       
        },

    mixIn: function() {

        // Get the custom form data from the page
        $.extend(this, customFormData);
        $.extend(this, i18nStrings);
    },
    
    initObjects:function() {
        this.addLabelForm = $('#addLabelForm');
        this.showFormButtonWrapper = $('#showAddForm');
        this.showFormButton = $("#showAddFormButton");
        //button to return to profile - div with only cancel 
        this.showCancelOnlyButton = $("#showCancelOnly");
        this.addLabelCancel = this.addLabelForm.find(".cancel");
        this.submit = this.addLabelForm.find('input#submit');
        this.labelLanguage = this.addLabelForm.find("#newLabelLanguage");
        this.existingLabelsList = $("#existingLabelsList");
    },

    // Initial page setup. Called only at page load.
    initPage: function() {
    	
        var disableSubmit = true;
        if(this.submissionErrorsExist == "false") {
        	//hide the form to add label
        	this.addLabelForm.hide();
        	
        	//If the number of available locales is zero, then hide the ability to show the form as well
        	if(this.numberAvailableLocales == 0) {
            	manageLabels.showFormButtonWrapper.hide();
            	this.showCancelOnlyButton.show();
        	} else {
        		//if the add label button is visible, don't need cancel only link
        		this.showCancelOnlyButton.hide();
        	}
        	
        } else {
        	//Display the form
        	this.onShowAddForm();
        	
        	//Also make sure the save button is enabled in case there is a value selected for the drop down
        	if(this.labelLanguage.val() != "") {
        		disableSubmit = false;
        	}
        
        }
        
       
       
        if(disableSubmit) {
        	//disable submit until user selects a language
            this.submit.attr('disabled', 'disabled');
            this.submit.addClass('disabledSubmit');
        }
       
        this.bindEventListeners();
                       
    },
    
    bindEventListeners: function() {
               
        this.labelLanguage.change( function() {
        	//if language selected, allow submission, otherwise disallow
        	var selectedLanguage = manageLabels.labelLanguage.val();
        	if(selectedLanguage != "") {
        		manageLabels.submit.attr('disabled', '');
        		manageLabels.submit.removeClass('disabledSubmit');      
        	} else {
        		manageLabels.submit.attr('disabled', 'disabled');
        		manageLabels.submit.addClass('disabledSubmit');     
        	}
        });
        
        //enable form to add label to be displayed or hidden
        this.showFormButton.click(function() {
        	//clear the inputs for the label if the button is being clicked
        	manageLabels.clearAddForm();
        	manageLabels.onShowAddForm();
        });
        
        //Check for clicking on existing labels list remove links
        //Note addition will refresh the page and removing will remove the item so adding event listeners
        //to remove links should keep remove link events in synch with page
       
        this.existingLabelsList.find("a.remove").click(function(event) {
        	var message = "Are you sure you wish to delete this label?"
        	if (!confirm(message)) {
                return false;
            }
            
        	//First check with confirmation whether or not they want to delete
        	manageLabels.processLabelDeletion(this);
        	return false;
        });
        
        
        this.addLabelForm.find("a.cancel").click(function(event){
        	event.preventDefault();
        	//clear the add form
        	manageLabels.clearAddForm();
        	//hide the add form
        	manageLabels.onHideAddForm();
        	return false;
        });


    },
    clearAddForm:function() {
    	//clear inputs and select
    	manageLabels.addLabelForm.find("input[type='text'],select").val("");
    	//set the button for save to be disabled again
    	manageLabels.submit.attr('disabled', 'disabled');
		manageLabels.submit.addClass('disabledSubmit');     
    },
    onShowAddForm:function() {
    	manageLabels.addLabelForm.show();
    	manageLabels.showFormButtonWrapper.hide();
    	manageLabels.addLabelCancel.click(function(){
    		//Canceling the add label form will hide the form
    		manageLabels.addLabelForm.hide();
    		manageLabels.showFormButtonWrapper.show();
    	});
    },
    
    onHideAddForm:function() {
    	manageLabels.addLabelForm.hide();
    	manageLabels.showFormButtonWrapper.show();
    	//manageLabels.addLabelCancel.unbind("click");
    },
    //Remove label                  
    processLabelDeletion: function(selectedLink) {
    	
        // PrimitiveDelete only handles one statement, so we have to use PrimitiveRdfEdit to handle multiple
        // retractions if they exist. But PrimitiveRdfEdit also handles assertions, so pass an empty string
        // for "additions"
        var add = "";
        var labelValue = $(selectedLink).attr('labelValue');
        var tagOrTypeValue = $(selectedLink).attr('tagOrType');
        if(tagOrTypeValue == "untyped") {
        	tagOrTypeValue = "";
        }
        var retract = "<" + manageLabels.individualUri + "> <http://www.w3.org/2000/01/rdf-schema#label> "
                        + "\"" + $(selectedLink).attr('labelValue') + "\"" + $(selectedLink).attr('tagOrType') + " ." ;
            
    

        retract = retract.substring(0,retract.length -1);

        $.ajax({
            url: manageLabels.processingUrl,
            type: 'POST', 
            data: {
                additions: add,
                retractions: retract
            },
            dataType: 'json',
            context: selectedLink, // context for callback
            complete: function(request, status) {
                
                if (status == 'success') {
                	//Remove the label from the list
                	manageLabels.removeLabelFromList(selectedLink);
                   manageLabels.updateLocaleSelection();
                }
                else {
                	//Instead of alert, write error to template
                    alert(manageLabels.errorProcessingLabels);
       
                }
            }
        });        

    },
    removeLabelFromList:function(selectedLink) {
    	var languageName = $(selectedLink).attr("languageName");
    	$(selectedLink).parent().remove();
    	//See if there are any other remove link
    	if(languageName != "untyped") {
    		//find if there are any other remove links for the same language
    		var allRemoveLinks = manageLabels.existingLabelsList.find("a.remove");
    		var removeLinks = manageLabels.existingLabelsList.find("a.remove[languageName='" + languageName + "']");
    		if(removeLinks.length == 0) {
    			//if there aren't any other labels for this language, also remove the heading
    			manageLabels.existingLabelsList.find("h3[languageName='" + languageName + "']").remove();
    			
    		}
    		//Check to see if there is only one label left on the page, if so remove or hide the remove link
    		if(allRemoveLinks.length == 1) {
    			allRemoveLinks.remove();
    			//These will be removed instead of hidden because currently add will reload the page
    			//whereas remove executes an ajax query and the page isn't reloaded
    		}
    	}
    	
    	
    },
    //Determine if there are new locales that can be added to the options once a delete has occurred
    updateLocaleSelection:function() {
    	//Check what languages remain
    	var existingLanguages = {};
    	//Look at which languages are currently represented
    	manageLabels.existingLabelsList.find("a.remove").each(function(){
    		var languageCode = $(this).attr("languageCode");
    		if(!(languageCode in existingLanguages)) {
    			existingLanguages[languageCode] = true;
    		}
    	});
    	
    	//Now check against full list, if any in full list not represented, will need to include in dropdown
    	//This is a list of 
    	var availableLocalesList = [];
    	var listLen = selectLocalesFullList.length;
    	var i;
    	for(i = 0; i < listLen; i++) {
    		var possibleLanguageInfo = selectLocalesFullList[i];
    		var possibleLanguageCode = possibleLanguageInfo["code"];
    		var possibleLangaugeLabel = possibleLanguageInfo["label"];
    		if(!(possibleLanguageCode in existingLanguages)) {
    			//manageLabels.addLanguageCode(possibleLanguageCode, possibleLanguageLabel);
    			availableLocalesList.push(possibleLanguageInfo);
    		}
    	}
    	
    	//Now sort this list by the label property on the object
    	 availableLocalesList.sort(function(a, b) {
    	        var compA = a["label"];
    	        var compB = b["label"];
    	        return compA < compB ? -1 : 1;
    	    });
    	 //Re-show the add button and the form if they were hidden before
    	 if(availableLocalesList.length > 0 && manageLabels.showFormButtonWrapper.is(":hidden")) {
    		 manageLabels.showFormButtonWrapper.show();
    		 //hide the cancel only button
    		 manageLabels.showCancelOnlyButton.hide();
    	 }
    	 
    	 //Now replace dropdown with this new list
    	 manageLabels.generateLocalesDropdown(availableLocalesList);
    	
    },
    generateLocalesDropdown:function(availableLocalesList) {
    	//First check if there are any available locales left, if not then hide the entire add form
    	//technically, this first part should  never be invoked client side because
    	//this can only happen on ADD not remove, and add will refresh the page
    	//On the other hand the show add button etc. can be displayed
    	if(availableLocalesList.length == 0) {
    		//Hide the add form if there are no locales left that can be added
    		manageLabels.addLabelForm.hide();
    		manageLabels.showFormButtonWrapper.hide();
    	} else {
    		//There are some locales so generate the dropdown accordingly, removing all elements but the first
    		$("#newLabelLanguage option:gt(0)").remove();
    		var i;
    		var len = availableLocalesList.length;
    		for(i = 0; i < len; i++) {
    			var localeInfo = availableLocalesList[i];
    			manageLabels.addLocaleInfo(localeInfo);
    		}
    		//Put some value in that shows whether neither add button nor add form were shown
    		//because the form thought there were no available locales
    	}
    },
    
    addLocaleInfo:function(localeInfo) {
    	//Add code to dropdown
    	//Would we need to regenerate alphabetically? Argh.
    	manageLabels.labelLanguage.append("<option value='" + localeInfo["code"] + "'>" + localeInfo["label"] +  "</option>");
    }

};

$(document).ready(function() {   
    manageLabels.onLoad();
}); 
