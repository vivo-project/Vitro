/* $This file is distributed under the terms of the license in /doc/license.txt$ */



$(document).ready(function(){
	
  $('#takeuri').submit(function() {
    if ($('#uri1').val() == '') {
      alert('Please enter a value for Individual URI 1.');
      return false;
    }
    if ($('#uri2').val() == '') {
      alert('Please enter a value for Individual URI 2.');
      return false;
    }
    if (!$('#uri1').val().match(/\/$/)) {
        $('#uri1').val($('#uri1').val() + "/");
    }
    if (!$('#uri2').val().match(/\/$/)) {
        $('#uri2').val($('#uri2').val() + "/");
    }
    if ($('#uri1').val() == $('#uri2').val()){
      alert('Primary and duplicate URI cannot be same.');
      return false;
    }
  });
  
});