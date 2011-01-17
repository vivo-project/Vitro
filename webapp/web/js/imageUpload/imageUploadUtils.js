/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

  // Confirmation alert for photo deletion in image upload and individual templates
  $('#photoUploadDefaultImage a.thumbnail,#share-contact a:eq(2)').click(function(){
    //$('#share-contact a [title="delete this relationship"]').click(function(){  
   var answer = confirm('Are you sure you want to delete your photo?');
    return answer;
  });
});