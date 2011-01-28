/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var browseClassGroups = {
    // Initial page setup
    onLoad: function() {
        this.mergeFromTemplate();
        this.initObjects();
    },
    
    // Add variables from browse template
    mergeFromTemplate: function() {
        $.extend(this, browseData);
    },
    
    // Create references to frequently used elements for convenience
    initObjects: function() {
        this.browseClassLinks = $('#vgraph-classes li a');
    },
    
    // Retrieve classes 
    graphSetup: function() {
        var values = [],
            labels = [],
            uris = [];
        this.browseClassLinks.each(function() {
            var count = $(this).children('span').text();
            var count = parseInt(count.slice(1, -1), 10);
            var percentage = parseInt(Math.round((count / browseClassGroups.classGroupIndividualCount) * 100), 10);
            var name = $(this).text();
            var countStart = name.lastIndexOf(' (');
            var name = name.substring(0, countStart);
            // alert(name +' | ' + count +' | '+ percentage);
            if ( percentage > 0) {
                values.push(percentage);
                var name = name +' ('+ percentage +'%)';
                labels.push(name);
                uris.push($(this).attr("data-uri"));
            }
        });
        
        // Send the parameters to build the pie chart
        graphClasses.piechart(values, labels, uris);
    }
};

var graphClasses = {
    // Build the pie chart using gRaphael
    piechart: function(values, labels, uris) {
        // Clear the existing pie chart
        $('#menupage-graph').empty();
        
        // Create the canvas
        var r = Raphael("menupage-graph", 500, 300);
        
        // Setup the colors for the slices
        // colors = ['#192933', '#26404E', '#294656', '#194c68', '#487A96', '#63A8CE', '#67AED6','#758A96', '#9DB9C9' ];
        colors = ['#143D52', '#1F5C7A', '#297AA3', '#3399CC', '#5CADD6', '#85C2E0', '#ADD6EB', '#ADCBDD', '#D6EBF5', '#E9F1F5' ];
        // Reverse colors to see how it looks with larger slices in lighter hues:
        // colors = colors.reverse();
        
        // Now draw the pie chart
        var pie = r.g.piechart(150, 142, 100, values, {legend: labels, legendmark: "square", legendpos: "east", colors: colors});
        pie.hover(function () {
            this.sector.stop();
            this.sector.scale(1.1, 1.1, this.cx, this.cy);
            if (this.label) {
                this.label[0].stop();
                this.label[0].scale(1.5);
                this.label[1].attr({"font-weight": 800});
            }
        }, function () {
            this.sector.animate({scale: [1, 1, this.cx, this.cy]}, 500, "bounce");
            if (this.label) {
                this.label[0].animate({scale: 1}, 500, "bounce");
                this.label[1].attr({"font-weight": 400});
            }
        });
        
        // Can't reliably link the slices at the moment. Will come up with a solution if we end up sticking with the pie charts
        // $('path').click(function() {
        //     var index = $('path').index(this);
        //     var uri = uris[index];
        //     // var link = browseClassGroups.baseUrl + '/individuallist?vclassId=' + encodeURIComponent(uri);
        //     window.location = "#browse-by";
        //     browseByVClass.getIndividuals(uri);
        //     return false;
        // });
    }
};

$(document).ready(function() {
    browseClassGroups.onLoad();
    browseClassGroups.graphSetup();
    // graphClasses.piechart();
});