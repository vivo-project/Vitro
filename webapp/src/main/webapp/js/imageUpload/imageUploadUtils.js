/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

  // Confirmation alert for photo deletion in image upload and individual templates
  $('#photoUploadDefaultImage a.thumbnail, a.delete-mainImage').click(function(){
      var answer = confirm(i18n_confirmDelete);
      return answer;
  });
});