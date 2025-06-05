/* $This file is distributed under the terms of the license in LICENSE$ */

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
        this.select.on("change", function() {
        	fauxPropertiesListingUtils.theForm.trigger("submit");
        });
	}
}
