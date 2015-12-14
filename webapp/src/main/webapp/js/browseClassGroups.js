/* $This file is distributed under the terms of the license in /doc/license.txt$ */


var browseClassGroups = {
    // Initial page setup
    onLoad: function() {
        this.mergeFromTemplate();
        this.initObjects();
        this.bindEventListeners();
    },
    
    // Add variables from browse template
    mergeFromTemplate: function() {
        $.extend(this, browseData);
        $.extend(this, i18nStrings);
    },
    
    // Create references to frequently used elements for convenience
    initObjects: function() {
        this.vClassesInClassGroup = $('ul#classes-in-classgroup');
        this.browseClassGroupLinks = $('#browse-classgroups li a');
        this.browseClasses = $('#browse-classes');
    },
    
    // Event listeners. Called on page load
    bindEventListeners: function() {
        // Listener for classGroup switching
        this.browseClassGroupLinks.click(function() {
            uri = $(this).attr("data-uri");
            individualCount = $(this).attr("data-count");
            browseClassGroups.getVClasses(uri, individualCount);
            return false;
        });
        
        // Call the bar chart highlighter listener
        this.chartHighlighterListener();
    },

    // Listener for bar chart highlighting -- separate from the rest because it needs to be callable
    chartHighlighterListener: function() {
        // This essentially replicates the native Raphael hover behavior (see chart.hover below)
        // but allows us to trigger it via jQuery from the list of classes adjacent to the chart
        $('ul#classes-in-classgroup li a').hover(function() {
            var classIndex = $('ul#classes-in-classgroup li a').index(this);
            $('#visual-graph svg path').eq(classIndex).attr('fill', '#ccc');
            return false;
        }, function() {
            var classIndex = $('ul#classes-in-classgroup li a').index(this);
            $('#visual-graph svg path').eq(classIndex).attr('fill', '#999');
        })
    },
    
    // Load classes and chart for default class group as defined by template
    defaultClassGroup: function() {
        if ( this.defaultBrowseClassGroupURI != "false" ) {
            this.getVClasses(this.defaultBrowseClassGroupUri, this.defaultBrowseClassGroupCount);
        }
    },
    
    // Where all the magic happens -- gonna fetch me some classes
    getVClasses: function(classgroupUri, classGroupIndivCount) {
        url = this.dataServiceUrl + encodeURIComponent(classgroupUri);

        // First wipe currently displayed classes, browse all link, and bar chart
        this.vClassesInClassGroup.empty();
        $('a.browse-superclass').remove();
        $('#visual-graph').empty();
        
        var values = [],
            labels = [],
            uris = [],
            classList = [],
            populatedClasses = 0;
            potentialSuperClasses = [];
        
        $.getJSON(url, function(results) {
            
            $.each(results.classes, function(i, item) {
                name = results.classes[i].name;
                uri = results.classes[i].URI;
                indivCount = results.classes[i].entityCount;
                indexUrl = browseClassGroups.baseUrl +'/individuallist?vclassId='+ encodeURIComponent(uri);
                // Only add to the arrays and render classes when they aren't empty
                if ( indivCount > 0 ) {
                    // if the class individual count is equal to the class group individual count, this could be a super class
                    if ( indivCount == classGroupIndivCount ) {
                        potentialSuperClasses.push(populatedClasses);
                    }
                    
                    values.push(parseInt(indivCount, 10));
                    labels.push(name);
                    uris.push(uri);
                    
                    // Build the content of each list item, piecing together each component
                    listItem = '<li role="listitem">';
                    listItem += '<a href="'+ indexUrl +'" title="' + browseClassGroups.browseAllString + ' ' 
                                + name + ' ' + browseClassGroups.contentString + '">'+ name +'</a>'; 
                    listItem += '</li>';
                    
                    // Add the list item to the array of classes
                    classList.push(listItem);
                    
                    populatedClasses++;
                }
            })
            
            // Test for number of potential super classes. If only 1, then remove it from all arrays
            // But only do so if there are at least 2 classes in the list to begin with
            if ( classList.length > 1 && potentialSuperClasses.length == 1 ){
                // Grab the URI of the super class before splicing
                superClassUri = uris[potentialSuperClasses];
                
                values.splice(potentialSuperClasses, 1);
                labels.splice(potentialSuperClasses, 1);
                uris.splice(potentialSuperClasses, 1);
                classList.splice(potentialSuperClasses, 1);
                
                browseAllUrl = browseClassGroups.baseUrl +'/individuallist?vclassId='+ encodeURIComponent(superClassUri);
                browseAllLink = '<a class="browse-superclass" href="'+ browseAllUrl +'" title="' 
                                + browseClassGroups.browseAllString + ' ' + results.classGroupName 
                                + '">' + browseClassGroups.browseAllString + ' &raquo;</a>';
                browseClassGroups.browseClasses.prepend(browseAllLink);
            }
            
            // Add the classes to the DOM
            $.each(classList, function(i, listItem) {
                browseClassGroups.vClassesInClassGroup.append(listItem);
            })
            
            // Set selected class group
            browseClassGroups.selectedClassGroup(results.classGroupUri);
            
            // Update the graph
            graphClassGroups.barchart(values, labels, uris);
            
            // Call the bar highlighter listener
            browseClassGroups.chartHighlighterListener();
        });
    },
    
    // Toggle the active class group so it's clear which is selected
    selectedClassGroup: function(classGroupUri) {
        // Remove active class on all vClasses
        $('#browse-classgroups li a.selected').removeClass('selected');
        
        // Add active class for requested vClass
        $('#browse-classgroups li a[data-uri="'+ classGroupUri +'"]').addClass('selected');
    }
};

var graphClassGroups = {
    // Build the bar chart using gRaphael
    barchart: function(values, labels, uris) {
        var height = values.length * 37;
        
        // Create the canvas
        var r = Raphael("visual-graph", 225, height + 10);
        
        var chart = r.g.hbarchart(0, 16, 225, height, [values], {type:"soft", singleColor:"#999"});
        
        // Was unable to append <a> within <svg> -- was always hidden and couldn't get it to display
        // so using jQuery click to add links
        $('rect').click(function() {
            var index = $('rect').index(this);
            var uri = uris[index];
            var link = browseClassGroups.baseUrl + '/individuallist?vclassId=' + encodeURIComponent(uri);
            window.location = link;
        });
        
        // Add title attributes to each <rect> in the bar chart
        $('rect').each(function() {
            var index = $('rect').index(this);
            var label = labels[index];
            var title = browseClassGroups.browseAllString + ' ' + label + ' ' + browseClassGroups.contentString;
            
            // Add a title attribute
            $(this).attr('title', title);
        });
        
        // On hover
        // 1. Change bar color
        // 2. Highlight class name in main list
        chart.hover(function() {
            this.bar.attr({fill: "#ccc"});
            $('rect').hover(function() {
                var index = $('rect').index(this);
                $('#classes-in-classgroup li a').eq(index).addClass('selected');
            })
        }, function() {
            this.bar.attr({fill: "#999"});
            $('rect').hover(function() {
                var index = $('rect').index(this);
                $('#classes-in-classgroup li a').eq(index).removeClass('selected');
            })
        });
    }
};

$(document).ready(function() {
    browseClassGroups.onLoad();
    browseClassGroups.defaultClassGroup();
});