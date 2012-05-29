/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
var pageManagementUtils = {
	dataGetterLabelToURI:null,//initialized by custom data
	processDataGetterUtils:processDataGetterUtils,//an external class that should exist before this one
	// on initial page setup
	onLoad:function(){
		if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }   
		this.mixIn();
		this.initDataGetterProcessors(),
	    this.initObjects();
	    this.bindEventListeners();
	    this.initDisplay();
	    
	},   
	initDataGetterProcessors:function() {
		//data getter processor map should come in from custom data
		//Go through each and initialize with their class
		
		if(pageManagementUtils.processDataGetterUtils != null) {
			var dataGetterProcessorMap = pageManagementUtils.processDataGetterUtils.dataGetterProcessorMap;
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
    },
	initObjects:function(){
		this.counter = 0;
		this.contentTypeSelect =  $("#typeSelect");
		//list of options
		this.contentTypeSelectOptions =  $('select#typeSelect option');
		this.classGroupSection = $("section#browseClassGroup");
		this.sparqlQuerySection = $("section#sparqlQuery");
		this.fixedHTMLSection = $("section#fixedHtml");
		//From original menu management edit
		this.defaultTemplateRadio = $('input.default-template');
        this.customTemplateRadio = $('input.custom-template');
        this.customTemplate = $('#custom-template');
        //In this version, these don't exist but we can consider this later
       // this.changeContentType = $('#changeContentType');
        this.selectContentType = $('#selectContentType');
       // this.existingContentType = $('#existingContentType');
        this.selectClassGroupDropdown = $('#selectClassGroup');
        this.classesForClassGroup = $('#classesInSelectedGroup');
        this.selectedGroupForPage = $('#selectedContentTypeValue');
        this.allClassesSelectedCheckbox = $('#allSelected');
        this.displayInternalMessage = $('#internal-class label em');
        this.pageContentSubmissionInputs = $("#pageContentSubmissionInputs");
        this.headerBar = $("section#headerBar");
        this.moreContentButton =  $("input#moreContent");
        this.isMenuCheckbox = $("input#menuCheckbox");
        this.menuSection = $("section#menu");
        this.submitButton = $("input#submit");
        this.leftSideDiv = $("div#leftSide");
        this.rightSideDiv = $("div#ri;ghtSide")
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
	    this.classesForClassGroup.addClass('hidden');
	    this.moreContentButton.hide();
	    //left side components
	    this.defaultTemplateRadio.attr('checked',true);
	    this.isMenuCheckbox.attr('checked',false);
	    this.menuSection.hide();
	
	},
	bindEventListeners:function(){
		
	    this.defaultTemplateRadio.click( function() {
	            pageManagementUtils.customTemplateRadio.addClass('hidden');
	            
	    });

	    this.customTemplateRadio.click( function() {
	            pageManagementUtils.defaultTemplateRadio.removeClass('hidden');            
	    });
	
	    this.isMenuCheckbox.click( function() {
	        if ( pageManagementUtils.menuSection.is(':hidden') ) {
	            pageManagementUtils.menuSection.show();
	        }
	        else {
	            pageManagementUtils.menuSection.hide();
	        }            
	    });
	
	   this.submitButton.click( function() {
	        $("section#pageDetails").show();
	    });
	
	    //Collapses the current content and creates a new section of content
	    //Resets the content to be cloned to default settings
	    this.moreContentButton.click( function() {
	        var selectedType = pageManagementUtils.contentTypeSelect.val();
	        var selectedTypeText = $("#typeSelect option:selected").text();
	        //Not sure why selected group here? This won't always be true for more content
	        //var selectedGroup = $('select#selectClassGroup').val();
	        
	        //Aren't these already hidden? 
	        //Hide both sections
	        pageManagementUtils.classGroupSection.hide();
	        pageManagementUtils.fixedHTMLSection.hide();
	        pageManagementUtils.sparqlQuerySection.hide();
	        
	        //Reset class group
	        pageManagementUtils.resetClassGroupSection();
	        pageManagementUtils.contentTypeSelectOptions.eq(0).attr('selected', 'selected');
	        pageManagementUtils.moreContentButton.hide();
	        if ( pageManagementUtils.leftSideDiv.css("height") != undefined ) {
	            pageManagementUtils.leftSideDiv.css("height","");
	            if ( pageManagementUtils.leftSideDiv.height() < pageManagementUtils.rightSideDiv.height() ) {
	                pageManagementUtils.leftSideDiv.css("height",pageManagementUtils.rightSideDiv.height() + "px");
	            }
	        }
	       pageManagementUtils.headerBar.hide();
	       pageManagementUtils.headerBar.text("");
	        //pageManagementUtils.cloneContentArea(selectedType,selectedGroup);
	        pageManagementUtils.cloneContentArea(selectedType, selectedTypeText);
	        pageManagementUtils.contentTypeSelect.focus();
	    });
	
	    //replacing with menu management edit version which is extended with some of the logic below
	    this.selectClassGroupDropdown.change(function() {
            pageManagementUtils.chooseClassGroup();
        });
	    /*
	    $("select#selectClassGroup").change( function() {
	        if ( $("select#selectClassGroup").val() == "" ) {
	            $("section#classesInSelectedGroup").addClass('hidden');
	            $("div#leftSide").css("height","");
	            $("input#moreContent").hide();
	            
	        }
	        else {
	            $("section#classesInSelectedGroup").removeClass('hidden');
	            $("input#moreContent").show();
	            if ( $("div#leftSide").height() < $("div#rightSide").height() ) {
	                $("div#leftSide").css("height",$("div#rightSide").height() + "px");
	            }          
	        }
	    });*/
	  //TODO: These will all change
	    $("select#typeSelect").change( function() {
	    	_this = pageManagementUtils;
	        $('input#variable').val("");
	        $('textarea#textArea').val("");
	        if ( _this.contentTypeSelect.val() == "browseClassGroup" ) {
	            pageManagementUtils.classGroupSection.show();
	            pageManagementUtils.fixedHTMLSection.hide();
	            pageManagementUtils.sparqlQuerySection.hide();
	            $("input#moreContent").hide();
	            $("section#headerBar").text("Browse Class Group - ");
	            $("section#headerBar").show();
	        }
	        if ( _this.contentTypeSelect.val() == "fixedHtml" || _this.contentTypeSelect.val() == "sparqlQuery" ) {
	        	 pageManagementUtils.classGroupSection.hide();
	            if ( _this.contentTypeSelect.val() == "fixedHtml" ) {
	                $('span#taSpan').text("Enter fixed HTML here");
	                $("section#headerBar").text("Fixed HTML - ");
	            }
	            else {
	                $('span#taSpan').text("Enter SPARQL query here");
	                $("section#headerBar").text("SPARQL Query Results - ");
	            }
	            //if fixhed html show that, otherwise show sparq
	            if(_this.contentTypeSelect.val() == "fixedHtml") {
	            	pageManagementUtils.fixedHTMLSection.show();
	            	pageManagementUtils.sparqlQuerySection.hide();
	            } else {
	            	 pageManagementUtils.sparqlQuerySection.show();
		            	pageManagementUtils.fixedHTMLSection.hide();
	            }
	            $("section#headerBar").show();
	            $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	            $("section#classesInSelectedGroup").addClass('hidden');
	            $("input#moreContent").show();
	        }
	        if ( _this.contentTypeSelect.val() == "" ) {
	            $("section#classGroup").hide();
	            $("section#nonClassGroup").hide();
	            $("input#moreContent").hide();
	            $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	            $("section#classesInSelectedGroup").addClass('hidden');
	            $("section#headerBar").hide();
	            $("section#headerBar").text("");
	        }
	        pageManagementUtils.adjustSaveButtonHeight();
	    });
	    
	    /*
	    // Listeners for vClass switching
        this.changeContentType.click(function() {
           pageManagementUtils.showClassGroups();
         
           return false;
        });*/
	    
	    //Submission: validate as well as create appropriate hidden json inputs
	    $("form").submit(function () { 
            var validationError = pageManagementUtils.validateMenuItemForm();
            if (validationError == "") {
            		//Create the appropriate json objects
            		pageManagementUtils.createPageContentForSubmission();
            		//return true;
            		//For testing, not submitting anything
            		return false;
               } else{
            	   
                   $('#error-alert').removeClass('hidden');
                   $('#error-alert p').html(validationError);
                   //TODO: Check why scrolling appears to be a problem
                   $.scrollTo({ top:0, left:0}, 500)
                   return false;
               } 
         });
    
	},
	//Clone content area
	cloneContentArea: function(contentType, contentTypeLabel) {
        var ctr = pageManagementUtils.counter;
        var counter = pageManagementUtils.counter;
        var varOrClass;
        
        //Clone the object, renaming ids and copying text area values as well
        $newContentObj = pageManagementUtils.createCloneObject(contentType, counter);
        
        if ( contentType == "fixedHTML" || contentType == "sparqlQuery") {
        	varOrClass = $newContentObj.find('input#saveToVar').val();
        }
        else if ( contentType == "browseClassGroup" ) {
        	$newContentObj.find('section#classesInSelectedGroup' + counter).removeClass('hidden');
        	varOrClass = $newContentObj.find('select#selectClassGroup' + counter + ' option:selected').text();
        }
        
        pageManagementUtils.createClonedContentContainer($newContentObj, counter, contentTypeLabel, varOrClass);
        //previously increased by 10, just increasing by 1 here
        counter++;  
    },
    createClonedContentContainer:function($newContentObj, counter, contentTypeLabel, varOrClass) {
        //Create the container for the new content
        $newDivContainer = $("<div></div>", {
            id: "divContainer" + counter,
            "class": "pageContentContainer",
            html: "<span class='pageContentTypeLabel'>" + contentTypeLabel + " - " + varOrClass 
                        + "</span><span id='clickable" + counter 
                        + "' class='pageContentExpand'><div class='arrow expandArrow'></div></span><div id='innerContainer" + counter 
                        + "' class='pageContentWrapper'><input id='remove" + counter 
                        + "' type='button' class='delete' value='Delete' class='deleteButton' /></div>"
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
        $newRemoveButton = $innerDiv.find('input#remove' + counter);
        // this will have to disable submitted fields as well as hide them.
        $newRemoveButton.click(function() {
            $innerDiv.parent("div").css("display","none");
            pageManagementUtils.adjustSaveButtonHeight();
        });
    },
    resetClassGroupSection:function() {
    	 $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	     $("section#classesInSelectedGroup").addClass('hidden');
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
    /************************************/
    //Copied from menu management edit javascript
    chooseClassGroup: function() {        
        var url = "dataservice?getVClassesForVClassGroup=1&classgroupUri=";
        var vclassUri = this.selectClassGroupDropdown.val();
        url += encodeURIComponent(vclassUri);
        //Make ajax call to retrieve vclasses
        $.getJSON(url, function(results) {
  
          if ( results.classes.length == 0 ) {
     
          } else {
              //update existing content type with correct class group name and hide class group select again
          //   pageManagementUtils.hideClassGroups();
      
              pageManagementUtils.selectedGroupForPage.html(results.classGroupName);
              //update content type in message to "display x within my institution"
              pageManagementUtils.updateInternalClassMessage(results.classGroupName);
              //retrieve classes for class group and display with all selected
              var selectedClassesList = pageManagementUtils.classesForClassGroup.children('ul#selectedClasses');
              
              selectedClassesList.empty();
              selectedClassesList.append('<li class="ui-state-default"> <input type="checkbox" name="allSelected" id="allSelected" value="all" checked="checked" /> <label class="inline" for="All"> All</label> </li>');
              
              $.each(results.classes, function(i, item) {
                  var thisClass = results.classes[i];
                  var thisClassName = thisClass.name;
                  //When first selecting new content type, all classes should be selected
                  appendHtml = ' <li class="ui-state-default">' + 
                          '<input type="checkbox" checked="checked" name="classInClassGroup" value="' + thisClass.URI + '" />' +  
                         '<label class="inline" for="' + thisClassName + '"> ' + thisClassName + '</label>' + 
                          '</li>';
                  selectedClassesList.append(appendHtml);
              });
              pageManagementUtils.toggleClassSelection();
              
              
              //From NEW code
              if (pageManagementUtils.selectClassGroupDropdown.val() == "" ) {
  	            $("section#classesInSelectedGroup").addClass('hidden');
  	            $("div#leftSide").css("height","");
  	            $("input#moreContent").hide();
  	            
	  	        }
	  	     else {
	  	            $("section#classesInSelectedGroup").removeClass('hidden');
	  	            $("input#moreContent").show();
	  	            if ( $("div#leftSide").height() < $("div#rightSide").height() ) {
	  	                $("div#leftSide").css("height",$("div#rightSide").height() + "px");
	  	            }          
	  	     }
              
              
          }
 
        });
    },
    toggleClassSelection: function() {
        // Check/unckeck all classes for selection
        $('input:checkbox[name=allSelected]').click(function(){
             if ( this.checked ) {
             // if checked, select all the checkboxes
             $('input:checkbox[name=classInClassGroup]').attr('checked','checked');

             } else {
             // if not checked, deselect all the checkboxes
               $('input:checkbox[name=classInClassGroup]').removeAttr('checked');
             }
        });

        $('input:checkbox[name=classInClassGroup]').click(function(){
            $('input:checkbox[name=allSelected]').removeAttr('checked');
        });
    }, //This is SPECIFIC to VIVO so should be moved there
    updateInternalClassMessage:function(classGroupName) { //User has changed content type 
        //Set content type within internal class message
        this.displayInternalMessage.filter(":first").html(classGroupName);
    },
    validateMenuItemForm: function() {
        var validationError = "";
        
        // Check menu name
        if ($('input[type=text][name=pageName]').val() == "") {
            validationError += "You must supply a name<br />";
            }
        // Check pretty url     
        if ($('input[type=text][name=prettyUrl]').val() == "") {
            validationError += "You must supply a pretty URL<br />";
        }
        if ($('input[type=text][name=prettyUrl]').val().charAt(0) != "/") {
            validationError += "The pretty URL must begin with a leading forward slash<br />";
        }
        
        // Check custom template
        if ($('input:radio[name=selectedTemplate]:checked').val() == "custom") {
            if ($('input[name=customTemplate]').val() == "") {
                validationError += "You must supply a template<br />"; 
            }
        }
        
        //the different types of error will depend on the specific type of data getter/page content
        /*
        
        // if no class group selected, this is an error
        if ($('#selectClassGroup').val() =='-1') {
            validationError += "You must supply a content type<br />"; 
        } else {
            //class group has been selected, make sure there is at least one class selected
            var allSelected = $('input[name="allSelected"]:checked').length;
            var noClassesSelected = $('input[name="classInClassGroup"]:checked').length;
            if (allSelected == 0 && noClassesSelected == 0) {
                //at least one class should be selected
                validationError += "You must select the type of content to display<br />";
            }
        }
      */
       
        //check select class group
       
        return validationError;
    },
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
    	
    },
    createPageContentInputForSubmission: function(inputValue) {
    	$("<input type='hidden' name='pageContentUnit' value='" + inputValue + "'>").appendTo(pageManagementUtils.pageContentSubmissionInputs);
    },
    //returns a json object with the data getter information required
    processPageContentSection:function(pageContentSection) {
    	//This processing should be specific to the type and so that content type's specific processor will
    	//return the json object required
    	if(pageManagementUtils.processDataGetterUtils != null) {
			var dataGetterType = pageManagementUtils.processDataGetterUtils.selectDataGetterType(pageContentSection);
			if(dataGetterProcessorMap != null) {
				var dataGetterProcessor = dataGetterProcessorMap[dataGetterType];
				dataGetterProcessor.processPageContentSection(pageContentSection);
				return dataGetterProcessor;
			} else {
				//ERROR handling
		    	alert("An error has occurred and the map of processors for this content is missing. Please contact the administrator");
				return null;
			} 
		}
    	alert("An error has occurred and the code for processing this content is missing a component. Please contact the administrator.");
    	//Error handling here
    	return null;
    },
    //clones and returns cloned object for content type
    createCloneObject:function(contentType, counter) {
    	var originalObjectPath = 'section#' + contentType;
    	var $newContentObj = $(originalObjectPath).clone();
 	    $newContentObj.addClass("pageContent"); 
 	    //Save content type
	    $newContentObj.attr("contentType", contentType);
	    //Set id for object
	    $newContentObj.attr("id", "contentType" + counter);
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
    		var originalId = $(this).attr["id"];
    		var newId = originalId + counter;
    		$(this).attr(newId);
    	});
    }
    
};

$(document).ready(function() {
   pageManagementUtils.onLoad();
});


