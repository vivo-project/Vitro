/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
        
    $.extend(this, individualLocalName);
    
    retrieveLocalStorage();

    // expands/collapses the div within each property group
    $.each($('section.property-group'), function() {
        var groupName = $(this).attr("id");
        $(this).children("nav").children("img").click(function() {
            if ( $("div[id='" + groupName + "Group']").is(":visible") ) {
                $("div[id='" + groupName + "Group']").slideUp(222);
                $(this).attr("src", $(this).attr("src").replace("collapse-prop-group","expand-prop-group"));
                $("section#" + groupName).children("h2").removeClass("expandedPropGroupH2");
            }
            else {
                $("div[id='" + groupName + "Group']").slideDown(222);
                $(this).attr("src", $(this).attr("src").replace("expand-prop-group","collapse-prop-group"));
                $("section#" + groupName).children("h2").addClass("expandedPropGroupH2");
            }
            manageLocalStorage();
        });
    });


    // expands/collapses all property groups together
    $.each($('a#propertyGroupsToggle'), function() {
        $('a#propertyGroupsToggle').click(function() {
            var anchorHtml = $(this).html();
            if ( anchorHtml.indexOf('expand') > -1 ) {
                $.each($('section.property-group'), function() {
                    $("div[id='" + $(this).attr("id") + "Group']").slideDown(222);
                    var innerSrc = $(this).children("nav").children("img").attr("src");
                    $(this).children("nav").children("img").attr("src",innerSrc.replace("expand-prop-group","collapse-prop-group"));
                    $(this).children("h2").addClass("expandedPropGroupH2");
                });
                $(this).html("collapse all");
            }
            else {
                $.each($('section.property-group'), function() {
                    $("div[id='" + $(this).attr("id") + "Group']").slideUp(222);
                    var innerSrc = $(this).children("nav").children("img").attr("src");
                    $(this).children("nav").children("img").attr("src",innerSrc.replace("collapse-prop-group","expand-prop-group"));
                    $(this).children("h2").removeClass("expandedPropGroupH2");
                });
                $(this).html("expand all");
            }
            manageLocalStorage();
        });
   }); 
   
    //  Next two functions --  keep track of which property group tabs have been expanded,
    //  so if we return from a custom form or a related individual, even via the back button,
    //  the property groups will be expanded as before.
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
        var groups = [];
        $.each($('section.property-group').children("nav").children("img"), function() {
            if ( $(this).attr('src').indexOf('collapse-prop-group') > -1 ) {
                groups.push($(this).attr('groupName'));
            }
        });
        amplify.store(localName, groups);
        var checkLength = amplify.store(localName);
        if ( checkLength.length == 0 ) {
            amplify.store(localName, null);
        }
    }

    function retrieveLocalStorage() {
        var localName = this.individualLocalName;
        var groups = amplify.store(individualLocalName);
            if ( groups != undefined ) {
                for ( i = 0; i < groups.length; i++) {
                    var groupName = groups[i];
                    // unlikely, but it's possible a group that was previously opened and stored won't be displayed
                    // because the object properties would have been deleted. So ensure that the group in local
                    // storage has been rendered on the page. More likely, a user navigated from a quick view to a full
                    // profile, opened a group, then navigated back to the quick view where the group isn't rendered.
                    if ($("section#" + groupName).length ) {
                        $("div[id='" + groupName + "Group']").slideDown(1);
                        $("img[groupName='" + groupName + "']").attr("src", $("img[groupName='" + groupName + "']").attr("src").replace("expand-prop-group","collapse-prop-group"));
                        $("section#" + groupName).children("h2").addClass("expandedPropGroupH2");
                    }
                }
                if ( groups.length == $('section.property-group').length ) {
                    $('a#propertyGroupsToggle').html('collapse all');
                }
            }
        }
});
