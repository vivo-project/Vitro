/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
    
    //Accounts per page
    //Hide is javascrip is disable
    $('.accounts-per-page-form input[type="submit"]').hide();
    
    $('.accounts-per-page').change(function() {
        $('#account-display').submit();
    });
    
    //Delete accounts
    //Show is javascript is enable
    $('input:checkbox[name=delete-all]').show();
    
    $('input:checkbox[name=delete-all]').click(function(){
         if ( this.checked ) {
         // if checked, select all the checkboxes
         $('input:checkbox[name=deleteAccount]').attr('checked','checked');
            
         } else {
         // if not checked, deselect all the checkboxes
           $('input:checkbox[name=deleteAccount]').removeAttr('checked');
         }
    });
    
    $('input:checkbox[name=deleteAccount]').click(function(){
        $('input:checkbox[name=delete-all]').removeAttr('checked');
    });
      
    // Confirmation alert for account deletion in userAccounts-list.ftl template
    $('input[name="delete-account"]').click(function(){
        var countAccount = $('input:checkbox[name=deleteAccount]:checked').length;
        if (countAccount == 0){
            return false;
        }else{
            var answer = confirm( 'Are you sure you want to delete ' + ((countAccount > 1) ? 'these accounts' : 'this account') +'?');
            return answer;
        }
    });
});