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
		$.extend(this, i18nStrings);

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
				return defaultDataPropertyUtils.createAndValidateLiteralValue(); 
			}
			return true;
        });               
    },

	createAndValidateLiteralValue: function() {
		var theType = datatype.substring(datatype.lastIndexOf("#") + 1);
		var temp = "";
		if ( $('#literal').attr("type") == "hidden" ) {
			if ( $('#dateTimeField-year').length ) { 
				if ( $('#dateTimeField-year').val().length < 4 ) {
					alert(defaultDataPropertyUtils.four_digit_year);
					return false;
				}
				var reg = /^\d+$/;
				if ( !reg.test($('#dateTimeField-year').val()) ) {
					alert(defaultDataPropertyUtils.year_numeric);
					return false;
				}
			}
			switch (theType) {
				case 'date':
					temp = $('#dateTimeField-year').val() + "-" 
					         + $('#dateTimeField-month').val() + "-"
					         + $('#dateTimeField-day').val(); 
					if ( temp.indexOf("-") == 0 || temp.lastIndexOf("-") == (temp.length - 1) || temp.indexOf("--") > 0 ) {
						alert(defaultDataPropertyUtils.year_month_day);
						return false;
					}
					$('#literal').val(temp);
					break;
				case 'dateTime':
					temp = $('#dateTimeField-year').val() + "-" 
				         	+ $('#dateTimeField-month').val() + "-"
				         	+ $('#dateTimeField-day').val() + "T" 
							+ ($('#dateTimeField-hour').val().length == 0 ? "00" : $('#dateTimeField-hour').val()) + ":"
							+ ($('#dateTimeField-minute').val().length == 0 ? "00" : $('#dateTimeField-minute').val()) + ":" 
							+ ($('#dateTimeField-second').val().length == 0 ? "00" : $('#dateTimeField-second').val());
					if ( temp.indexOf("-") == 0 || temp.indexOf("-T") > 0 || temp.indexOf("--") > 0 ) {
						alert(defaultDataPropertyUtils.minimum_ymd);
						return false;
					}
					$('#literal').val(temp);
					break;
				case 'time':
					temp = $('#dateTimeField-hour').val() + ":"
							+ ($('#dateTimeField-minute').val().length == 0 ? "00" : $('#dateTimeField-minute').val()) + ":" 
							+ ($('#dateTimeField-second').val().length == 0 ? "00" : $('#dateTimeField-second').val());
					if ( temp.indexOf(":") == 0 ) {
						alert(defaultDataPropertyUtils.minimum_hour);
						return false;
					}
					$('#literal').val(temp);
					break;
				case 'gYear':
					$('#literal').val($('#dateTimeField-year').val());
					break;
				case 'gYearMonth':
					temp = $('#dateTimeField-year').val() + "-" + $('#dateTimeField-month').val();
					if ( temp.indexOf("-") == 0 || temp.lastIndexOf("-") == (temp.length - 1) ) {
						alert(defaultDataPropertyUtils.year_month);
						return false;
					}
					$('#literal').val(temp);
					break;
				case 'gMonth':
					if ( $('#dateTimeField-month').val().length == 2 ) {
						temp = "--" + $('#dateTimeField-month').val()
						$('input#literal').val(temp);
					}
					break;			
			}
		}
		else if (  $('#literal').attr("type") == "text" ) {
			switch (theType) {
				case 'float':
					if ( $('#literal').val().indexOf(",") > -1 ) {
						alert(defaultDataPropertyUtils.decimal_only);
						return false;
					}
					$('#literal').val($('#literal').val().replace(",",""));
					break;
				case 'integer':
					if ( $('#literal').val().indexOf(".") > -1 || $('#literal').val().indexOf(",") > 0 ) {
						alert(defaultDataPropertyUtils.whole_number);
						return false;
					}
					break;
				case 'int':
					if ( $('#literal').val().indexOf(".") > -1 || $('#literal').val().indexOf(",") > 0 ) {
						alert(defaultDataPropertyUtils.whole_number);
						return false;
					}
					break;
			}
		}
		return true;
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
 
