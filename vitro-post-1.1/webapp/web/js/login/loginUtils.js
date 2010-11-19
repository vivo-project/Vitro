/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

    // login form is hidden by default; use JavaScript to reveal
    $("#loginFormAndLinks").show();
  
    // focus on email or newpassword field
    $('.focus').focus();

});

// fade out error alerts
$('#errorAlert').css('display', 'none');
$('#errorAlert').fadeIn(2000);
  

