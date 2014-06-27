/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
var pageManagementUtils = {
	dataGetterLabelToURI:null,//initialized by custom data
	dataGetterURIToLabel:null, //initialized from custom data
	processDataGetterUtils:processDataGetterUtils,//an external class that should exist before this one
	dataGetterMap:null,
	menuAction:null,
	// on initial page setup
	onLoad:function(){
		if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }   
		this.mixIn();
		this.initReverseURIToLabel();
		this.initDataGetterProcessors();
	    this.initObjects();
	    this.bindEventListeners();
	    this.initDisplay();
	    //if edit, then generate existing content
	    if(this.isEdit()) {
	    	this.initExistingContent();
	    }
	}, 
	isEdit:function() {
		if(pageManagementUtils.menuAction != null && pageManagementUtils.menuAction == "Edit") {
			return true;
		}
		return false;
	},
	isAdd:function() {
		if(pageManagementUtils.menuAction != null && pageManagementUtils.menuAction == "Add") {
			return true;
		}
		return false;
	},
	isAddMenuItem:function() {
		if(pageManagementUtils.addMenuItem != null && pageManagementUtils.addMenuItem == "true") {
			return true;
		}
		return false;	
	},
	initExistingContent:function() {
		this.generateExistingContentSections();
		//display more content button - will need to review how to hit save etc. 
        //Don't need to display this b/c already in appended section
		//pageManagementUtils.moreContentButton.show();
		//Need to have additional save button
	},
	initReverseURIToLabel:function() {
		if(this.dataGetterLabelToURI != null) {
			this.dataGetterURIToLabel = {};
			for(var label in this.dataGetterLabelToURI) {
				if(label != undefined) {
					var uri = this.dataGetterLabelToURI[label];
					this.dataGetterURIToLabel[uri] = label;
				}
			}
		} else {
			//Error condition.  
		}
	},
	initDataGetterProcessors:function() {
		//data getter processor map should come in from custom data
		//Go through each and initialize with their class
		
		if(pageManagementUtils.processDataGetterUtils != null) {
			var dataGetterProcessorMap = pageManagementUtils.dataGetterProcessorMap = pageManagementUtils.processDataGetterUtils.dataGetterProcessorMap;
			$.each(dataGetterProcessorMap, function(key, dataGetterProcessorObject) {
				//passes class name from data getter label to uri to processor
				dataGetterProcessorObject.initProcessor(pageManagementUtils.dataGetterLabelToURI[key]);
			})
		}
	
	},
    disableFormInUnsupportedBrowsers: function() {       
        var disableWrapper = $('#ie67DisableWrapper');
        
        // Check for unsupported browsers only if the element exists on the page
        if (disableWrapper.length) {
            if (vitro.browserUtils.isIELessThan8()) {
                disableWrapper.show();
                $('.noIE67').hide();
                return true;
            }
        }            
        return false;      
    },

    mixIn: function() {
    	//Data getter process list input should be retrieved from the custom data
        // Mix in the custom form utility methods
        $.extend(this, vitro.customFormUtils);
        // Get the custom form data from the page
        $.extend(this, customFormData);
        $.extend(this, i18nStrings);
        
    },
	initObjects:function(){
		this.counter = 0;
		this.contentTypeSelect =  $("select#typeSelect");
		//list of options
		this.contentTypeSelectOptions =  $('select#typeSelect option');
		this.classGroupSection = $("section#browseClassGroup");
		this.sparqlQuerySection = $("section#sparqlQuery");
		this.fixedHTMLSection = $("section#fixedHtml");
		this.searchIndividualsSection = $("section#searchIndividuals");
		//From original menu management edit
		this.defaultTemplateRadio = $('input.default-template');
        this.customTemplateRadio = $('input.custom-template');
        this.selfContainedTemplateRadio = $('input.selfContained-template');
        this.customTemplate = $('#custom-template');
        //In this version, these don't exist but we can consider this later
       // this.changeContentType = $('#changeContentType');
       // this.selectContentType = $('#selectContentType');
       // this.existingContentType = $('#existingContentType');
        this.selectClassGroupDropdown = $('select#selectClassGroup');
        this.classesForClassGroup = $('section#classesInSelectedGroup');
        this.selectedGroupForPage = $('#selectedContentTypeValue');
        this.allClassesSelectedCheckbox = $('#allSelected');
        
        this.displayInternalMessage = $('#internal-class label em');
        this.pageContentSubmissionInputs = $("#pageContentSubmissionInputs");
        this.headerBar = $("section#headerBar");
        this.doneButton =  $("input#doneWithContent");
        this.cancelLink =  $("a#cancelContentLink");
        this.isMenuCheckbox = $("input#menuCheckbox");
        this.menuLinkText = $("input#menuLinkText");
        this.menuSection = $("section#menu");
        this.pageNameInput = $("input#pageName");
        this.pageSaveButton = $("input#pageSave");
        this.leftSideDiv = $("div#leftSide");
        this.rightSideDiv = $("div#rightSide");
        //contentDivs container where content added/existing placed
        this.savedContentDivs = $("section#contentDivs");
    	//for search individuals data getter
        this.searchAllClassesDropdown = $("select#vclassUri");
	},
	initDisplay: function(){
		//right side components
	    this.contentTypeSelectOptions.eq(0).attr('selected', 'selected');
	    $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	    
	    //Why would you want to hide this? This hides everything
	   // $("section#pageDetails").hide();
	    this.headerBar.hide();
	    this.classGroupSection.hide();
	    this.sparqlQuerySection.hide();
	    this.fixedHTMLSection.hide();
	    this.searchIndividualsSection.hide();
	    this.classesForClassGroup.addClass('hidden');
	    //left side components
	    //These depend on whether or not this is an existing item or not
	    if(this.isAdd()) {
	    	this.defaultTemplateRadio.attr('checked',true);
	    	//disable save button
	    	this.disablePageSave();
	    	if(!this.isAddMenuItem()) {
		    	this.isMenuCheckbox.attr('checked',false);
		    	this.menuSection.hide();
	    	}
	    }
	    //populates the dropdown of classes for the search individuals template
	    //dropdown now populated in template/from form specific data instead of ajax request
	    //this.populateClassForSearchDropdown();
	},
	//this method can be utilized if using an ajax request to get the vclasses
	/*
	//for search individuals - remember this populates the template class dropdown
	populateClassForSearchDropdown:function() {
	
        //Run ajax query
        var url = "dataservice?getAllVClasses=1";
       
        //Make ajax call to retrieve vclasses
        $.getJSON(url, function(results) {
        	//Moved the function to processClassGroupDataGetterContent
        	//Should probably remove this entire method and copy there
        	pageManagementUtils.displayAllClassesForSearchDropdown(results);
        });
	},
	displayAllClassesForSearchDropdown:function(results) {
		 if ( results.classes.length == 0 ) {
	            
        } else {
        	var appendHtml = "";
            $.each(results.classes, function(i, item) {
                var thisClass = results.classes[i];
                var thisClassName = thisClass.name;
                //Create options for the dropdown
                appendHtml += "<option value='" + thisClass.URI + "'>" + thisClassName + "</option>";
            });
        
            //if there are options to add
            if(appendHtml != "") {
            	pageManagementUtils.searchAllClassesDropdown.html(appendHtml);
            }
         
        }
	},*/
	bindEventListeners:function(){
		
	    this.defaultTemplateRadio.click( function() {
	            pageManagementUtils.customTemplate.addClass('hidden');
	            //Also clear custom template value so as not to submit it
	            pageManagementUtils.clearInputs(pageManagementUtils.customTemplate);
	            pageManagementUtils.rightSideDiv.show(); 
	            //Check to see if there is already content on page, in which case save should be enabled
	        	var pageContentSections = $("section[class='pageContent']");
	        	if(pageContentSections.length == 0) {
	        		pageManagementUtils.disablePageSave();
	        	} 
	    });

	    this.customTemplateRadio.click( function() {
	            pageManagementUtils.handleSelectCustomTemplate();  
	    });
	
	    this.selfContainedTemplateRadio.click( function() {
	            pageManagementUtils.customTemplate.removeClass('hidden');
	            pageManagementUtils.rightSideDiv.hide(); 
	            pageManagementUtils.enablePageSave();           
	    });

	    this.isMenuCheckbox.click( function() {
	        if ( pageManagementUtils.menuSection.is(':hidden') ) {
	            pageManagementUtils.menuSection.show();
	        }
	        else {
	            pageManagementUtils.menuSection.hide();
	        }            
	    });
	
	    //Collapses the current content and creates a new section of content
	    //Resets the content to be cloned to default settings
	    this.doneButton.click( function() {
	       pageManagementUtils.handleClickDone();
	    });
	
	    this.cancelLink.click( function() {
	        pageManagementUtils.clearSourceTemplateValues();
	        pageManagementUtils.headerBar.hide();
            pageManagementUtils.classGroupSection.hide();
            pageManagementUtils.fixedHTMLSection.hide();
            pageManagementUtils.sparqlQuerySection.hide();
            pageManagementUtils.contentTypeSelectOptions.eq(0).attr('selected', 'selected');
            pageManagementUtils.contentTypeSelect.focus();
            pageManagementUtils.adjustSaveButtonHeight();
            pageManagementUtils.checkSelfContainedRadio();
	    });
	    //replacing with menu management edit version which is extended with some of the logic below
	    //This is technically content specific and should be moved into the individual processor classes somehow
	    this.selectClassGroupDropdown.change(function() {
            pageManagementUtils.chooseClassGroup();
        });
	    
	    
	    
	    this.contentTypeSelect.change( function() {
	    	pageManagementUtils.handleContentTypeSelect();
	    });
	    
	    
	    //Submission: validate as well as create appropriate hidden json inputs
	    $("form").submit(function (event) { 
           pageManagementUtils.handleFormSubmission(event);
         });
    
	},
	handleSelectCustomTemplate: function() {
		pageManagementUtils.customTemplate.removeClass('hidden');            
        pageManagementUtils.rightSideDiv.show();
        //Check to see if there is already content on page, in which case save should be enabled
        var pageContentSections = $("section[class='pageContent']");
    	if(pageContentSections.length == 0) {
    		pageManagementUtils.disablePageSave();
    	}          
	},
	
	handleClickDone:function() {
		var selectedType = pageManagementUtils.contentTypeSelect.val();
		var selectedTypeText = $("#typeSelect option:selected").text();
		
		//Hide all sections
		pageManagementUtils.classGroupSection.hide();
		pageManagementUtils.fixedHTMLSection.hide();
		pageManagementUtils.sparqlQuerySection.hide();
		pageManagementUtils.searchIndividualsSection.hide();
		//Reset main content type drop-down
		pageManagementUtils.contentTypeSelectOptions.eq(0).attr('selected', 'selected');
		if ( pageManagementUtils.leftSideDiv.css("height") != undefined ) {
			pageManagementUtils.leftSideDiv.css("height","");
			if ( pageManagementUtils.leftSideDiv.height() < pageManagementUtils.rightSideDiv.height() ) {
			    pageManagementUtils.leftSideDiv.css("height",pageManagementUtils.rightSideDiv.height() + "px");
			}
		}
	   pageManagementUtils.headerBar.hide();
	   pageManagementUtils.headerBar.text("");
	   pageManagementUtils.cloneContentArea(selectedType, selectedTypeText);
	    //Reset class group section AFTER cloning not before
	   pageManagementUtils.resetClassGroupSection();
	   //Clear all inputs values
	   pageManagementUtils.clearSourceTemplateValues();
	 //If adding browse classgroup, need to remove the classgroup option from the dropdown
	   if(selectedType == "browseClassGroup") {
		   pageManagementUtils.handleAddBrowseClassGroupPageContent();
	   }
	   //Enable save button now that some content has been selected
	   pageManagementUtils.enablePageSave();
	   pageManagementUtils.contentTypeSelect.focus();
	
	},
	//Form submission
	handleFormSubmission:function(event) {
		 var validationError = pageManagementUtils.validateMenuItemForm();
	     //Add any errors from page content sections if necessary
	     // Only validate the content sections if the self contained template section is NOT selected tlw72
	     if ( !pageManagementUtils.isSelfContainedTemplateChecked() ) {
	          validationError += pageManagementUtils.validatePageContentSections();
	     }
	     if (validationError == "") {
	    	//Check if menu label needs to be page title
	    	pageManagementUtils.checkMenuTitleSubmission();
	 		//Create the appropriate json objects if necessary
     		pageManagementUtils.createPageContentForSubmission();
     		//pageManagementUtils.mapCustomTemplateName();
     		pageManagementUtils.setUsesSelfContainedTemplateInput();
     		return true;
        } else{
            $('#error-alert').removeClass('hidden');
            $('#error-alert p').html(validationError);
            event.preventDefault();
            return false;
        } 	
	},
	checkMenuTitleSubmission:function() {
		var isMenu = pageManagementUtils.isMenuCheckbox.is(":checked");
		var linkText = pageManagementUtils.menuLinkText.val();
		if(isMenu && linkText == "") {
			//substitute with page title instead
			var pageName = pageManagementUtils.pageNameInput.val();
			pageManagementUtils.menuLinkText.val(pageName);
		}
		if(!isMenu && linkText.length > 0) {
			// if the isMenuCheckbox is unchecked, we need to clear the
			// menuLinkText field; otherwise, the page remains a menu
			pageManagementUtils.menuLinkText.val("");
		}
	},
	
	//Select content type - this is content type specific
	//TODO: Find better way to refactor this and/or see if any of this display functionality can be moved into content type processing
	handleContentTypeSelect:function() {
		_this = pageManagementUtils;
    	pageManagementUtils.clearSourceTemplateValues();
        if ( _this.contentTypeSelect.val() == "browseClassGroup" ) {
            pageManagementUtils.classGroupSection.show();
            pageManagementUtils.fixedHTMLSection.hide();
            pageManagementUtils.sparqlQuerySection.hide();
            pageManagementUtils.searchIndividualsSection.hide();
            pageManagementUtils.headerBar.text(pageManagementUtils.browseClassGroup + " - ");
            pageManagementUtils.headerBar.show();
            $('div#selfContainedDiv').hide();
        }
        if ( _this.contentTypeSelect.val() == "fixedHtml" || _this.contentTypeSelect.val() == "sparqlQuery" || _this.contentTypeSelect.val() == "searchIndividuals") {
        	 pageManagementUtils.classGroupSection.hide();
        	 //if fixed html show that, otherwise show sparql results
            if ( _this.contentTypeSelect.val() == "fixedHtml" ) {
                pageManagementUtils.headerBar.text(pageManagementUtils.fixedHtml + " - ");
                pageManagementUtils.fixedHTMLSection.show();
            	pageManagementUtils.sparqlQuerySection.hide();
            	pageManagementUtils.searchIndividualsSection.hide();
            }
            else if (_this.contentTypeSelect.val() == "sparqlQuery"){
                pageManagementUtils.headerBar.text(pageManagementUtils.sparqlResults + " - ");
                pageManagementUtils.sparqlQuerySection.show();
            	pageManagementUtils.fixedHTMLSection.hide();
            	pageManagementUtils.searchIndividualsSection.hide();
            } else {
            	//search individuals
            	pageManagementUtils.headerBar.text(pageManagementUtils.searchIndividuals + " - ");
                pageManagementUtils.sparqlQuerySection.hide();
            	pageManagementUtils.fixedHTMLSection.hide();
            	pageManagementUtils.searchIndividualsSection.show();
            }
           
            pageManagementUtils.headerBar.show();
            pageManagementUtils.classesForClassGroup.addClass('hidden');
            $('div#selfContainedDiv').hide();
        }
        if ( _this.contentTypeSelect.val() == "" ) {
        	pageManagementUtils.classGroupSection.hide();
        	pageManagementUtils.fixedHTMLSection.hide();
        	pageManagementUtils.sparqlQuerySection.hide();
        	pageManagementUtils.searchIndividualsSection.hide();
            pageManagementUtils.classesForClassGroup.addClass('hidden');
            pageManagementUtils.headerBar.hide();
            pageManagementUtils.headerBar.text("");
            pageManagementUtils.checkSelfContainedRadio();
        }
        //Collapse any divs for existing content if it exists
        pageManagementUtils.collapseAllExistingContent();
        //adjust save button height
        pageManagementUtils.adjustSaveButtonHeight();
        //Disable save button until the user has clicked done or cancel from the addition
        pageManagementUtils.disablePageSave();
        //If the default template is selected, there is already content on the page, and the user is selecting new content
        //display alert message that they must select a custom template and select 
        pageManagementUtils.checkTemplateForMultipleContent(_this.contentTypeSelect.val());
	},
	disablePageSave:function() {
        pageManagementUtils.pageSaveButton.attr("disabled", "disabled");
        pageManagementUtils.pageSaveButton.addClass("disabledSubmit");
	},
	enablePageSave:function() {
        pageManagementUtils.pageSaveButton.removeAttr("disabled");
        pageManagementUtils.pageSaveButton.removeClass("disabledSubmit");
	},
	collapseAllExistingContent:function() {
		var spanArrows = pageManagementUtils.savedContentDivs.find("span.pageContentExpand div.arrow");
		spanArrows.removeClass("collapseArrow");
		spanArrows.addClass("expandArrow");
		pageManagementUtils.savedContentDivs.find("div.pageContentContainer div.pageContentWrapper").slideUp(222);
	},
	//Clear values in content areas that are cloned to create the page content type specific sections
	//i.e. reset sparql query/class group areas
	//TODO: Check if reset is more what we need here?
	clearSourceTemplateValues:function() {
		//inputs, textareas
		pageManagementUtils.clearInputs(pageManagementUtils.fixedHTMLSection);
		pageManagementUtils.clearInputs(pageManagementUtils.sparqlQuerySection);
		pageManagementUtils.clearInputs(pageManagementUtils.classGroupSection);
		pageManagementUtils.clearInputs(pageManagementUtils.searchIndividualsSection);

	},
	clearInputs:function($el) {
		// jquery selector :input selects all input text area select and button elements
	    $el.find("input").each( function() {
	        if ( $(this).attr('id') != "doneWithContent" ) {
	            $(this).val("");
	        }
        });
		$el.find("textarea").val("");
		//resetting class group section as well so selection is reset if type changes
		$el.find("select option:eq(0)").attr("selected", "selected");
		
	},
	checkTemplateForMultipleContent:function(contentTypeSelected) {
		if(contentTypeSelected != "") {
	    	var pageContentSections = $("section[class='pageContent']");
            var selectedTemplateValue = $('input:radio[name=selectedTemplate]:checked').val();
	    	//A new section hasn't been added yet so check to see if there is at least one content type already on page
	    	if(selectedTemplateValue == "default" && pageContentSections.length >= 1) {
	    		//alert the user that they should be picking custom template instead
	    		alert(pageManagementUtils.multipleContentWithDefaultTemplateError);
	    		//pick custom template
	    		 $('input:radio[name=selectedTemplate][value="custom"]').attr("checked", true);
	    		 pageManagementUtils.handleSelectCustomTemplate();  

	    	}
		}
	},
	//Clone content area
	//When adding a new content type, this function will copy the values from the new content form and generate
	//the content for the new section containing the content
	cloneContentArea: function(contentType, contentTypeLabel) {
        var ctr = pageManagementUtils.counter;
        var counter = pageManagementUtils.counter;
        var varOrClass;
        
        //Clone the object, renaming ids and copying text area values as well
        $newContentObj = pageManagementUtils.createCloneObject(contentType, counter);

        // Get rid of the cancel link; it'll be replaced by a delete link
        $newContentObj.find('span#cancelContent' + counter).html('');
        
        if ( contentType == "sparqlQuery" || contentType == "fixedHtml" || contentType == "searchIndividuals") {
        	varOrClass = $newContentObj.find('input[name="saveToVar"]').val();
        } 
        else if ( contentType == "browseClassGroup" ) {
        	$newContentObj.find('section#classesInSelectedGroup' + counter).removeClass('hidden');
        	varOrClass = $newContentObj.find('select#selectClassGroup' + counter + ' option:selected').text();
        }
        //For cases where varOrClass might be empty,  pass an empty string
        if(varOrClass == null || varOrClass==undefined) {
        	varOrClass = "";
        }
        //Attach event handlers if they exist
        pageManagementUtils.bindClonedContentEventHandlers($newContentObj);
        pageManagementUtils.createClonedContentContainer($newContentObj, counter, contentTypeLabel, varOrClass);
        //previously increased by 10, just increasing by 1 here
        pageManagementUtils.counter++;  
    },
    
    //For binding content type specific event handlers should they exist
    bindClonedContentEventHandlers:function($newContentObj) {
    	var dataGetterProcessorObj = pageManagementUtils.getDataGetterProcessorObject($newContentObj);
    	if($.isFunction(dataGetterProcessorObj.bindEventHandlers)) {
    		dataGetterProcessorObj.bindEventHandlers($newContentObj);
    	}
    	//Bind done event as the done button is within the cloned content
    	pageManagementUtils.bindClonedContentDoneEvent($newContentObj);
    },
    createClonedContentContainer:function($newContentObj, counter, contentTypeLabel, varOrClass) {
        //Create the container for the new content
        $newDivContainer = $("<div></div>", {
            id: "divContainer" + counter,
            "class": "pageContentContainer",
            html: "<span class='pageContentTypeLabel'>" + contentTypeLabel + " - " + varOrClass 
                        + "</span><span id='clickable" + counter 
                        + "' class='pageContentExpand'><div id='woof' class='arrow expandArrow'></div></span><div id='innerContainer" + counter 
                        + "' class='pageContentWrapper'><span class='deleteLinkContainer'>&nbsp;" + pageManagementUtils.orString + "&nbsp;<a id='remove" + counter   // changed button to a link
                        + "' href='' >" + pageManagementUtils.deleteString + "</a></span></div>"
        });
        //Hide inner div
        var $innerDiv = $newDivContainer.children('div#innerContainer' + counter);
        $innerDiv.hide();
        //Bind event listers for the new content for display/removal etc.
        pageManagementUtils.bindClonedContentContainerEvents($newDivContainer, counter);
        //Append the new container to the section storing these containers
        $newDivContainer.appendTo($('section#contentDivs'));
        //place new content object        
        $newContentObj.prependTo($innerDiv);
    },
    bindClonedContentDoneEvent:function($newContentObj) {
    	//Done button should just collapse the cloned content
        $newContentObj.find("input[name='doneWithContent']").click(function() {
        		var thisInnerDiv = $(this).closest("div.pageContentWrapper");
                var thisClickableSpan = thisInnerDiv.prev("span.pageContentExpand");
                var thisArrowDiv = thisClickableSpan.find('div.arrow');
                thisInnerDiv.slideUp(222);
                thisArrowDiv.removeClass("collapseArrow");
                thisArrowDiv.addClass("expandArrow");
                window.setTimeout('pageManagementUtils.adjustSaveButtonHeight()', 223);
         
        });	
    },
    bindClonedContentContainerEvents:function($newDivContainer, counter) {
    	 var $clickableSpan = $newDivContainer.children('span#clickable' + counter);
         var $innerDiv = $newDivContainer.children('div#innerContainer' + counter);
    	 //Expand/collapse toggle
        $clickableSpan.click(function() {
            if ( $innerDiv.is(':visible') ) {
               $innerDiv.slideUp(222);
               //$clickableSpan.find('img').attr("src","arrow-down.gif");
               var arrowDiv = $clickableSpan.find('div.arrow');
               arrowDiv.removeClass("collapseArrow");
               arrowDiv.addClass("expandArrow");
            }
            else {
                $innerDiv.slideDown(222);
                //$clickableSpan.find('img').attr("src","arrow-up.gif");
                var arrowDiv = $clickableSpan.find('div.arrow');
                arrowDiv.removeClass("expandArrow");
                arrowDiv.addClass("collapseArrow");
            }
            window.setTimeout('pageManagementUtils.adjustSaveButtonHeight()', 223);             
        });
        
        //remove button
        $newRemoveLink = $innerDiv.find('a#remove' + counter); 
        //remove the content entirely
        $newRemoveLink.click(function(event) {
        	//if content type of what is being deleted is browse class group, then
        	//add browse classgroup back to set of options
        	var contentType = $innerDiv.find("section.pageContent").attr("contentType");
        	if(pageManagementUtils.processDataGetterUtils.isRelatedToBrowseClassGroup(contentType)) {
        		pageManagementUtils.handleRemoveBrowseClassGroupPageContent();
        	}
        	//remove the section
        	$innerDiv.parent("div").remove();
            pageManagementUtils.adjustSaveButtonHeight();
            pageManagementUtils.checkSelfContainedRadio();
            //Because this is now a link, have to prevent default action of navigating to link
            event.preventDefault();
        });
    },
    //clones and returns cloned object for content type
    createCloneObject:function(contentType, counter) {
    	var originalObjectPath = 'section#' + contentType;
    	var $newContentObj = $(originalObjectPath).clone();
 	    $newContentObj.removeClass("contentSectionContainer"); 
 	    $newContentObj.addClass("pageContent"); 
 	    $newContentObj.attr("contentNumber", counter);
 	    //Save content type
	    $newContentObj.attr("contentType", contentType);
	    //Set id for object
	    $newContentObj.attr("id", contentType + counter);
	    $newContentObj.show();
	    pageManagementUtils.renameIdsInClone($newContentObj, counter);
	 //   pageManagementUtils.cloneTextAreaValues(originalObjectPath, $newContentObj);
	    return $newContentObj;
    },
    //This is specifically for cloning text area values
    //May not need this if implementing the jquery fix
    ///would need a similar function for select as well
    cloneTextAreaValues:function(originalAncestorPath, $newAncestorObject) {
    	$(originalAncestorPath + " textarea").each(function(index, el) {
    		var originalTextAreaValue = $(this).val();
    		var originalTextAreaName = $(this).attr("name");
    		$newAncestorObject.find("textarea[name='" + originalTextAreaName + "']").val(originalTextAreaValue);
    	});
    }, 
    //given an object and a counter, rename all the ids
    renameIdsInClone:function($newContentObj, counter) {
    	$newContentObj.find("[id]").each(function(index, el) { 
    		var originalId = $(this).attr("id");
    		var newId = originalId + counter;
    		$(this).attr("id", newId);
    	});
    },
    /**Existing Content**/
  //For edit, need to have text passed in
    //Returns new content object itself
    cloneContentAreaForEdit: function(contentType, contentTypeLabel, labelText) {
           var counter = pageManagementUtils.counter;           
           //Clone the object, renaming ids and copying text area values as well
           $newContentObj = pageManagementUtils.createCloneObject(contentType, counter);
           //Attach done event
         //Bind done event as the done button is within the cloned content
       		pageManagementUtils.bindClonedContentDoneEvent($newContentObj);
       		//create cloned content container
           pageManagementUtils.createClonedContentContainer($newContentObj, counter, contentTypeLabel, labelText);
           //previously increased by 10, just increasing by 1 here
           pageManagementUtils.counter++;  
           return $newContentObj;
    }, 
    //To actually generate the content for existing values
    generateExistingContentSections:function() {
    	if(pageManagementUtils.menuAction == "Edit") {
    		var $existingContent = $("#existingPageContentUnits");
    		//create json object from string version json2.
    		if($existingContent.length > 0) {
    			var jsonObjectString = $existingContent.val();
    			//this returns an array
    			var JSONContentObjectArray = JSON.parse(jsonObjectString);
    			var len = JSONContentObjectArray.length;
    			var i;
    			for(i = 0; i < len; i++) {
    				//Get the type of data getter and create the appropriate section/populate
    				var JSONContentObject = JSONContentObjectArray[i];
	    			var dataGetterClass = JSONContentObject["dataGetterClass"];
	    			if(dataGetterClass != null) {
	    				//Get the Label for the URI
	    				var contentType = pageManagementUtils.dataGetterURIToLabel[dataGetterClass];
	    				var contentTypeForCloning = pageManagementUtils.processDataGetterUtils.getContentTypeForCloning(contentType);
	    				//Get the processor class for this type 
	    				var dataGetterProcessorObject = pageManagementUtils.processDataGetterUtils.dataGetterProcessorMap[contentType];
	    				var contentTypeLabel = dataGetterProcessorObject.retrieveContentLabel();
	    				var additionalLabelText = dataGetterProcessorObject.retrieveAdditionalLabelText(JSONContentObject);
	    				//Clone the appropriate section for the label
	    				var $newContentObj = pageManagementUtils.cloneContentAreaForEdit(contentTypeForCloning, contentTypeLabel, additionalLabelText);
	    				//Populate the section with the values
	    				dataGetterProcessorObject.populatePageContentSection(JSONContentObject, $newContentObj);
	    				//Also include a hidden input with data getter URI
	    				pageManagementUtils.includeDataGetterURI(JSONContentObject, $newContentObj);
	    				//If content type is browseClassGroup or other 'related types' that are derived from it
	    				if(pageManagementUtils.processDataGetterUtils.isRelatedToBrowseClassGroup(contentType)) {
	    					pageManagementUtils.handleAddBrowseClassGroupPageContent();
	    				}
	    			} else {
	    				//error condition
	    			}
    			}
    			
    		}
    		
    	}
    },
    //What's the label of the content type from the drop down
    getContentTypeLabelFromSelect:function(contentType) {
    	var text= pageManagementUtils.contentTypeSelect.find("option[value='" + contentType + "']").text();
    	if(text == null) {
    		text = "";
    	}
    	return text;
    },
    includeDataGetterURI:function(JSONContentObject, $newContentObj) {
    	var uri = JSONContentObject["URI"];
    	if(uri != null) {
    		$("<input type='hidden' name='URI' value='" + uri + "'>").appendTo($newContentObj);
    	}
    },
    
    //Adjust save button height
    adjustSaveButtonHeight:function() {
        if ( $("div#leftSide").css("height") != undefined ) {
             $("div#leftSide").css("height","");
             if ( $("div#leftSide").height() < $("div#rightSide").height() ) {
                 $("div#leftSide").css("height",$("div#rightSide").height() + "px");
             }
        }
    },
    /***Class group selection***/
    //Copied from menu management edit javascript
  //Class group 
    resetClassGroupSection:function() {
    	//doing this in clear inputs instead which will be triggered
    	//every time content type is changed AS well as on  more content button after
    	//original content is cloned and stored
    	//$('select#selectClassGroup option').eq(0).attr('selected', 'selected');
    	 pageManagementUtils.classesForClassGroup.addClass('hidden');
    },
    chooseClassGroup: function() {        
        var url = "dataservice?getVClassesForVClassGroup=1&classgroupUri=";
        var vclassUri = this.selectClassGroupDropdown.val();
        url += encodeURIComponent(vclassUri);
        //Make ajax call to retrieve vclasses
        $.getJSON(url, function(results) {
        	//Moved the function to processClassGroupDataGetterContent
        	//Should probably remove this entire method and copy there
        	pageManagementUtils.displayClassesForClassGroup(results);
        });
    },
    displayClassesForClassGroup:function(results) {
        if ( results.classes.length == 0 ) {
            
        } else {
            //update existing content type with correct class group name and hide class group select again
        //   pageManagementUtils.hideClassGroups();
    
            pageManagementUtils.selectedGroupForPage.html(results.classGroupName);
            //update content type in message to "display x within my institution"
            //SPECIFIC TO VIVO: So include in internal CLASS section instead
            pageManagementUtils.updateInternalClassMessage(results.classGroupName);
            //retrieve classes for class group and display with all selected
            var selectedClassesList =  pageManagementUtils.classesForClassGroup.children('ul#selectedClasses');
            
            selectedClassesList.empty();
            selectedClassesList.append('<li class="ui-state-default"> <input type="checkbox" name="allSelected" id="allSelected" value="all" checked="checked" /> <label class="inline" for="All"> ' + pageManagementUtils.allCapitalized + '</label> </li>');
            
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
            pageManagementUtils.toggleClassSelection();
            
            
            //From NEW code
            if (pageManagementUtils.selectClassGroupDropdown.val() == "" ) {
          	  pageManagementUtils.classesForClassGroup.addClass('hidden');
	            $("div#leftSide").css("height","");
	            
	  	        }
	  	     else {
	  	    	 	pageManagementUtils.classesForClassGroup.removeClass('hidden');
	  	            if ( $("div#leftSide").height() < $("div#rightSide").height() ) {
	  	                $("div#leftSide").css("height",$("div#rightSide").height() + "px");
	  	            }          
	  	     }
        }
    },
    toggleClassSelection: function() {
        // Check/unckeck all classes for selection
        $('input:checkbox[name=allSelected]').click(function(){
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

        $('input:checkbox[name=classInClassGroup]').click(function(){
            $(this).closest("ul").find('input:checkbox[name=allSelected]').removeAttr('checked');
        });
    }, //This is SPECIFIC to VIVO so should be moved there
    updateInternalClassMessage:function(classGroupName) { //User has changed content type 
        //Set content type within internal class message
        this.displayInternalMessage.filter(":first").html(classGroupName);
    },
    
    /**On submission**/
    //On submission, generates the content for the input that will return the serialized JSON objects representing
    //each of the content types
    createPageContentForSubmission: function() {
    	//Iterate through the page content and create the appropriate json object and save
    	var pageContentSections = $("section[class='pageContent']");
    	$.each(pageContentSections, function(i) {
    		var id = $(this).attr("id");
    		var jo = pageManagementUtils.processPageContentSection($(this));
    		var jsonObjectString = JSON.stringify(jo);
    		//Create a new hidden input with a specific name and assign value per page content
        	pageManagementUtils.createPageContentInputForSubmission(jsonObjectString);
    	});
    	//For the case where the template only selection is picked, there will be
    	//no page contents, but the hidden input still needs to be created as it is expected
    	//to exist by the edit configuration, this creates the hidden input with an empty value
	     if (pageManagementUtils.isSelfContainedTemplateChecked() ) {
	    	 	//An empty string as no content selected
	        	pageManagementUtils.createPageContentInputForSubmission("");
	     }
    },
    createPageContentInputForSubmission: function(inputValue) {
    	//Previously, this code created the hidden input and included the value inline
    	//but this was converting html encoding for quotes/single quotes into actual quotes
    	//which prevented correct processing as the html thought the string had ended
    	//Creating the input and then using the val() method preserved the encoding
    	var pageContentUnit = $("<input type='hidden' name='pageContentUnit'>");
    	pageContentUnit.val(inputValue);
    	pageContentUnit.appendTo(pageManagementUtils.pageContentSubmissionInputs);
    },
    //returns a json object with the data getter information required
    processPageContentSection:function(pageContentSection) {
    	//This processing should be specific to the type and so that content type's specific processor will
    	//return the json object required
    	if(pageManagementUtils.processDataGetterUtils != null) {
			var dataGetterType = pageManagementUtils.processDataGetterUtils.selectDataGetterType(pageContentSection);
			if(pageManagementUtils.dataGetterProcessorMap != null) {
				var dataGetterProcessor = pageManagementUtils.dataGetterProcessorMap[dataGetterType];
				//the content type specific processor will create the json object to be returned
				var jsonObject = dataGetterProcessor.processPageContentSection(pageContentSection);
				//if data getter uri included, include that as well
				if(pageContentSection.find("input[name='URI']").length > 0) {
					var uriValue = pageContentSection.find("input[name='URI']").val();
					jsonObject["URI"] = uriValue;
				}
				return jsonObject;
			} else {
				//ERROR handling
		    	alert(pageManagementUtils.mapProcessorError);
				return null;
			} 
		}
    	alert(pageManagementUtils.codeProcessingError);
    	//Error handling here
    	return null;
    },
  
    //Get the data getter processor
    getDataGetterProcessorObject:function(pageContentSection) {
    	var dataGetterType = pageManagementUtils.processDataGetterUtils.selectDataGetterType(pageContentSection);
		var dataGetterProcessor = null;
    	if(pageManagementUtils.dataGetterProcessorMap != null) {
			dataGetterProcessor = pageManagementUtils.dataGetterProcessorMap[dataGetterType];
		}
    	return dataGetterProcessor;
    },
    handleAddBrowseClassGroupPageContent:function() {
    	//if browse class group content has been added, then remove browse classgroup option from dropdown
    	if(pageManagementUtils.contentTypeSelect.find("option[value='browseClassGroup']").length > 0) {
    		pageManagementUtils.contentTypeSelect.find("option[value='browseClassGroup']").remove();
    	}
    },
    handleRemoveBrowseClassGroupPageContent:function() {
    	if(pageManagementUtils.contentTypeSelect.find("option[value='browseClassGroup']").length == 0) {
	    	//if removed, add browse class group back
	    	var classGroupOption = '<option value="browseClassGroup">Browse Class Group</option>';           
	    	pageManagementUtils.contentTypeSelect.find('option:eq(0)').after(classGroupOption);
    	}
    },
    //get label of page content section
    getPageContentSectionLabel:function(pageContentSection) {
    	var label = pageContentSection.closest("div.pageContentContainer").find("span.pageContentTypeLabel").html();
    	if(label == null) {
    		label = "";
    	}
    	return label;
    },
    /***Validation***/
    validateMenuItemForm: function() {
        var validationError = "";
        
        // Check menu name
        if ($('input[type=text][name=pageName]').val() == "") {
            validationError += pageManagementUtils.supplyName + "<br />";
            }
        // Check pretty url     
        if ($('input[type=text][name=prettyUrl]').val() == "") {
            validationError += pageManagementUtils.supplyPrettyUrl + "<br />";
        }
        if ($('input[type=text][name=prettyUrl]').val().charAt(0) != "/") {
            validationError += pageManagementUtils.startUrlWithSlash + "<br />";
        }
        
        // Check custom template and self contained template
        var selectedTemplateValue = $('input:radio[name=selectedTemplate]:checked').val();
        if (selectedTemplateValue == "custom" || selectedTemplateValue == "selfContained") {
            if ($('input[name=customTemplate]').val() == "") {
                validationError += pageManagementUtils.supplyTemplate + "<br />"; 
            }
        }
        
       
        return validationError;
    },
    //Validation across different content types
    validatePageContentSections:function() {
    	//Get all page content sections
    	var pageContentSections = $("section[class='pageContent']");
    	var validationErrorMsg = "";
    	//If there ARE not contents selected, then error message should indicate user needs to add them
    	if(pageContentSections.length == 0) {
    		validationErrorMsg = pageManagementUtils.selectContentType + " <br /> ";
    	} else {
    		//If there are multiple content types, and the default template option is selected, then display error message
            if(pageContentSections.length > 1) {
	    		var selectedTemplateValue = $('input:radio[name=selectedTemplate]:checked').val();
	            if(selectedTemplateValue == "default") {
	            	validationErrorMsg += pageManagementUtils.multipleContentWithDefaultTemplateError + "<br/>";
	            }
            }
    		//For each, based on type, validate if a validation function exists
	    	$.each(pageContentSections, function(i) {
	    		if(pageManagementUtils.processDataGetterUtils != null) {
	    			var dataGetterType = pageManagementUtils.processDataGetterUtils.selectDataGetterType($(this));
	    			if(pageManagementUtils.dataGetterProcessorMap != null) {
	    				var dataGetterProcessor = pageManagementUtils.dataGetterProcessorMap[dataGetterType];
	    				//the content type specific processor will create the json object to be returned
	    				if($.isFunction(dataGetterProcessor.validateFormSubmission)) {
	    					//Get label of page content section
	    					var label = pageManagementUtils.getPageContentSectionLabel($(this));
	    					validationErrorMsg += dataGetterProcessor.validateFormSubmission($(this), label);
	    				}
	    			}
	    		}
	    	});
    	}
    	return validationErrorMsg;
    },

    //If the selfContained-template radio is checked, copy the custom template name to the hidden
    //selfContainedTemplate input element. We need that for edit mode to select the correct radio button.
    mapCustomTemplateName:function() {
        if ( pageManagementUtils.selfContainedTemplateRadio.is(':checked') ) {
            $("input[name='selfContainedTemplate']").val($("input[name='customTemplate']").val());
        }
    },
    
    setUsesSelfContainedTemplateInput:function() {
    	//On form submission attach hidden input to form if the custom template selection is picked
        if ( pageManagementUtils.isSelfContainedTemplateChecked() ) {
        	$("<input name='isSelfContainedTemplate' value='true'>").appendTo($("form"));
        }
    },
    
    //If any content is defined, keep the selContained radio button hidden
    checkSelfContainedRadio:function() {
        if ( pageManagementUtils.savedContentDivs.html().length == 0 ) {
            $('div#selfContainedDiv').show();
        }
        
    },
    isSelfContainedTemplateChecked:function() {
    	return pageManagementUtils.selfContainedTemplateRadio.is(':checked');
    }

}
    
$(document).ready(function() {
   pageManagementUtils.onLoad();
});


