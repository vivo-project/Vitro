/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

    $.extend(this, individualLocalName);
    adjustFontSize();

    // controls the property group tabs
    let showAllBtn = $('#show-all-tabs')[0];

    showAllBtn.addEventListener('show.bs.tab', function (event) {
        event.preventDefault()
        $('.propertyTabsList.nav.nav-tabs > li').each(function() {
            $(this).attr("aria-selected", "false");
            $(this).removeClass("active");
        });
        
        $('#show-all-tabs').addClass("active").attr("aria-selected", "true");
        $(".tab-content>section.tab-pane").addClass('active show')    
    })

    showAllBtn.addEventListener('hide.bs.tab', function (event) {
        $(".tab-content>section.tab-pane").removeClass('show active')
    })

    // if there are so many tabs that they wrap to a second line, adjust the font size to
    //prevent wrapping
    function adjustFontSize() {
        var width = 0;
        $('ul.propertyTabsList li').each(function() {
            width += $(this).outerWidth();
        });
        if ( width < 922 ) {
            var diff = 927-width;
            $('ul.propertyTabsList li:last-child').css('width', diff + 'px');
        }
        else {
            var diff = width-926;
            if ( diff < 26 ) {
                $('ul.propertyTabsList li').css('font-size', "0.96em");
            }
            else if ( diff > 26 && diff < 50 ) {
                $('ul.propertyTabsList li').css('font-size', "0.92em");
            }
            else if ( diff > 50 && diff < 80 ) {
                $('ul.propertyTabsList li').css('font-size', "0.9em");
            }
            else if ( diff > 80 && diff < 130 ) {
                $('ul.propertyTabsList li').css('font-size', "0.84em");
            }
            else if ( diff > 130 && diff < 175 ) {
                $('ul.propertyTabsList li').css('font-size', "0.8em");
            }
            else if ( diff > 175 && diff < 260 ) {
                $('ul.propertyTabsList li').css('font-size', "0.73em");
            }
            else {
                $('ul.propertyTabsList li').css('font-size', "0.7em");
            }

            // get the new width
            var newWidth = 0
            $('ul.propertyTabsList li').each(function() {
                newWidth += $(this).outerWidth();
            });
            var newDiff = 926-newWidth;
            $('ul.propertyTabsList li:last-child').css('width', newDiff + 'px');
        }
    }
});


