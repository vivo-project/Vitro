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
    },
    
    // Create references to frequently used elements for convenience
    initObjects: function() {
        this.vClassesInClassGroup = $('ul#classes-in-classgroup');
        this.browseClassGroupLinks = $('#browse-classgroups li a');
    },
    
    // Event listeners. Called on page load
    bindEventListeners: function() {
        // Listener for classGroup switching
        this.browseClassGroupLinks.click(function() {
            uri = $(this).attr("data-uri");
            browseClassGroups.getVClasses(uri);
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
            $('#visual-graph svg text').eq(classIndex).toggle();
            return false;
        }, function() {
            var classIndex = $('ul#classes-in-classgroup li a').index(this);
            $('#visual-graph svg path').eq(classIndex).attr('fill', '#999');
            $('#visual-graph svg text').eq(classIndex).toggle();
        })
    },
    
    // Load classes and chart for default class group as defined by template
    defaultClassGroup: function() {
        if ( this.defaultBrowseClassGroupURI != "false" ) {
            this.getVClasses(this.defaultBrowseClassGroupUri);
        }
    },
    
    // Where all the magic happens -- gonna fetch me some classes
    getVClasses: function(classgroupUri, alpha) {
        url = this.dataServiceUrl + encodeURIComponent(classgroupUri);
        if ( alpha && alpha != "all") {
            url = url + '&alpha=' + alpha;
        }
        
        // First wipe currently displayed classes
        this.vClassesInClassGroup.empty();
        
        var values = [],
            labels = [],
            uris = [];
        
        $.getJSON(url, function(results) {
            $.each(results.classes, function(i, item) {
                name = results.classes[i].name;
                uri = results.classes[i].URI;
                indivCount = results.classes[i].entityCount;
                indexUrl = browseClassGroups.baseUrl + '/individuallist?vclassId=' + encodeURIComponent(uri);
                // Only add to the arrays and render classes when they aren't empty
                if ( indivCount > 0 ) {
                    values.push(parseInt(indivCount, 10));
                    labels.push(name + ' (' + parseInt(indivCount, 10) +')');
                    uris.push(uri);
                    
                    // Build the content of each list item, piecing together each component
                    listItem = '<li role="listitem">';
                    listItem += '<a href="'+ indexUrl +'" title="Browse all '+ name +' content">'+ name;
                    listItem += ' <span class="count-individuals">('+ indivCount +')</span></a>';
                    listItem += '</li>';
                    browseClassGroups.vClassesInClassGroup.append(listItem);
                }
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
        // Clear the existing bar chart
        $('#visual-graph').empty();
        
        var height = values.length * 37;
        
        // Create the canvas
        var r = Raphael("visual-graph", 300, height + 10);
        
        var chart = r.g.hbarchart(0, 16, 300, height, [values], {type:"soft", singleColor:"#999"});
        
        // Add the class names as labels and then hide them
        chart.label(labels, true);
        // Getting a JS error in the console when trying to add the class
        // "setting a property that has only a getter"
        // $('svg text').addClass('hidden');
        // so using .hide() instead
        $('svg text').hide();
        
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
            var countStart = label.lastIndexOf(' (');
            var label = label.substring(0, countStart);
            var title = 'View all '+ label +' content';
            
            // Add a title attribute
            $(this).attr('title', title);
        });
        
        // On hover
        // 1. Change bar color
        // 2. Reveal label
        // 3. Highlight class name in main list
        chart.hover(function() {
            this.bar.attr({fill: "#ccc"});
            $('rect').hover(function() {
                var index = $('rect').index(this);
                $('svg text').eq(index).show();
                $('#classes-in-classgroup li a').eq(index).addClass('selected');
            })
        }, function() {
            this.bar.attr({fill: "#999"});
            $('rect').hover(function() {
                var index = $('rect').index(this);
                $('svg text').eq(index).hide();
                $('#classes-in-classgroup li a').eq(index).removeClass('selected');
            })
        });
    }
};

$(document).ready(function() {
    browseClassGroups.onLoad();
    browseClassGroups.defaultClassGroup();
});