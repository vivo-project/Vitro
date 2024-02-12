/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

    $.extend(this, i18nStringsUriRdf);

    let tooltips = [
        {
            querySelector: "span#iconControlsLeftSide > img#uriIcon",
            data: {
                title: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>',
                html: true,
                sanitize: false,
                trigger: "click",
                customClass: "vivoTooltip",
                fallbackPlacements: ['bottom', 'left', 'top', 'right']

            }
        },
        {
            querySelector: "span#iconControlsVitro > img#uriIcon",
            data: {
                title: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>',
                html: true,
                sanitize: false,
                trigger: "click",
                customClass: "vivoTooltip",
                fallbackPlacements: ['bottom', 'left', 'top', 'right']

            }
        },
        {
            querySelector: "span#iconControlsRightSide > img#uriIcon",
            data: {
                title: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a>',
                html: true,
                sanitize: false,
                trigger: "click",
                customClass: "vivoTooltip",
                fallbackPlacements: ['bottom', 'right', 'top', 'left']
            }
        },
    ]

    tooltips.forEach(tooltip => {
        setTooltip(tooltip.querySelector, tooltip.data)
    })
    
});
