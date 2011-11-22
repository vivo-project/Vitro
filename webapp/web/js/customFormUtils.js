/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var vitro;

// vitro == null: true
// vitro === null: false (only true if undefined)
// typeof vitro == 'undefined': true
if (!vitro) { 
	vitro = {};
}

vitro.customFormUtils = {
		
	hideForm: function() {
		this.hideFields(this.form);
	},
	
	clearFormData: function() {
		this.clearFields(this.form);
	},

    // This method should always be called instead of calling hide() directly on any
    // element containing form fields.
    hideFields: function(el) {
        // Clear any input and error messages, so if we re-show the element it won't still be there.
    	this.clearFields(el);
        el.hide();
    },
    
    // Clear data from form elements in element el
    clearFields: function(el) {
    	el.find(':input[type!="hidden"][type!="submit"][type!="button"]').each(function() {
            $(this).val('');
            // Remove a validation error associated with this element.
            // For now we can remove the error elements. Later we may include them in
            // the markup, for customized positioning, in which case we will empty them
            // but not remove them here. See findValidationErrors().  
            $('[id=' + $(this).attr('id') + '_validationError]').remove();
        });
    	    
    },
       
    // Return true iff there are validation errors on the form
    //Updating to include new functionality where errors are on top of page
    findValidationErrors: function() {
    	var validationErrorSection = $('#error-alert');
    	if(validationErrorSection.length > 0) {
    		return true;
    	}
        return this.form.find('.validationError').length > 0;
    	
// RY For now, we just need to look for the presence of the error elements.
// Later, however, we may generate empty error messages in the markup, for
// customized positioning, in which case we need to look for whether they have 
// content. See clearFormData().
//    	var foundErrors = false,
//    	    errors = this.form.find('.validationError'),
//    	    numErrors = errors.length,
//    	    i,
//    	    error;
//    	
//    	for (i = 0; foundErrors == false && i < numErrors; i++) {
//    		error = errors[i];
//    		if (error.html() != '') {
//    			foundErrors = true;
//    		}
//    	}
//    	
//    	return foundErrors;
    },
    
    capitalize: function(word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}