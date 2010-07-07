/* $This file is distributed under the terms of the license in /doc/license.txt$ */

function validate_upload_file(form_passed){

    if (form_passed.datafile.value == "") {
        alert ("Please browse and select a photo");
        return false;
    }
}
