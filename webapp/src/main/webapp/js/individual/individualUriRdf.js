/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
    
    $.extend(this, i18nStringsUriRdf);

    $('head').append('<style id="uriIconCSS">.qtip { font-size: 14px; max-width: none !important; } .uriIconTip { background-color: #f1f2ee; } </style>');

    // This function creates and styles the "qTip" tooltip that displays the resource uri and the rdf link when the user clicks the uri/rdf icon.
    $('span#iconControlsLeftSide').children('img#uriIcon').each(function()
    {
        $(this).qtip(
        {
            prerender: true, // We need this for the .click() event listener on 'a.close'
            content: {
                text: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>'
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
                classes: 'uriIconTip',
                width: 400
            }
        });
    });

    $('span#iconControlsVitro').children('img#uriIcon').each(function()
    {
        $(this).qtip(
        {
            prerender: true, // We need this for the .click() event listener on 'a.close'
            content: {
                text: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>'
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
                classes: 'uriIconTip',
                width: 400
            }
        });
    });

    $('span#iconControlsRightSide').children('img#uriIcon').each(function()
    {
        $(this).qtip(
        {
            prerender: true, // We need this for the .click() event listener on 'a.close'
            content: {
                text: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>'
            },
            position: {
                my: 'top right',
                at: 'bottom right'
            },
            show: {
                event: 'click'
            },
            hide: {
                event: 'click'
            },
            style: {
                classes: 'uriIconTip',
                width: 400
            }
        });
    });

    // Prevent close link for URI qTip from requesting bogus '#' href
    $('a.close').click(function() {
        $('#uriIcon').qtip("hide");
        return false;
    });
});