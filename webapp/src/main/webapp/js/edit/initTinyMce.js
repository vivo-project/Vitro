/* $This file is distributed under the terms of the license in LICENSE$ */

var initTinyMCE = {
    // Initial page setup
    onLoad: function(textareas) {
        this.mergeFromTemplate();
        this.initEditor(textareas);
    },

    // Add variables from menupage template
    mergeFromTemplate: function() {
        $.extend(this, customFormData);
    },
    // Create references to frequently used elements for convenience
    initEditor: function(textareas) {
    	textareas.tinymce(initTinyMCE.tinyMCEData);

    }
};

$(document).ready(function() {
    initTinyMCE.onLoad($(".useTinyMce"));
});

