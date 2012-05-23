/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
var pageManagementUtils = {
		
	// on initial page setup
	onLoad:function(){
		if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }   
		this.mixIn();
	    this.initObjects();
	    this.bindEventListeners();
	    this.initDisplay();
	    
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
		this.classGroupSection = $("section#classGroup");
		this.nonClassGroupSection = $("section#nonClassGroup");
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
	},
	initDisplay: function(){
		//right side components
	    this.contentTypeSelectOptions.eq(0).attr('selected', 'selected');
	    $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	    
	    //Why would you want to hide this? This hides everything
	   // $("section#pageDetails").hide();
	    $("section#headerBar").hide();
	    this.classGroupSection.hide();
	    this.nonClassGroupSection.hide();
	    $("section#classesInSelectedGroup").addClass('hidden');
	    $("input#moreContent").hide();
	    //left side components
	    $("input.default-template").attr('checked',true);
	    $("input#menuCheckbox").attr('checked',false);
	    $("section#menu").hide();
	
	},
	bindEventListeners:function(){
	    $("input.default-template").click( function() {
	            $("section#custom-template").addClass('hidden');
	            
	    });

	    $("input.custom-template").click( function() {
	            $("section#custom-template").removeClass('hidden');            
	    });
	
	    $("input#menuCheckbox").click( function() {
	        if ( $("section#menu").is(':hidden') ) {
	            $("section#menu").show();
	        }
	        else {
	            $("section#menu").hide();
	        }            
	    });
	
	    $("input#submit").click( function() {
	        $("section#pageDetails").show();
	    });
	
	    //Collapses the current content and creates a new section of content
	    //Resets the content to be cloned to default settings
	    $("input#moreContent").click( function() {
	        var selectedType = pageManagementUtils.contentTypeSelect.val();
	        var selectedTypeText = $("#typeSelect option:selected").text();
	        //Not sure why selected group here? This won't always be true for more content
	        //var selectedGroup = $('select#selectClassGroup').val();
	        
	        //Aren't these already hidden? 
	        //Hide both sections
	        $("section#classGroup").hide();
	        $("section#nonClassGroup").hide();
	        
	        //Reset class group
	        pageManagementUtils.resetClassGroupSection();
	        pageManagementUtils.contentTypeSelectOptions.eq(0).attr('selected', 'selected');
	        $("input#moreContent").hide();
	        if ( $("div#leftSide").css("height") != undefined ) {
	            $("div#leftSide").css("height","");
	            if ( $("div#leftSide").height() < $("div#rightSide").height() ) {
	                $("div#leftSide").css("height",$("div#rightSide").height() + "px");
	            }
	        }
	        $("section#headerBar").hide();
	        $("section#headerBar").text("");
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
	  
	    $("select#typeSelect").change( function() {
	        $('input#variable').val("");
	        $('textarea#textArea').val("");
	        if ( $("#typeSelect").val() == "browseClassGroup" ) {
	            $("section#classGroup").show();
	            $("section#nonClassGroup").hide();
	            $("input#moreContent").hide();
	            $("section#headerBar").text("Browse Class Group - ");
	            $("section#headerBar").show();
	        }
	        if ( $("#typeSelect").val() == "fixedHtml" || $("#typeSelect").val() == "sparqlQuery" ) {
	            $("section#classGroup").hide();
	            if ( $("#typeSelect").val() == "fixedHtml" ) {
	                $('span#taSpan').text("Enter fixed HTML here");
	                $("section#headerBar").text("Fixed HTML - ");
	            }
	            else {
	                $('span#taSpan').text("Enter SPARQL query here");
	                $("section#headerBar").text("SPARQL Query Results - ");
	            }
	            $("section#nonClassGroup").show();
	            $("section#headerBar").show();
	            $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	            $("section#classesInSelectedGroup").addClass('hidden');
	            $("input#moreContent").show();
	        }
	        if ( $("#typeSelect").val() == "" ) {
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
            		return true;
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
        var varOrClas;
        
        
        if ( contentType == "fixedHTML" || contentType == "sparqlQuery" ) {
            var taValue = $('textarea#textArea').val();
            alert("original text area value is " + taValue);
            var $newContentObj = $('section#nonClassGroup').clone();
            $newContentObj.addClass("pageContent");
            varOrClass = $newContentObj.find('input').val();
            $newContentObj.show();
            //Save content type
            $newContentObj.attr("contentType", contentType);
            $newContentObj.attr("id", contentType + counter);
            $newContentObj.find('input#variable').attr("id","variable" + counter);
            $newContentObj.find('textarea#textArea').attr("id","textArea" + counter);
            $newContentObj.find('label#variableLabel').attr("id","variableLabel" + counter);
            $newContentObj.find('label#taLabel').attr("id","taLabel" + counter);

            //Keep the name of the inputs the same
         //   $newContentObj.find('input#variable').attr("name","variable" + counter);
         //   $newContentObj.find('textarea#textArea').attr("name","textArea" + counter);
            // There's a jquery bug when cloning textareas: the value doesn't
			// get cloned. So
            // copy the value "manually."
            $newContentObj.find("textarea[name='textArea']").val(taValue);
        }
        else if ( contentType == "browseClassGroup" ) {
            
            var $newContentObj = $('section#classGroup').clone();

            $newContentObj.addClass("pageContent");
            $newContentObj.show();   
            $newContentObj.attr("contentType", contentType);
            $newContentObj.attr("id", "classGroup" + counter);
            $newContentObj.find('section#selectContentType').attr("id", "selectContentType" + counter);
            $newContentObj.find('select#selectClassGroup').val(groupValue);
            $newContentObj.find('select#selectClassGroup').attr("id","selectClassGroup" + counter);
            $newContentObj.find('select#selectClassGroup' + counter).attr("name","selectClassGroup" + counter);
            $newContentObj.find('section#classesInSelectedGroup').attr("id","classesInSelectedGroup" + counter);
            $newContentObj.find('section#classesInSelectedGroup' + counter).removeClass('hidden');
            $newContentObj.find('p#selectClassesMessage').attr("id","selectClassesMessage" + counter);
            // Will need to uncomment this and find a way to apply the css style
// $newContentObj.find('section#internal-class').attr("id","internal-class" +
// counter);
            $newContentObj.find("input[name='display-internalClass']").attr("name","display-internalClass" + counter);
            $newContentObj.find('ul#selectedClasses').attr("id","selectedClasses" + counter);
            $newContentObj.find('ul#selectedClasses' + counter).attr("name","selectedClasses" + counter);

            $newContentObj.find('ul#selectedClasses' + counter).children("li").each( function(i,abc) {
                var $theCheckbox =  $(this).find('input');
                $theCheckbox.attr("name", $theCheckbox.attr("name") + counter);
            });
            
            varOrClass = $newContentObj.find('select#selectClassGroup' + counter + ' option:selected').text();
        }
        
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
        var $clickableSpan = $newDivContainer.children('span#clickable' + counter);
        var $innerDiv = $newDivContainer.children('div#innerContainer' + counter);
        $innerDiv.hide();
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
        $newRemoveButton = $innerDiv.find('input#remove' + counter);
        // this will have to disable submitted fields as well as hide them.
        $newRemoveButton.click(function() {
            $innerDiv.parent("div").css("display","none");
            pageManagementUtils.adjustSaveButtonHeight();
        });

        $newDivContainer.appendTo($('section#contentDivs'));
        $newContentObj.prependTo($innerDiv);
        counter = counter + 10;  
    },
    resetClassGroupSection:function() {
    	 $('select#selectClassGroup option').eq(0).attr('selected', 'selected');
	     $("section#classesInSelectedGroup").addClass('hidden');
    },
    //finalize later, but basically use same attribute across page content and use attribute instead of id
    //Attribute would be what keeps track of content, so contentCounter or something like that
    toggleArrow:function() {
    	
       
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
              var _this = pageManagementUtils;
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
    /*
    showClassGroups: function() { //User has clicked change content type
        //Show the section with the class group dropdown
        this.selectContentType.removeClass("hidden");
        //Hide the "change content type" section which shows the selected class group
       this.existingContentType.addClass("hidden");
        //Hide the checkboxes for classes within the class group
        this.classesForClassGroup.addClass("hidden");
    },
    hideClassGroups: function() { //User has selected class group/content type, page should show classes for class group and 'existing' type with change link
        //Hide the class group dropdown
        this.selectContentType.addClass("hidden");
        //Show the "change content type" section which shows the selected class group
        this.existingContentType.removeClass("hidden");
        //Show the classes in the class group
        this.classesForClassGroup.removeClass("hidden");
        
    },*/
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
    	//in this case it's a sparql data getter, but what about the others
    	/*
    	var len = pageContentSections.length;
    	var i;
    	for(i = 0; i < len; i++) {
    		var pageContentSection = $(pageContentSections[i]);
    		var pageContentSectionId = pageContentSection.attr("id");
    		var jsonObject = pageManagementUtils.processPageContentSection(pageContentSection);
    		var jsonObjectString = JSON.stringify(jsonObject);
    		//Create a new hidden input with a specific name and assign value per page content
        	pageManagementUtils.createPageContentInputForSubmission(jsonObjectString);
    	}*/
    	
    },
    createPageContentInputForSubmission: function(inputValue) {
    	$("<input type='hidden' name='pageContentUnit' value='" + inputValue + "'>").appendTo(pageManagementUtils.pageContentSubmissionInputs);
    },
    processPageContentSection:function(pageContentSection) {
    	
    	var variableValue = pageContentSection.find("input[name='variable']").val();
    	var queryValue = pageContentSection.find("textarea[name='textArea']").val();
    	var returnObject = {saveToVar:variableValue, query:queryValue, dataGetterClass:pageManagementUtils.dataGetterLabelToURI["sparqlDataGetter"], queryModel:"vitro:contextDisplayModel"};
    	return returnObject;
    }
    
};

$(document).ready(function() {
   pageManagementUtils.onLoad();
});


