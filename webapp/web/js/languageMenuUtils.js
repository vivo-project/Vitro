/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(function() {
		
	$.extend(this, i18nStringsLangMenu);
	var theText = this.selectLanguage;
	var imgHTML = "";
    $("ul.language-dropdown li#language-menu").hover(function(){ 
		$("a#lang-link").html(theText);
        $(this).addClass("hover");
        $('ul:first',this).css('visibility', 'visible');
        $("ul.language-dropdown ul.sub_menu li").css('width', ($("ul.language-dropdown").width() - 4) + 'px');
    
    }, function(){
    	$("a#lang-link").html(imgHTML);
        $(this).removeClass("hover");
        $('ul:first',this).css('visibility', 'hidden');    
    });
    
    $("ul.language-dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");
	
	$("ul.language-dropdown ul.sub_menu li").each(function() {
		if ( $(this).attr("status") == "selected" ) {
			$("a#lang-link").html($(this).children("a").html());
			imgHTML = $(this).children("a").html();
		}
	});

});