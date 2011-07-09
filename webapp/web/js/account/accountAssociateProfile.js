/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var associateProfileFields = {
    onLoad: function() {
        if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }        

        this.mixIn();
        this.initObjectReferences();                 
        this.bindEventListeners();
        this.setInitialState();       
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
    
    initObjectReferences: function() {
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
        this.newProfileClassSelector = $('#degreeUri');
        
        // Container <div> elements to provide background shading -- tlw72
        this.associateProfileBackgroundOneArea = $('#associateProfileBackgroundOne');
    },

    bindEventListeners: function() {
        this.externalAuthIdField.change(function() {
            associateProfileFields.externalAuthIdFieldHasChanged();
        }); 
        this.externalAuthIdField.keyup(function() {
            associateProfileFields.externalAuthIdFieldHasChanged();
        }); 
        
        this.verifyAssociatedProfileLink.click(function() {
            associateProfileFields.openVerifyWindow();
            return false;
        });   
        
        this.changeAssociatedProfileLink.click(function() {
            associateProfileFields.showAssociatingOptionsArea();
            return false;
        });   
        
        this.newProfileClassSelector.change(function() {
            console.log('selector has changed.')
            associateProfileFields.newProfileClassHasChanged();
        });
        
        this.associateProfileNameField.autocomplete({
            minLength: 3,
            source: function(request, response) {
                $.ajax({
                    url: associateProfileFields.ajaxUrl,
                    dataType: 'json',
                    data: {
                        action: "autoCompleteProfile",
                        term: request.term,
                        externalAuthId: associateProfileFields.externalAuthIdField.val()
                    }, 
                    complete: function(xhr, status) {
                        var results = jQuery.parseJSON(xhr.responseText);
                        response(results);
                    }
                });
            },
            select: function(event, ui) {
                associateProfileFields.showAssociatedProfileArea(ui.item.label, ui.item.uri, ui.item.url); 
            }
        });

        
    },
    
    setInitialState: function() {
        if (this.externalAuthIdField.val().length == 0) {
            this.hideAllOptionals();
        } else if (this.associatedProfileInfo) {
            this.showAssociatedProfileArea(this.associatedProfileInfo.label, this.associatedProfileInfo.uri, this.associatedProfileInfo.url);
        } else {
            this.showAssociatingOptionsArea();
        }
    },
    
    externalAuthIdFieldHasChanged: function() {
        if (this.externalAuthIdField.val().length == 0) {
            this.hideAllOptionals();
            return;
        }

        $.ajax({
            url: associateProfileFields.ajaxUrl,
            dataType: "json",
            data: {
                action: "checkExternalAuth",
                userAccountUri: associateProfileFields.userUri,
                externalAuthId: associateProfileFields.externalAuthIdField.val()
            },
            complete: function(xhr, status) {
                var results = $.parseJSON(xhr.responseText);
                if (results.idInUse) {
                    associateProfileFields.showExternalAuthInUseMessage()
                } else if (results.matchesProfile) {
                    associateProfileFields.showAssociatedProfileArea(results.profileLabel, results.profileUri, results.profileUrl)
                } else {
                    associateProfileFields.showAssociatingOptionsArea();
                }
            }
        });
    },
    
    openVerifyWindow: function() {
        window.open(this.verifyUrl, 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
    },
    
    newProfileClassHasChanged: function() {
        if (this.newProfileClassSelector.val().length == 0) {
            this.associateProfileNameField.disabled = false;
        } else {
            this.associateProfileNameField.val('');
            this.associateProfileNameField.disabled = true;
        }
    },
    
    hideAllOptionals: function() {
        this.hideExternalAuthInUseMessage();
        this.hideAssociatedProfileArea();
        this.hideAssociatingOptionsArea();
    },
    
    hideExternalAuthInUseMessage: function() {
        this.externalAuthIdInUseMessage.hide();
    },
    
    hideAssociatedProfileArea: function() {
        this.associatedArea.hide();
        this.associatedProfileUriField.val('');
    },
    
    hideAssociatingOptionsArea: function() {
        this.associationOptionsArea.hide();
        this.associateProfileNameField.val('');
        this.newProfileClassSelector.selectedIndex = 0;
    },
    
    showExternalAuthInUseMessage: function() {
        this.hideAssociatedProfileArea();
        this.hideAssociatingOptionsArea();

        this.externalAuthIdInUseMessage.show();
    },
    
    showAssociatedProfileArea: function(name, uri, url) {
        this.hideExternalAuthInUseMessage();
        this.hideAssociatingOptionsArea();

        if (this.associationEnabled) {
            this.associatedProfileNameSpan.html(name);
            this.associatedProfileUriField.val(uri);
            this.verifyUrl = url;
            this.associatedArea.show();
        }
    },
    
    showAssociatingOptionsArea: function() {
        this.hideExternalAuthInUseMessage();
        this.hideAssociatedProfileArea();

        if (this.associationEnabled) {
            this.newProfileClassHasChanged();
            this.associationOptionsArea.show();
        }
    },
    
}
 
$(document).ready(function() {   
    associateProfileFields.onLoad();
}); 
        
