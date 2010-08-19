/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

  // upload form is hidden by default; use JavaScript to reveal
  $("#photoUploadContainer").removeClass("hidden");

  // Confirmation alert for photo deletion
  $('a.thumbnail').click(function(){
   var answer = confirm('Are you sure you want to delete your photo?');
    return answer;
  });

});