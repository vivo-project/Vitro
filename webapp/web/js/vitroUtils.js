/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

    // fade out welcome-message when user logs in
    $('section#welcome-message').css('display', 'block').delay(2000).fadeOut(1500);
    
    // fade in flash-message when user logs out
    $('section#flash-message').css('display', 'none').fadeIn(1500);
});