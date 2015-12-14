/* $This file is distributed under the terms of the license in /doc/license.txt$ */

$(document).ready(function(){
    
    $.fn.exists = function () {
        return this.length !== 0;
    }
    
    $.fn.moreLess = function () {
        $(this).each
    }
    
    var togglePropDisplay = {
        showMore: function($toggleLink, $itemContainer) {
            $toggleLink.click(function() {
                $itemContainer.show();
                $(this).attr('href', '#show less content');
                $(this).text(i18nStrings.displayLess);
                togglePropDisplay.showLess($toggleLink, $itemContainer);
                return false;
            });
        },
        
        showLess: function($toggleLink, $itemContainer) {
            $toggleLink.click(function() {
                $itemContainer.hide();
                $(this).attr('href', '#show more content');
                $(this).text(i18nStrings.displayMoreEllipsis);
                togglePropDisplay.showMore($toggleLink, $itemContainer);
                return false;
            });
        }
    };
    
    // var $propList = $('.property-list').not('>li>ul');
    var $propList = $('.property-list:not(:has(>li>ul))');
    $propList.each(function() {
	    var limit = $(this).attr("displayLimit"); 
        var $additionalItems = $(this).find('li:gt(' + (limit - 1) + ')');
        if ( $additionalItems.exists() ) {
            // create container for additional elements
            var $itemContainer = $('<div class="additionalItems" />').appendTo(this);
            
            // create toggle link
            var $toggleLink = $('<a class="more-less" href="#show more content" title="' + i18nStrings.showMoreContent + '">' + i18nStrings.displayMoreEllipsis + '</a>').appendTo(this);
            
            $additionalItems.appendTo($itemContainer);
            
            $itemContainer.hide();
            
            togglePropDisplay.showMore($toggleLink, $itemContainer);
        }
    });
    
    var $subPropList = $('.subclass-property-list');
    $subPropList.each(function() {
		var limit = $(this).parent().parent().attr("displayLimit"); 
        var $additionalItems = $(this).find('li:gt(' + (limit - 1) + ')');
        if ( $additionalItems.exists() ) {
            // create container for additional elements
            var $itemContainer = $('<div class="additionalItems" />').appendTo(this);
            
            // create toggle link
            var $toggleLink = $('<a class="more-less" href="#show more content" title="' + i18nStrings.showMoreContent + '">' + i18nStrings.displayMoreEllipsis + '</a>').appendTo(this);
            
            $additionalItems.appendTo($itemContainer);
            
            $itemContainer.hide();
            
            togglePropDisplay.showMore($toggleLink, $itemContainer);
        }
    });
    
    var $subPropSibs = $subPropList.closest('li').last().nextAll();
    var $subPropParent = $subPropList.closest('li').last().parent();
    var $additionalItems = $subPropSibs.slice(3);
    if ( $additionalItems.length > 0 ) {
        // create container for additional elements
        var $itemContainer = $('<div class="additionalItems" />').appendTo($subPropParent);
        
        // create toggle link
        var $toggleLink = $('<a class="more-less" href="#show more content" title="' + i18nStrings.showMoreContent + '">' + i18nStrings.displayMoreEllipsis + '</a>').appendTo($subPropParent);
        
        $additionalItems.appendTo($itemContainer);
        
        $itemContainer.hide();
        
        togglePropDisplay.showMore($toggleLink, $itemContainer);
    }
    
});
