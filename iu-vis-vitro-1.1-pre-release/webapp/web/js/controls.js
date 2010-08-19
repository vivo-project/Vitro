/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
 toggles admin panel using jQuery
*/

$(document).ready(function(){
  
  // Sliding admin panel
  $("div.admin .toggle").css("cursor","pointer");
  $("div.admin .toggle").click(function(){
    $(this).parent().children("div.panelContents").slideToggle("fast");
  })
  
  $("div.navlinkblock").removeClass("navlinkblock").addClass("navlinkblock-collapsed").click(function(){
    $(this).removeClass("navlinkblock-collapsed").toggleClass("navlinkblock-expanded");
  });
  
  
})