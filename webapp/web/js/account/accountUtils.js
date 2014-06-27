/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
// Change form actions in account main page
function changeAction(form, url) {
    form.action = url;
    return true;
} 
    
$(document).ready(function(){
    
    // If filtering by role, make sure the role is included as a parameter (1) when the
    // page count changes or (2) when the next or previous links are clicked.
    if ( $('#roleFilterUri').val().length > 0 ) {
        var roleURI = $('#roleFilterUri').val().substring($('#roleFilterUri').val().indexOf("=")+1);
        roleURI = roleURI.replace("%3A%2F%2F","://").replace("%23","#").replace(/%2F/g,"/")
        $('input#roleTypeContainer').val(roleURI);
        var prevHref = $('a#previousPage').attr('href');
        var nextHref = $('a#nextPage').attr('href');
        prevHref += "&roleFilterUri=" + roleURI.replace("#","%23");
        nextHref += "&roleFilterUri=" + roleURI.replace("#","%23");
        $('a#previousPage').attr('href',prevHref);
        $('a#nextPage').attr('href',nextHref);
    }

    //Accounts per page
    //Hide if javascript is enabled
    $('input[name="accounts-per-page"]').addClass('hidden');
    
    $('.accounts-per-page').change(function() {
        // ensure both accounts-per-page select elements are
        // set to the same value before submitting
        var selectedValue = $(this).val();
        $('.accounts-per-page').val(selectedValue);
        $('#account-display').submit();
    });
    
    //Delete accounts
    //Show is javascript is enabled
    $('input:checkbox[name=delete-all]').removeClass('hidden');
    
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
            var answer = confirm( ((countAccount > 1) ? confirm_delete_account_plural : confirm_delete_account_singular) +'?');
            return answer;
        }
    });
    
    //Select role and filter
    $('#roleFilterUri').bind('change', function () {
        var url = $(this).val(); // get selected value
        if (url) { // require a URL
            window.location = url; // redirect
        }
        return false;
    });
});