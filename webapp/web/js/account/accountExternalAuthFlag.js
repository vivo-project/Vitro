/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
// Change form actions in account main page
    
$(document).ready(function(){

    // The externalAuthOnly checkbox drives the display of the password and re-set
    // password fields. When checked, the password fields are hidden
    $('input:checkbox[name=externalAuthOnly]').click(function(){
         if ( this.checked ) {
         // If checked, hide those puppies
            $('#passwordContainer').addClass('hidden');
            $('#pwdResetContainer').addClass('hidden');
        // And clear any values entered in the password fields
            $('input[name=confirmPassword]').val("");
            $('input[name=initialPassword]').val("");
            $('input[name=newPassword]').val("");
         } 
         else {
         // if not checked, display them
            $('#passwordContainer').removeClass('hidden');
            $('#pwdResetContainer').removeClass('hidden');
         }
    });

});