/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

    $.extend(this, individualLocalName);
    adjustFontSize();
    checkLocationHash();
    
    // controls the property group tabs
    let showAllBtn = $('#show-all-tabs')[0];
    let tabList = $('ul.propertyTabsList')[0];

    showAllBtn.addEventListener('show.bs.tab', function (event) {
        event.preventDefault()
        showAllTabs();
        manageLocalStorage();
    })

    showAllBtn.addEventListener('hide.bs.tab', function (event) {
        $(".tab-content>section.tab-pane").removeClass('show active')
    })

    tabList.addEventListener('shown.bs.tab', function (event) {
        manageLocalStorage();
    })


    function showAllTabs() {
        $('.propertyTabsList.nav.nav-tabs > li').each(function() {
            $(this).attr("aria-selected", "false");
            $(this).removeClass("active");
        });
        
        $('#show-all-tabs').addClass("active").attr("aria-selected", "true");
        $(".tab-content>section.tab-pane").addClass('active show')    
    }


    // If users click a marker on the home page map, they are taken to the profile
    // page of the corresponding country. The url contains the word "Research" in
    // the location hash. Use this to select the Research tab, which displays the
    // researchers who have this countru as a geographic focus.
    function checkLocationHash() {
        if ( location.hash ) {
            // remove the trailing white space
            location.hash = location.hash.replace(/\s+/g, '');
            if ( location.hash.indexOf("map") >= 0 ) {
                // get the name of the group that contains the geographicFocusOf property.
                var tabName = $('h3#geographicFocusOf').parent('article').parent('div').attr("id");
                tabName = tabName.replace("Group","");
                tabNameCapped = tabName.charAt(0).toUpperCase() + tabName.slice(1);
                // if the name of the first tab section = tabName we don't have to do anything;
                // otherwise, select the correct tab and deselect the first one
                var $firstTab = $('li.nav-link').first();
                if ( $firstTab.text() != tabNameCapped ) {
                    // select the correct tab
                    let tabSelect = $('li[groupName="' + tabName + '"]');
                    let tab = new bootstrap.Tab(tabSelect);
                    tab.show()
                }
                // if there is a more link, "click" more to show all the researchers
                // we need the timeout delay so that the more link can get rendered
                setTimeout(geoFocusExpand,250);
            }
            else {
                retrieveLocalStorage();
            }
        }
        else {
            retrieveLocalStorage();
        }
    }

    function geoFocusExpand() {
        // if the ontology is set to collate by subclass, $list.length will be > 0
        // this ensures both possibilities are covered
        var $list = $('ul#geographicFocusOfList').find('ul');
        if ( $list.length > 0 )
        {
            var $more = $list.find('a.more-less');
            $more.trigger("click");
        }
        else {
            var $more = $('ul#geographicFocusOfList').find('a.more-less');
            $more.trigger("click");
        }
    }

    //  Next two functions --  keep track of which property group tab was selected,
    //  so if we return from a custom form or a related individual, even via the back button,
    //  the same property group will be selected as before.
    function manageLocalStorage() {
        var localName = this.individualLocalName;
        // is this individual already stored? If not, how many have been stored?
        // If the answer is 3, remove the first one in before adding the new one
        var current = amplify.store(localName);
        var profiles = amplify.store("profiles");
        if ( current == undefined ) {
            if ( profiles == undefined ) {
                var lnArray = [];
                lnArray.push(localName);
                amplify.store("profiles", lnArray);
            }
            else if ( profiles != undefined && profiles.length >= 3 ) {
                firstItem = profiles[0];
                amplify.store(firstItem, null);
                profiles.splice(0,1);
                profiles.push(localName);
                amplify.store("profiles", profiles)
            }
            else if ( profiles != undefined && profiles.length < 3 ) {
                profiles.push(localName);
                amplify.store("profiles", profiles)
            }
        }
        var selectedTab = [];
        selectedTab.push($('li.nav-link.active').attr('groupName'));
        amplify.store(localName, selectedTab);
        var checkLength = amplify.store(localName);
        if ( checkLength.length == 0 ) {
            amplify.store(localName, null);
        }
    }
    function retrieveLocalStorage() {

        var localName = this.individualLocalName;
        var selectedTab = amplify.store(individualLocalName);

        if ( selectedTab != undefined ) {
            var groupName = selectedTab[0];

            // unlikely, but it's possible a tab that was previously selected and stored won't be
            // displayed because the object properties would have been deleted (in non-edit mode).
            // So ensure that the tab in local storage has been rendered on the page.
            if ( $("ul.propertyTabsList li[groupName='" + groupName + "']").length ) {
                // if the selected tab is the default first one, don't do anything
                if ( $('li.nav-link').first().attr("groupName") != groupName ) {
                    // deselect the default first tab

                    if ( groupName == "viewAll" ) {
                        console.log("View all")
                        showAllTabs();
                    } else {

                        let tabSelect = $('li[groupName="' + groupName + '"]');
                        let tab = new bootstrap.Tab(tabSelect);
                        tab.show();
                    }
                }
            }
        }
    }

    // if there are so many tabs that they wrap to a second line, adjust the font size to
    //prevent wrapping
    function adjustFontSize() {
        var width = 0;
        $('ul.propertyTabsList li').each(function() {
            width += $(this).outerWidth();
        });
        if ( width < 922 ) {
            var diff = 927-width;
            $('ul.propertyTabsList li:last-child').css('width', diff + 'px');
        }
        else {
            var diff = width-926;
            if ( diff < 26 ) {
                $('ul.propertyTabsList li').css('font-size', "0.96em");
            }
            else if ( diff > 26 && diff < 50 ) {
                $('ul.propertyTabsList li').css('font-size', "0.92em");
            }
            else if ( diff > 50 && diff < 80 ) {
                $('ul.propertyTabsList li').css('font-size', "0.9em");
            }
            else if ( diff > 80 && diff < 130 ) {
                $('ul.propertyTabsList li').css('font-size', "0.84em");
            }
            else if ( diff > 130 && diff < 175 ) {
                $('ul.propertyTabsList li').css('font-size', "0.8em");
            }
            else if ( diff > 175 && diff < 260 ) {
                $('ul.propertyTabsList li').css('font-size', "0.73em");
            }
            else {
                $('ul.propertyTabsList li').css('font-size', "0.7em");
            }

            // get the new width
            var newWidth = 0
            $('ul.propertyTabsList li').each(function() {
                newWidth += $(this).outerWidth();
            });
            var newDiff = 926-newWidth;
            $('ul.propertyTabsList li:last-child').css('width', newDiff + 'px');
        }
    }
});


