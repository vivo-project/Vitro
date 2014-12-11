/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
    var fauxPropertiesListingUtils = {
    onLoad: function() {
		this.initObjects();
        this.bindEventListeners();
    },

    initObjects: function() { 
		this.select = $('select#displayOption');
        this.theForm = $('form#fauxListing');
    },

    bindEventListeners: function() {
        this.select.change(function() {
        	fauxPropertiesListingUtils.theForm.submit();
        });
	}
}
