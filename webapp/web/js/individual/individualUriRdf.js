/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
    
    $.extend(this, i18nStringsUriRdf);
    
    // This function creates and styles the "qTip" tooltip that displays the resource uri and the rdf link when the user clicks the uri/rdf icon.
    $('span#iconControlsLeftSide').children('img#uriIcon').each(function()
    {
        $(this).qtip(
        {
            content: {
                prerender: true, // We need this for the .click() event listener on 'a.close'
                text: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>'
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
                width: 400,
                backgroundColor: '#f1f2ee'
            }
        });
    });

    $('span#iconControlsVitro').children('img#uriIcon').each(function()
    {
        $(this).qtip(
        {
            content: {
                prerender: true, // We need this for the .click() event listener on 'a.close'
                text: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>'
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
                width: 400,
                backgroundColor: '#f1f2ee'
            }
        });
    });

    $('span#iconControlsRightSide').children('img#uriIcon').each(function()
    {
        $(this).qtip(
        {
            content: {
                prerender: true, // We need this for the .click() event listener on 'a.close'
                text: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>'
            },
            position: {
                corner: {
                    target: 'bottomRight',
                    tooltip: 'topRight'
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
                width: 400,
                backgroundColor: '#f1f2ee'
            }
        });
    });

    // Prevent close link for URI qTip from requesting bogus '#' href
    $('a.close').click(function() {
        $('#uriIcon').qtip("hide");
        return false;
    });
});