/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

  $("#photoUploadContainer").removeClass("hidden");

  $('#photoUploadForm form').submit(function() {
    if ($("#datafile").val() == '') {
      alert('Please browse and select a photo.');
      return false;
    }
  });

  $('a.delete').click(function(){
    var answer = confirm('Are you sure you want to '+ jQuery(this).attr('title') +'?' );
    // jQuery(this).attr('title') gets anchor title attribute
    return answer;
  });

});