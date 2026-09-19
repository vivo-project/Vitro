/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

    function configSlider() {

        $("#slider-vertical").slider({
            orientation: "vertical",
            range: "min",
            min: 10,
            max: 1000,
            value: 500,
            slide: function(event, ui) {
                $("#amount").val(ui.value);
                $('#csvDownload').attr("href", urlsBase + '/search?' + queryText + '&csv=1&documentsNumber=' + ui.value);
                $('#xmlDownload').attr("href", urlsBase + '/search?' + queryText + '&xml=1&documentsNumber=' + ui.value);
                // Update aria-valuenow on slide
                $("#slider-vertical .ui-slider-handle").attr("aria-valuenow", ui.value);
                $("#slider-vertical .ui-slider-range").attr("role", "presentation");
                $("#slider-vertical .ui-slider-range").attr("aria-hidden", "true");

            },
            create: function(event, ui) {
                // Set ARIA attributes on the slider handle after creation
                var $handle = $("#slider-vertical .ui-slider-handle");
                $handle.attr({
                    "role": "slider",
                    "aria-valuemin": 10,
                    "aria-valuemax": 1000,
                    "aria-valuenow": 500,
                    "aria-orientation": "vertical",
                    "aria-label": i18nStrings.downloadResultsMaxResultsSliderString
                });
            }
        });
        $("#amount").val($("#slider-vertical").slider("value"));
    }

    setTooltip("#downloadIcon", {
        title: '<div class="clearfix">'
            + '<div style="float:left; font-size:14px; width:280px; padding: 3px 0 0 20px">'
            + '<p><label>'+ i18nStrings.downloadResultsModalTitleString +'</label></p>'
            + '<ul><li class="download-url"><a id=xmlDownload href="' + urlsBase + '/search?' + queryText +'&amp;xml=1&amp;documentsNumber=500">'+ i18nStrings.downloadResultsXmlFormatString +'</a></li>'
            + '<li class="download-url"><a id=csvDownload href="' + urlsBase + '/search?' + queryText +'&amp;csv=1&amp;documentsNumber=500">'+ i18nStrings.downloadResultsCsvFormatString +'</a></li></ul>'
            + '</div><div style="float:right; width:150px;border-left: 1px solid #A6B1B0; padding: 3px 0 0 20px">'
            + '<p><label for="amount" style="font-size:14px;">'+ i18nStrings.downloadResultsMaxResultsTitleString +'</label>'
            + '<input disabled type="text" id="amount" style="margin-left:35px; border: 0; color: #91540F; font-weight: bold; width:45px" />'
            + '</p><div role="presentation" id="slider-vertical" aria-label="Adjust maximum number of records" style="margin-left:52px; margin-top: -20px; height: 100px; background-color:white; border-color: #6E6E6E"></div>'
            + '<br aria-hidden="true" /><a class="close" href="#">' + i18nStrings.closeString + '</a></div></div>',
        trigger: "click",
        customClass: "vitroTooltip downloadTip",
        placements: ['right', 'bottom', 'top', 'left'],
        afterCreate: configSlider,

    })

});
