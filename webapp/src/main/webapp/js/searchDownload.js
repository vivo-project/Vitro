/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
    // This function creates and styles the "qTip" tooltip that displays the resource uri and the rdf link when the user clicks the uri/rdf icon.

    $('head').append('<style id="downloadCSS">.qtip { font-size: 14px; max-width: none !important; } .downloadTip { background-color: #f1f2ee; } </style>');

	$('img#downloadIcon').qtip(
        {
            prerender: true, // We need this for the .click() event listener on 'a.close'
            content: {
                text:  '<div style="float:right; width:150px;border-left: 1px solid #A6B1B0; padding: 3px 0 0 20px">'
	            	+'<p><label for="amount" style="font-size:14px;">Maximum Records:</label>'
	            	+'<input disabled type="text" id="amount" style="margin-left:35px; border: 0; color: #f6931f; font-weight: bold; width:45px" /></p>'
	            	+'<div id="slider-vertical" style="margin-left:60px; margin-top: -20px; height: 100px; background-color:white"></div>'
	            	+'<br /><a class="close" href="#">close</a></div>'
                	+'<div style="float:left; font-size:14px; width:280px; padding: 3px 0 0 20px"><p><label>Download the results from this search</label></p> '
                	+'<p class ="download-url"><a id=xmlDownload href="' + urlsBase + '/search?' + queryText +'&amp;xml=1&amp;hitsPerPage=500">download results in XML format</a></p>'
                	+'<p class ="download-url"><a id=csvDownload href="' + urlsBase + '/search?' + queryText +'&amp;csv=1&amp;hitsPerPage=500">download results in CSV format</a></p>'
                	+'</div>'
            },
            position: {
                my: 'top left',
                at: 'bottom left'
            },
            show: {
                event: 'click'
            },
            hide: {
                event: 'click'
            },
            style: {
                classes: 'downloadTip',
                width: 500
            }
        });

    $( "#slider-vertical" ).slider({
	      orientation: "vertical",
	      range: "min",
	      min: 10,
	      max: 1000,
	      value: 500,
	      slide: function( event, ui ) {
	        $( "#amount" ).val( ui.value );
	        $('#csvDownload').attr("href", urlsBase + '/search?' + queryText +'&csv=1&hitsPerPage=' + ui.value);
	        $('#xmlDownload').attr("href", urlsBase + '/search?' + queryText +'&xml=1&hitsPerPage=' + ui.value);
	      }
	    });
	    $( "#amount" ).val( $( "#slider-vertical" ).slider( "value" ) );
	

    // Prevent close link for URI qTip from requesting bogus '#' href
    $('a.close').click(function() {
        $('#downloadIcon').qtip("hide");
        return false;
    });
    

});