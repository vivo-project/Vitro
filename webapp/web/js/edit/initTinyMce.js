/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var initTinyMCE = {
    // Initial page setup
    onLoad: function() {
        this.mergeFromTemplate();
        this.initObjects();
        this.initEditor();
       
    },
    
    // Add variables from menupage template
    mergeFromTemplate: function() {
        $.extend(this, customFormData);
    },
    initObjects: function() {
    	this.wsywigFields = $(".useTinyMce");
    },
    // Create references to frequently used elements for convenience
    initEditor: function() {
    	initTinyMCE.wsywigFields.tinymce(initTinyMCE.tinyMCEData);
    	
    }
};

$(document).ready(function() {
    initTinyMCE.onLoad();
});

