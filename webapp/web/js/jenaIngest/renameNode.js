/* $This file is distributed under the terms of the license in /doc/license.txt$ */



$(document).ready(function(){
	
  $('#takeuri').submit(function() {
    if ($('#namespace').val() == '') {
      alert('Please enter URI prefix.');
      return false;
    }
  });
  
});