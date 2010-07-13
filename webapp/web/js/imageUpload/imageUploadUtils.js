/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

  $("#photoUploadContainer").removeClass("hidden");


  $('a.delete').click(function(){
   // var answer = confirm('Are you sure you want to '+ jQuery(this).attr('title') +'?' );
   var answer = confirm('Are you sure you want to delete your photo?');
    // jQuery(this).attr('title') gets anchor title attribute
    return answer;
  });

});