/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var defaultDataPropertyUtils = {
        
    onLoad: function() {

        this.initObjectReferences();                 
        this.bindEventListeners();
        
    },

    initObjectReferences: function() {
    
        this.form = $('form.editForm');
        this.textArea = $('textarea.useTinyMce');

    },

    bindEventListeners: function() {

        this.form.submit(function() {
            var theText = tinyMCE.get('literal').getContent();

            if ( theText.indexOf("<!--") > -1 && theText.indexOf("-->") > -1 ) {
                var start = theText.indexOf("<p><!--");
                var end = (theText.indexOf("--></p>") + 10);
                var removeText = theText.slice(start,end);
                var newText = theText.replace(removeText,"");
                tinyMCE.get('literal').setContent(newText);                
            }
            else if ( theText.indexOf("&lt;!--") > -1 && theText.indexOf("--&gt;") > -1 ) {
                var start = theText.indexOf("<p>&lt;!--");
                var end = (theText.indexOf("--&gt;</p>") + 10);
                var removeText = theText.slice(start,end);
                var newText = theText.replace(removeText,"");
                tinyMCE.get('literal').setContent(newText);                
            }
        });               
    },
} 
