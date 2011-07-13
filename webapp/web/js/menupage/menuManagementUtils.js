/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
//  menu management util tools
    
$(document).ready(function(){

    // reveal/unveil custom template field
    $('input.default-template').click(function(){
         if ( this.checked ) {
         // If checked, hide this input element
            $('#custom-template').addClass('hidden');
         } 
         else {
         // if not checked, display them
            $('#custom-template').removeClass('hidden');
         }
    });
    
    $('input.custom-template').click(function(){
         if ( this.checked ) {
         // If checked, hide this input element
            $('#custom-template').removeClass('hidden');
        // And clear any values entered in the password fields
            //$('input[name=confirmPassword]').val("");
         } 
         //else {
         // if not checked, display them
          //  $('#custom-template').removeClass('hidden');
         //}
    });
    
    // Check/unckeck all account for deletion
    $('input:checkbox[name=allSelected]').click(function(){
         if ( this.checked ) {
         // if checked, select all the checkboxes
         $('input:checkbox[name=classInClassGroup]').attr('checked','checked');
            
         } else {
         // if not checked, deselect all the checkboxes
           $('input:checkbox[name=classInClassGroup]').removeAttr('checked');
         }
    });
    
    $('input:checkbox[name=classInClassGroup]').click(function(){
        $('input:checkbox[name=allSelected]').removeAttr('checked');
    });
});