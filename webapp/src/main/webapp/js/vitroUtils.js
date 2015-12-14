/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

    // Use jQuery() instead of $() alias, because dwr/util.js, loaded on back end editing 
    // pages, overwrites $.
    // fade out welcome-message when user logs in
    jQuery.extend(this, i18nStrings);
    
    jQuery('section#welcome-message').css('display', 'block').delay(2000).fadeOut(1500);
    
    // fade in flash-message when user logs out
    jQuery('section#flash-message').css('display', 'none').fadeIn(1500);
    
    /////////////////////////////
     // Home search filter
     // Toggle filter select list
     var $searchFilterList = $('#filter-search-nav');
     var $isFilterOpen = false;

     $('a.filter-search').click(function(e) {
         e.preventDefault();

         if (!$isFilterOpen) {

             //Change button filter state to selected
             //$(this).css('background','url(../../themes/vivo-cornell/images/filteredSearchActive.gif) no-repeat right top');
             $(this).removeClass('filter-default');
             $(this).addClass('filter-active');

             //Reveal filter select list
             $searchFilterList.css('display','block');

             $isFilterOpen = true;

         } else {
             //Change button filter state to default
             //$('a.filter-search').css('background','url(../../themes/vivo-cornell/images/filteredSearch.gif) no-repeat right top');
             $(this).removeClass('filter-active');
             $(this).addClass('filter-default');

             //Hide filter select list
             $searchFilterList.css('display','none');

             $isFilterOpen = false;

         }
    });

     // Collect users' selection

       $('#filter-search-nav li').each(function(index){
           $(this).click(function(ev){
               ev.preventDefault();

               if ($(this).text() == i18nStrings.allCapitalized) {
                  //Selected filter feedback
                  $('.search-filter-selected').text('');
                  $('input[name="classgroup"]').val('');
               } else {

                     $('.search-filter-selected').text($(this).text()).fadeIn('slow');
                     $('input[name="classgroup"]').val($(this).children("a").attr("title"));
               }

               //Hide filter select list
               $searchFilterList.css('display','none');

               //Change button filter state to default
               //$('a.filter-search').css('background','url(../../themes/vivo-cornell/images/filteredSearch.gif) no-repeat right top');
               $('a.filter-search').removeClass('filter-active');
               $('a.filter-search').addClass('filter-default');

               $isFilterOpen = false;
           });

       });

       //When focus, hide filter select list and change filter button state to default
       $('input.search-homepage').focus(function(){

           $('input.search-homepage').attr("value","");
           $('input.search-homepage').css({
               'text-align' : 'left',
               'opacity' : 1
           });

           if (!$isFilterOpen) {

               $isFilterOpen = false;

           }else {

                //Hide filter select list
                    $('#filter-search-nav').hide();

                    //Change button filter state to default
                    //$('a.filter-search').css('background','url(../../themes/vivo-cornell/images/filteredSearch.gif) no-repeat right top');
                    $('a.filter-search').removeClass('filter-active');
                    $('a.filter-search').addClass('filter-default');

                    $isFilterOpen = false;

             }

       });
});
