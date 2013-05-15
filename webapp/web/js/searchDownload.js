/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
    // This function creates and styles the "qTip" tooltip that displays the resource uri and the rdf link when the user clicks the uri/rdf icon.
    $('span#downloadResults').children('img#downloadIcon').each(function()
    {
        $(this).qtip(
        {
            content: {
                prerender: true, // We need this for the .click() event listener on 'a.close'
                text: '<h5>Download the results from this search</h5> <h5 class ="download-url"><a href="' + urlsBase + '/search?querytext=' + queryText +'&amp;xml=1&amp;hitsPerPage=500">download results in XML format</a></h5><h5 class ="download-url"><a href="' + urlsBase + '/search?querytext=' + queryText +'&amp;csv=1&amp;hitsPerPage=500">download results in CSV format</a></h5><br /><a class="close" href="#">close</a>'
            },
            position: {
                corner: {
                    target: 'bottomLeft',
                    tooltip: 'topLeft'
                }
            },
            show: {
                when: {event: 'click'}
            },
            hide: {
                fixed: true, // Make it fixed so it can be hovered over and interacted with
                when: {
                    target: $('a.close'),
                    event: 'click'
                }
            },
            style: {
                padding: '1em',
                width: 350,
                backgroundColor: '#f1f2ee'
            }
        });
    });

    

    // Prevent close link for URI qTip from requesting bogus '#' href
    $('a.close').click(function() {
        $('#downloadIcon').qtip("hide");
        return false;
    });
});