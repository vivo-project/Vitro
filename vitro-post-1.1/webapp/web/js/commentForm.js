/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function ValidateForm(formName) {
    var x = 0; // counts form elements - used as array index
    var y = 0; // counts required fields - used as array index
    errors = false;
    var errorList;

    if (document.forms[formName].RequiredFields) {
        errorList = 'Please fill out the following required fields:\n';
        // build array of required fields
        reqStr = document.forms[formName].RequiredFields.value;
        requiredFields = reqStr.split(',');
        // build array holding the names of required fields as
        // displayed in error box
        if (document.forms[formName].RequiredFieldsNames) {
            reqNameStr = document.forms[formName].RequiredFieldsNames.value;
        } else {
            reqNameStr = document.forms[formName].RequiredFields.value;
        }
        requiredNames = reqNameStr.split(',');
        // Loop through form elements, checking for required fields
        while ((x < document.forms[formName].elements.length)) {
            if (document.forms[formName].elements[x].name == requiredFields[y]) {
                if (document.forms[formName].elements[x].value == '') {
                    errorList += requiredNames[y] + '\n';
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

    // Check for Email formatting
    if (document.forms[formName].EmailFields) {
        errorList = 'Please format your e-mail address as: \"userid@institution.edu\" or enter another complete email address';
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