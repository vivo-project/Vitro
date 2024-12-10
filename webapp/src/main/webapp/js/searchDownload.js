/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){
    // This function creates and styles bootstrap-popper tooltip that displays the resource uri and the rdf link when the user clicks the uri/rdf icon.

    function configSlider() {
        $( "#slider-vertical" ).slider({
        orientation: "vertical",
        range: "min",
        min: 10,
        max: 1000,
        value: 500,
        slide: function( event, ui ) {
            $( "#amount" ).val( ui.value );
            $('#csvDownload').attr("href", urlsBase + '/search?' + queryText +'&csv=1&documentsNumber=' + ui.value);
            $('#xmlDownload').attr("href", urlsBase + '/search?' + queryText +'&xml=1&documentsNumber=' + ui.value);
            }
        });
        $( "#amount" ).val( $( "#slider-vertical" ).slider( "value" ) );
    }

    setTooltip("img#downloadIcon", {
        title: '<div class="clearfix"><div style="float:right; width:150px;border-left: 1px solid #A6B1B0; padding: 3px 0 0 20px">'
                    	+'<p><label for="amount" style="font-size:14px;">Maximum Records:</label>'
                    	+'<input disabled type="text" id="amount" style="margin-left:35px; border: 0; color: #f6931f; font-weight: bold; width:45px" /></p>'
                    	+'<div id="slider-vertical" style="margin-left:52px; margin-top: -20px; height: 100px; background-color:white"></div>'
                    	+'<br /><a class="close" href="#">close</a></div>'
                    	+'<div style="float:left; font-size:14px; width:280px; padding: 3px 0 0 20px"><p><label>Download the results from this search</label></p> '
                    	+'<p class ="download-url"><a id=xmlDownload href="' + urlsBase + '/search?' + queryText +'&amp;xml=1&amp;documentsNumber=500">download results in XML format</a></p>'
                    	+'<p class ="download-url"><a id=csvDownload href="' + urlsBase + '/search?' + queryText +'&amp;csv=1&amp;documentsNumber=500">download results in CSV format</a></p>'
                    	+'</div></div>',
        trigger: "click",
        customClass: "vitroTooltip downloadTip",
        placements: ['right', 'bottom', 'top', 'left'],
        afterCreate: configSlider,

    })

});
