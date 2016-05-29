/* $This file is distributed under the terms of the license in /doc/license.txt$ */
  
/*Display bubble message letting the user knows that it is necessary to create class groups and associate classes with class groups when there is no individual classes to select in Data Input section and 
hide it when there are classes*/
    
$(document).ready(function(){
    
     var classesInSelectList = $('#addIndividualClass option').length;
     
     if (classesInSelectList == 0) {
         $('#addIndividualClass input[type="submit"]').css('opacity','.4').click(function(event){
             event.preventDefault();
             $('#addClassBubble').effect( "shake", {times:2, direction:"up", distance:5}, 50 );
          });
          
         $('#VClassURI').css('width','150px');
         
         $('#addClassBubble').show();
         
     }else{
         $('#addIndividualClass input[type="submit"]').removeClass('opacity');
         
         $('#VClassURI').removeClass('width');
         
         $('.long-options').css('width','300px');
         
         $('#addClassBubble').hide();
     }
});