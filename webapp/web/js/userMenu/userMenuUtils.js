/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(function(){

    $("ul.dropdown li").hover(function(){  
        $(this).addClass("hover");
        $('ul:first',this).css('visibility', 'visible');
        if ( $('ul.dropdown').width() > 88 ) {
            $('ul:first li',this).css('width', ($("ul.dropdown").width() - 22) + 'px');
        }
        $("ul.dropdown ul.sub_menu li:last-child").css('width', ($("ul.dropdown").width() - 14) + 'px');
    
    }, function(){
    
        $(this).removeClass("hover");
        $('ul:first',this).css('visibility', 'hidden');
    
    });
    
    $("ul.dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");

});