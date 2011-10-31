/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
// Sets up event listeners so that the submit button gets enabled only if the user has changed
// an existing value.
//
// Used with both the userAccounts--myAccounts.ftl and userAccounts--edit.ftl.
    
$(document).ready(function(){

    var theForm = $('form').last();
    var theSubmitButton = theForm.find(':submit');

    theSubmitButton.addClass("disabledSubmit");

    $('input').each(function() {
        if ( $(this).attr('type') != 'submit' && $(this).attr('name') != 'querytext') {
            $(this).change(function() {
                theSubmitButton.removeAttr('disabled');
                theSubmitButton.removeClass("disabledSubmit");
            });
            $(this).bind("propertychange", function() {
                theSubmitButton.removeAttr('disabled');
                theSubmitButton.removeClass("disabledSubmit");
            });
            $(this).bind("input", function() {
                theSubmitButton.removeAttr('disabled');
                theSubmitButton.removeClass("disabledSubmit");
            });
        }
    });
    $('select').each(function() {
        $(this).change(function() {
            theSubmitButton.removeAttr('disabled');
            theSubmitButton.removeClass("disabledSubmit");
        });
    });
    
    $('.remove-proxy').click(function(){
        theSubmitButton.removeAttr('disabled');
        theSubmitButton.removeClass("disabledSubmit");
    })
});

