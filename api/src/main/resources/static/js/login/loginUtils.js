/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

    // login form is hidden by default; use JavaScript to reveal
    $("#login").removeClass('hidden');

    // focus on email or newpassword field
    $('.focus').focus();

    // fade in error alerts
    $('section#error-alert').css('display', 'none').fadeIn(1500);

    // toggle vivo account authentication form
    $('h3.internal-auth').click(function() {
        $('.vivoAccount').toggle();
    });

});
