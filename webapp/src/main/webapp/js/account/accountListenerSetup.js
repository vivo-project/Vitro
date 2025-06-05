/* $This file is distributed under the terms of the license in LICENSE$ */

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
            $(this).on("change", function() {
                disableSubmit()
            });
            $(this).on("propertychange", function() {
                disableSubmit();
            });
            $(this).on('input', function() {
                disableSubmit()
            });
        }
    });
    $('select').each(function() {
        $(this).on("change", function() {
            disableSubmit()
        });
    });

    $('.remove-proxy').on("click", function(){
        disableSubmit()
    })
});

