function validate_upload_file(form_passed)  {
	
	var msg="";

	if (form_passed.datafile.value == "") msg += "Please browse and select a photo\n";

	
	if (msg == "") {

		document.form_upload_image.submit();
		
		
	} else{
		
		alert(msg);
		return false;
		
		}

	
}