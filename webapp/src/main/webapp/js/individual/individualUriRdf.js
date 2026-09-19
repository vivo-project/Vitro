/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

    $.extend(this, i18nStringsUriRdf);

    let tooltips = [
        {
            querySelector: "span#iconControlsLeftSide > #uriIcon",
            data: {
                title: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><div class="close-footer"><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a></div>',
                trigger: "click",
                customClass: "vitroTooltip",
                placements: ['top', 'right', 'bottom', 'left'],

            }
        },
        {
            querySelector: "span#iconControlsVitro > #uriIcon",
            data: {
                title: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><div class="close-footer"><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a></div>',
                trigger: "click",
                customClass: "vitroTooltip",
                placements: ['top', 'right', 'bottom', 'left'],

            }
        },
        {
            querySelector: "span#iconControlsRightSide > #uriIcon",
            data: {
                title: '<h5>' + i18nStringsUriRdf.shareProfileUri + '</h5> <input id="uriLink" type="text" value="' + $('#uriIcon').attr('title') + '" /><h5><a class ="rdf-url" href="' + individualRdfUrl + '">' + i18nStringsUriRdf.viewRDFProfile + '</a></h5><div class="close-footer"><a class="close" href="#">' + i18nStringsUriRdf.closeString + '</a></div>',
                trigger: "click",
                customClass: "vitroTooltip",
                placements: ['top', 'left', 'bottom', 'right'],
            }
        },
    ]

    tooltips.forEach(tooltip => {
        setTooltip(tooltip.querySelector, tooltip.data)
    })
    
});
