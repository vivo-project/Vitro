/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$.extend(this, i18nStrings);

function ValidateForm(formName) {
    var x = 0; // counts form elements - used as array index
    var y = 0; // counts required fields - used as array index
    errors = false;
    var errorList;

    // Check for Email formatting
    if (document.forms[formName].EmailFields) {
        errorList = '\n' + i18nStrings.pleaseFormatEmail + '\n\n \"userid@institution.edu\" \n\n' 
                    + i18nStrings.enterValidAddress;
        // build array of required fields
        emailStr = document.forms[formName].EmailFields.value;
        emailFields = emailStr.split(',');
        // build array holding the names of required fields as
        // displayed in error box
        if (document.forms[formName].EmailFieldsNames) {
            emailNameStr = document.forms[formName].EmailFieldsNames.value;
        } else {
            emailNameStr = document.forms[formName].EmailFields.value;
        }
        emailNames = emailNameStr.split(',');
        // Loop through form elements, checking for required fields
        while ((x < document.forms[formName].elements.length)) {
            if (document.forms[formName].elements[x].name == emailFields[y]) {
                if ((document.forms[formName].elements[x].value.indexOf('@') < 1)
                    || (document.forms[formName].elements[x].value.lastIndexOf('.') < document.forms[formName].elements[x].value.indexOf('@')+1)) {
                    errors = true;
                }
                y++;
            }
            x++;
        }
        if (errors) {
            alert(errorList);
            return false;
        }
    x = 0;
    y = 0;
    }

    return true;
}