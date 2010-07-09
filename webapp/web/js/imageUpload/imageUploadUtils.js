/* $This file is distributed under the terms of the license in /doc/license.txt$ */


/* comments */

$(document).ready(function(){

	$("#photoUploadContainer").removeClass("hidden");
	
	
	$('#photoUploadForm form').submit(function() {
	 if (form_passed.datafile.value == "") {
	  alert ("Please browse and select a photo");
	  return false;
	  }
	});
	
	
});


function delete_photo(passUrl)  {
	var delete_photo_answer = confirm ("Are you sure you want to delete your photo?");
		if (delete_photo_answer){
			window.open(passUrl);
			}	
}
