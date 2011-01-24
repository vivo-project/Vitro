/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var browseClassGroups = {
    // Initial page setup
    onLoad: function() {
        this.mergeFromTemplate();
        this.initObjects();
        // this.bindEventListeners();
    },
    
    // Add variables from menupage template
    mergeFromTemplate: function() {
        $.extend(this, browseData);
    },
    
    // Create references to frequently used elements for convenience
    initObjects: function() {
        this.vClassesInClassGroup = $('ul#classgroup-list');
        this.browseClassGroupLinks = $('#browse-classgroups li a');
    },
    
    // Event listeners. Called on page load
    bindEventListeners: function() {
        // Listener for classGroup switching
        this.browseClassGroupLinks.click(function() {
            uri = $(this).attr("data-uri");
            browseClassGroups.getVClasses(uri);
            return false;
        })
    },
    
    // Load classes and chart for default class group as defined by template
    defaultClassGroup: function() {
        if ( this.defaultBrowseClassGroupURI != "false" ) {
            this.getVClasses(this.defaultBrowseClassGroupUri);
        }
    },
    
    getVClasses: function(classgroupUri, alpha) {
        url = this.dataServiceUrl + encodeURIComponent(classgroupUri);
        if ( alpha && alpha != "all") {
            url = url + '&alpha=' + alpha;
        }
        
        // First wipe currently displayed classes
        this.vClassesInClassGroup.empty();
        
        $.getJSON(url, function(results) {
            $.each(results.vClasses, function(i, item) {
                name = results.vClasses[i].name;
                uri = results.vClasses[i].URI;
                indivCount = results.vClasses[i].individualCount;
                indexUrl = browseClassGroups.baseUrl + '/individuallist?vclassId=' + encodeURIComponent(uri);
                // Build the content of each list item, piecing together each component
                listItem = '<li role="listitem">';
                listItem += '<a href="'+ indexUrl +'" title="View all '+ name +'content">'+ name +'</a>';
                // Include the moniker only if it's not empty and not equal to the VClass name
                listItem += ' <span class="count-individuals">(${indivCount})</span>'
                listItem += '</li>';
                browseClassGroups.vClassesInClassGroup.append(listItem);
            })
            // set selected class group
            browseClassGroups.selectedClassGroup(results.vclassGroup.URI);
        });
    },
    
    selectedClassGroup: function(vclassUri) {
        // Remove active class on all vClasses
        $('#browse-childClasses li a.selected').removeClass('selected');
        // Can't figure out why using this.selectedBrowseVClass doesn't work here
        // this.selectedBrowseVClass.removeClass('selected');
        
        // Add active class for requested vClass
        $('#browse-childClasses li a[data-uri="'+ vclassUri +'"]').addClass('selected');
    }
};

var graphClassGroups = {
    
    barchart: function() {
        var values = [],
            labels = [];
            uris = [];
        $("tr").each(function() {
            values.push(parseInt($("td", this).text(), 10));
            labels.push($("th", this).text());
            uris.push($("th", this).attr("data-uri"));
        });
        var height = values.length * 37;
        
        // Create the canvas
        var r = Raphael("pieViz", 300, height + 10);
        
        // Hide the table containing the data to be graphed
        $('table.graph-data').addClass('hidden');
        var chart = r.g.hbarchart(0, 16, 300, height, [values], {type:"soft", singleColor:"#999"});
        
        // Add the class names as labels and then hide them
        chart.label(labels, true);
        // getting a JS error in the console when trying to add the class
        // "setting a property that has only a getter"
        // $('svg text').addClass('hidden');
        // so using .hide() instead
        $('svg text').hide();
        
        // Was unable to append <a> within <svg> -- was always hidden and couldn't get it to display
        // So using jQuery click to add links
        $('rect').click(function() {
            var index = $('rect').index(this);
            var uri = uris[index];
            var link = browseClassGroups.baseUrl + '/individuallist?vclassId=' + encodeURIComponent(uri);
            window.location = link;
        })
        
        // On hover
        // 1. Change bar color
        // 2. Reveal label
        // 3. Highlight class name in main list
        chart.hover(function() {
            this.bar.attr({fill: "#ccc"});
            $('rect').hover(function() {
                var index = $('rect').index(this);
                $('svg text').eq(index).show();
                $('#classgroup-list li a').eq(index).addClass('selected');
            })
        }, function() {
            this.bar.attr({fill: "#999"});
            $('rect').hover(function() {
                var index = $('rect').index(this);
                $('svg text').eq(index).hide();
                $('#classgroup-list li a').eq(index).removeClass('selected');
            })
        });
    }
};

$(document).ready(function() {
    browseClassGroups.onLoad();
    // browseClassGroups.defaultClassGroup();
    graphClassGroups.barchart();
});