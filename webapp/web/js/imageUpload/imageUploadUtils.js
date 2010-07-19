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


//The above code for revealing the photo upload form doesn't work  IE 6 or 7. The code below fix the problem

var Browser = {
  Version: function() {
    var version;
    if (navigator.appVersion.indexOf("MSIE") != -1)
      version = parseFloat(navigator.appVersion.split("MSIE")[1]);
    return version;
  }
}

if (Browser.Version() <= 7) {
  document.getElementById('photoUploadContainer').style.display = 'block';
}


