/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

  // login form is hidden by default; use JavaScript to reveal
  $("#formLogin").removeClass("hidden");
  
  // focus on email or newpassword field
  $('.focus').focus();

});

//The above code for revealing the login form doesn't work  IE 6 or 7. The code below  fix the problem

var Browser = {
  Version: function() {
    var version;
    if (navigator.appVersion.indexOf("MSIE") != -1)
      version = parseFloat(navigator.appVersion.split("MSIE")[1]);
    return version;
  }
}

if (Browser.Version() <= 7) {
  document.getElementById('formLogin').style.display = 'block';
}


