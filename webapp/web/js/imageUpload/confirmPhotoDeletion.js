/* $This file is distributed under the terms of the license in /doc/license.txt$ */
function delete_photo(passUrl)  {
	
	var delete_photo_answer = confirm ("Are you sure you want to delete your photo?");

		if (delete_photo_answer){
			window.open(passUrl);
			}	
}

