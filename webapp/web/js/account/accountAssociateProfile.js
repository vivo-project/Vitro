/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var associateProfileFields = {

    /* *** Initial page setup *** */
   
    onLoad: function() {
        console.log('Here we are');
        if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }        

        this.mixIn();
        this.initObjects();                 
        this.initPage();       
    },

    disableFormInUnsupportedBrowsers: function() {       
        var disableWrapper = $('#ie67DisableWrapper');
        
        // Check for unsupported browsers only if the element exists on the page
        if (disableWrapper.length) {
            if (vitro.browserUtils.isIELessThan8()) {
                disableWrapper.show();
                $('.noIE67').hide();
                return true;
            }
        }            
        return false;      
    },

    mixIn: function() {
        $.extend(this, associateProfileFieldsData);
    },
    
    // On page load, create references for easy access to form elements.
    initObjects: function() {
        this.form = $('#userAccountForm');
        
        // The external auth ID field and messages
        this.externalAuthIdField = $('#externalAuthId');
        this.externalAuthIdInUseMessage = $('#externalAuthIdInUse');

        // We have an associated profile
        this.associatedArea = $('#associated');
        this.associatedProfileNameSpan = $('#associatedProfileName');
        this.verifyAssociatedProfileLink = $('#verifyProfileLink');
        this.changeAssociatedProfileLink = $('#changeProfileLink');
        this.associatedProfileUriField = $('#associatedProfileUri')
        
        // We want to associate a profile
        this.associationOptionsArea = $('#associationOptions');
        this.associateProfileNameField = $('#associateProfileName');
    },
    
    // Initial page setup. Called only at page load.
    initPage: function() {
        this.checkForAssociatedProfile();
        this.bindEventListeners();
        this.initAutocomplete();
    },
    
    bindEventListeners: function() {
        console.log('bindEventListeners');

        this.externalAuthIdField.change(function() {
            associateProfileFields.checkForAssociatedProfile();
        }); 
        this.externalAuthIdField.keyup(function() {
            associateProfileFields.checkForAssociatedProfile();
        }); 
        
        this.verifyAssociatedProfileLink.click(function() {
            associateProfileFields.openVerifyWindow();
            return false;
        });   
        
        this.changeAssociatedProfileLink.click(function() {
            associateProfileFields.associatedProfileUriField.val('');
            associateProfileFields.associateProfileNameField.val('');
            associateProfileFields.showExternalAuthIdNotRecognized();
            return false;
        });   
        
    },
    
    initAutocomplete: function() {
        this.associateProfileNameField.autocomplete({
            minLength: 3,
            source: function(request, response) {
                $.ajax({
                    url: associateProfileFields.ajaxUrl,
                    dataType: 'json',
                    data: {
                        function: "autoCompleteProfile",
                        term: request.term,
                        externalAuthId: associateProfileFields.externalAuthIdField.val()
                    }, 
                    complete: function(xhr, status) {
                        console.log('response text' + xhr.responseText);
                        var results = jQuery.parseJSON(xhr.responseText);
                        response(results);
                    }
                });
            },
            select: function(event, ui) {
                associateProfileFields.showSelectedProfile(ui.item); 
            }
        });

    },

    checkForAssociatedProfile: function() {
        $.ajax({
            url: associateProfileFields.ajaxUrl,
            dataType: "json",
            data: {
                function: "checkExternalAuth",
                userAccountUri: associateProfileFields.userUri,
                externalAuthId: associateProfileFields.externalAuthIdField.val()
            },
            complete: function(xhr, status) {
                var results = $.parseJSON(xhr.responseText);
                if (results.idInUse) {
                    associateProfileFields.showExternalAuthIdInUse()
                } else if (results.matchesProfile) {
                    associateProfileFields.showExternalAuthIdMatchesProfile(results.profileUri, results.profileUrl, results.profileLabel)
                } else {
                    associateProfileFields.showExternalAuthIdNotRecognized()
                }
            }
        });
    },

    openVerifyWindow: function() {
        window.open(this.verifyUrl, 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
    },
        
    showExternalAuthIdInUse: function() {
        this.externalAuthIdInUseMessage.show();
        this.associatedArea.hide();
        this.associationOptionsArea.hide();
    },
 
    showExternalAuthIdMatchesProfile: function(profileUri, profileUrl, profileLabel) {
        console.log('showExternalAuthIdMatchesProfile: profileUri=' + profileUri + ', profileUrl=' + profileUrl + ', profileLabel='+ profileLabel);

        this.externalAuthIdInUseMessage.hide();
        this.associatedArea.show();
        this.associationOptionsArea.hide();
        
        this.associatedProfileNameSpan.html(profileLabel);
        this.associatedProfileUriField.val(profileUri);
        this.verifyUrl = profileUrl;
    },
       
    showExternalAuthIdNotRecognized: function() {
        this.externalAuthIdInUseMessage.hide();
        this.associatedArea.hide();
        
        if (this.associationEnabled && this.externalAuthIdField.val().length > 0) {
            this.associationOptionsArea.show();
        } else {
            this.associationOptionsArea.hide();
        }
    },

    showSelectedProfile: function(item) {
    	this.showExternalAuthIdMatchesProfile(item.uri, item.url, item.label);
    },
    
}
 
$(document).ready(function() {   
    associateProfileFields.onLoad();
}); 
        