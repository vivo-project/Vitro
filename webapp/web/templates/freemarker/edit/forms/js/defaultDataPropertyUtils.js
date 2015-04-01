/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var defaultDataPropertyUtils = {
        
    onLoad: function() {

        this.initObjectReferences();                 
        this.bindEventListeners();
		if ( $('#literal').val().length > 0 ) {
			this.parseLiteralValue();
		}
    },

    initObjectReferences: function() {
    
        this.form = $('form.editForm');
        this.textArea = $('textarea.useTinyMce');

		$.extend(this, datatype);

    },

    bindEventListeners: function() {

        this.form.submit(function() {
			if ( defaultDataPropertyUtils.textArea.length ) {
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
			}
			else { 
				defaultDataPropertyUtils.createLiteralValue(); 
			}
        });               
    },

	createLiteralValue: function() {
		var theType = datatype.substring(datatype.lastIndexOf("#") + 1);
		var temp = "";
		
		switch (theType) {
			case 'date':
				temp = $('#dateTimeField-year').val() + "-" 
				         + $('#dateTimeField-month').val() + "-"
				         + $('#dateTimeField-day').val(); 
				$('#literal').val(temp);
				break;
			case 'dateTime':
				temp = $('#dateTimeField-year').val() + "-" 
			         	+ $('#dateTimeField-month').val() + "-"
			         	+ $('#dateTimeField-day').val() + "T" 
						+ $('#dateTimeField-hour').val() + ":"
						+ $('#dateTimeField-minute').val() + ":" 
						+ $('#dateTimeField-second').val();
				$('#literal').val(temp);
				break;
			case 'time':
				temp = $('#dateTimeField-hour').val() + ":"
						+ $('#dateTimeField-minute').val() + ":" 
						+ $('#dateTimeField-second').val();
				$('#literal').val(temp);
				break;
			case 'gYear':
				$('#literal').val($('#dateTimeField-year').val());
				break;
			case 'gYearMonth':
				temp = $('#dateTimeField-year').val() + "-" + $('#dateTimeField-month').val();
				$('#literal').val(temp);
				break;
			case 'gMonth':
				temp = "--" + $('#dateTimeField-month').val()
				$('input#literal').val(temp);
				break;			
		}
	},
	
	parseLiteralValue: function() {
		var theType = datatype.substring(datatype.lastIndexOf("#") + 1);
		var temp = $('#literal').val();
		
		switch (theType) {
			case 'date':
				$('#dateTimeField-year').val(temp.substring(0, temp.indexOf("-")));
				$('#dateTimeField-month').val(temp.substring(temp.indexOf("-")+1,temp.lastIndexOf("-")));
				$('#dateTimeField-day').val(temp.substring(temp.lastIndexOf("-")+1)); 
				break;
			case 'dateTime':
				$('#dateTimeField-year').val(temp.substring(0, temp.indexOf("-")));
				$('#dateTimeField-month').val(temp.substring(temp.indexOf("-")+1,temp.lastIndexOf("-")));
				$('#dateTimeField-day').val(temp.substring(temp.lastIndexOf("-")+1,temp.indexOf("T"))); 
				$('#dateTimeField-hour').val(temp.substring(temp.indexOf("T")+1,temp.indexOf(":")));
				$('#dateTimeField-minute').val(temp.substring(temp.indexOf(":")+1,temp.lastIndexOf(":"))); 
				$('#dateTimeField-second').val(temp.substring(temp.lastIndexOf(":")+1));
				break;
			case 'time':
				$('#dateTimeField-hour').val(temp.substring(0,temp.indexOf(":")));
				$('#dateTimeField-minute').val(temp.substring(temp.indexOf(":")+1,temp.lastIndexOf(":"))); 
				$('#dateTimeField-second').val(temp.substring(temp.lastIndexOf(":")+1));
				break;
			case 'gYear':
				$('#dateTimeField-year').val(temp);
				break;
			case 'gYearMonth':
				$('#dateTimeField-year').val(temp.substring(0, temp.indexOf("-")));
				$('#dateTimeField-month').val(temp.substring(temp.indexOf("-")+1));
				break;
			case 'gMonth':
				$('#dateTimeField-month').val(temp.substring(temp.lastIndexOf("-")+1));
				break;			
		}
	}
	
}
$(document).ready(function(){
	defaultDataPropertyUtils.onLoad();
});
 
