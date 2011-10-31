/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
// Sets up event listeners so that the submit button gets enabled only if the user has changed
// an existing value.
//
// Used with both the userAccounts--myAccounts.ftl and userAccounts--edit.ftl.
    
$(document).ready(function(){

    var theForm = $('form').last();
    var theSubmitButton = theForm.find(':submit');

    theSubmitButton.addClass("disabledSubmit");
    
    function disableSubmit() {
        theSubmitButton.removeAttr('disabled');
        theSubmitButton.removeClass("disabledSubmit");
    }

    $('input').each(function() {
        if ( $(this).attr('type') != 'submit' && $(this).attr('name') != 'querytext') {
            $(this).change(function() {
                disableSubmit()
            });
            $(this).bind("propertychange", function() {
                disableSubmit();
            });
            $(this).bind("input", function() {
                disableSubmit()
            });
        }
    });
    $('select').each(function() {
        $(this).change(function() {
            disableSubmit()
        });
    });
    
    $('.remove-proxy').click(function(){
        disableSubmit()
    })
});

