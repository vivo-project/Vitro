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
	        var selectedTypeText = $("#typeSelect :select").text();
	        //Not sure why selected group here? This won't always be true for more content
	        //var selectedGroup = $('select#selectClassGroup').val();
	        
	        //Aren't these already hidden? 
	        //Hide both sections
	        $("section#classGroup").hide();
	        $("section#nonClassGroup").hide();
	        
	        //Reset class group
	        pageManagementUtils.resetClassGroupSection();
	        pageManagementUtils.contentTyeSelectOptions.eq(0).attr('selected', 'selected');
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
	    });
	  
	    $("select#typeSelect").change( function() {
	        $('input#variable').val("");
	        $('textarea#textArea').val("");
	        if ( $("#typeSelect").val() == "Browse Class Group" ) {
	            $("section#classGroup").show();
	            $("section#nonClassGroup").hide();
	            $("input#moreContent").hide();
	            $("section#headerBar").text("Browse Class Group - ");
	            $("section#headerBar").show();
	        }
	        if ( $("#typeSelect").val() == "Fixed HTML" || $("#typeSelect").val() == "SPARQL Query Results" ) {
	            $("section#classGroup").hide();
	            if ( $("#typeSelect").val() == "Fixed HTML" ) {
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
    
	},
	//Clone content area
	cloneContentArea: function(contentType, contentTypeLabel) {
        var ctr = pageManagementUtils.counter;
        var counter = pageManagementUtils.counter;
        var varOrClas;
        
        
        if ( contentType == "fixedHTML" || contentType == "sparqlResults" ) {
           
            var $newContentObj = $('section#nonClassGroup').clone();
            $newContentObj.addClass("pageContent");
            varOrClass = $newContentObj.find('input').val();
            $newContentObj.show();
            //Save content type
            $newContentObj.attr("contentType", contentType);
            $newContentObj.attr("id", contentType + counter);
            $newContentObj.find('input#variable').attr("id","variable" + counter);
            $newContentObj.find('textarea#textArea').attr("id","textArea" + counter);
            $newContentObj.find('input#variable').attr("name","variable" + counter);
            $newContentObj.find('textarea#textArea').attr("name","textArea" + counter);
            $newContentObj.find('label#variableLabel').attr("id","variableLabel" + counter);
            $newContentObj.find('label#taLabel').attr("id","taLabel" + counter);
            // There's a jquery bug when cloning textareas: the value doesn't
			// get cloned. So
            // copy the value "manually."
            var taValue = $('textarea#textArea').val();
            $newContentObj.find('textarea').val(taValue);
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
    }
    
};

$(document).ready(function() {
   pageManagementUtils.onLoad();
});