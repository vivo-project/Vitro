/* $This file is distributed under the terms of the license in /doc/license.txt$ */
    
$(document).ready(function(){
     //Remove initial value of input text 'Select an existing last name'
    $('input[name="proxySelectorAC"]').click(function(){
        $(this).val('');
        $("span[name='proxySelectorSearchStatus']").text('')
    });
    
    //Alert when user doesn't select an editor and a profile after submitting from for relating proxy-profiles
    $('input[name="createRelationship"]').click(function(){
        var $proxyUri = $('#add-relation input[name="proxyUri"]').val();
        var $profileUri = $('#add-relation input[name="profileUri"]').val();

       if ($proxyUri == undefined || $profileUri == undefined){
           $('#error-alert').removeClass('hidden');
           
           var $errorAlert = $('#error-alert p').html();
           
           if ($errorAlert !=""){
               return false;
           }else{
               $('#error-alert p').append("You must select a minimum of 1 editor and profile.");
               return false;
           }
       }     
    });
});

