/* $This file is distributed under the terms of the license in LICENSE$ */

$(function(){

    $("ul.dropdown li").on("mouseenter", function(){
        $(this).addClass("hover");
        $('ul:first',this)
			.css('visibility', 'visible')
			.attr('aria-hidden', 'false');
		$(this).attr('aria-expanded', 'true');
        if ( $('ul.dropdown').width() > 88 ) {
            $('ul:first li',this).css('width', ($("ul.dropdown").width() - 22) + 'px');
        }
        $("ul.dropdown ul.sub_menu li:last-child").css('width', ($("ul.dropdown").width() - 14) + 'px');

    }).on("mouseleave", function(){

        $(this).removeClass("hover");
        $('ul:first',this)
			.css('visibility', 'hidden')
			.attr('aria-hidden', 'true');
		$(this).attr('aria-expanded', 'false');

    });

    $("ul.dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");

});
