/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){

    // Use jQuery() instead of $() alias, because dwr/util.js, loaded on back end editing 
    // pages, overwrites $.
    // fade out welcome-message when user logs in
    jQuery('section#welcome-message').css('display', 'block').delay(2000).fadeOut(1500);
    
    // fade in flash-message when user logs out
    jQuery('section#flash-message').css('display', 'none').fadeIn(1500);
    
    
    /////////////////////////////
     // Home search fiter
     // Toggle filter select list

     var $searchFilterList = $('#filter-search-nav');
     var $selectedFilter;
     var $queryToSend;
     var $queryToSendAll = true;
     var $isFilterOpen = false;

     console.log("Filter is open = " + $isFilterOpen);

     $('a.filter-search').click(function(e) {
         e.preventDefault();

         if (!$isFilterOpen) {

            console.log("Filer is close = " + $isFilterOpen);

             //Change button filter state to selected
             //$(this).css('background','url(../../themes/vivo-cornell/images/filteredSearchActive.gif) no-repeat right top');
             $(this).removeClass('filter-default');
             $(this).addClass('filter-active');

             //Reveal filter select list
             $searchFilterList.css('display','block');

             $isFilterOpen = true;

             console.log("open");
         } else {
             //Change button filter state to default
             //$('a.filter-search').css('background','url(../../themes/vivo-cornell/images/filteredSearch.gif) no-repeat right top');
             $(this).removeClass('filter-active');
             $(this).addClass('filter-default');

             //Hide filter select list
             $searchFilterList.css('display','none');

             $isFilterOpen = false;

             console.log("close");
         }
    });

     // Collect users' selection

       $('#filter-search-nav li').each(function(index){
           $(this).click(function(ev){
               ev.preventDefault();

               if ($(this).text() == 'All') {
                  $queryToSendAll = true; 
                  //Selected filter feedback
                  $('.search-filter-selected').text('');
                  console.log("ALL");
               } else {

                   //Selected filter feedback
                     $('.search-filter-selected').text($(this).text()).fadeIn('slow');
                     $queryToSendAll = false; 
               }

               //Hide filter select list
               $searchFilterList.css('display','none');

               //Change button filter state to default
               //$('a.filter-search').css('background','url(../../themes/vivo-cornell/images/filteredSearch.gif) no-repeat right top');
               $('a.filter-search').removeClass('filter-active');
               $('a.filter-search').addClass('filter-default');


               $selectedFilter = $(this).text();
               $isFilterOpen = false;
               console.log("$queryToSend " + $selectedFilter);
           });

       });

       //When focus, hide filter select list and change filter button state to default
       $('input.search-homepage').focus(function(){

           $('input.search-homepage').css({
               'background' : 'none',
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

           console.log("HIDE input value ") ;
       });

     $('#search-homepage').submit(function(){

         if ($queryToSendAll) {
             $filterType = '';
         }else {
             $filterType = '&classgroup=http://vivoweb.org/ontology/vitroClassGroup' + $selectedFilter;
         }

         $queryToSend = 'querytext=' + $('input.search-homepage').val()  + $filterType;
         console.log("Query to send: " + $queryToSend);

         return false; 
     });
});