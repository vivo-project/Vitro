/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
$.extend(this, i18nStrings);

var pageDeletion = {
	// on initial page setup
	onLoad:function(){
		if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }   
		
	    this.initObjects();
	    this.bindEventListeners();
	}, 
	initObjects:function() {
		this.deleteLinks = $("a[cmd='deletePage']");
	},
	bindEventListeners:function() {
		this.deleteLinks.click(function(event) {
			var href=$(this).attr("href");
			var pageTitle = $(this).attr("pageTitle");
			var confirmResult = confirm( i18nStrings.confirmPageDeletion + " " + pageTitle + "?");
			if(confirmResult) {
				//Continue with the link
				return true;
			} else {
				event.preventDefault();
				return false;
			}
		});
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
    }
};

$(document).ready(function() {
   pageDeletion.onLoad();
});


