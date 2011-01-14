/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var browseByVClass = {
    // Initial page setup
    onLoad: function() {
        this.mergeFromTemplate();
        this.initObjects();
        this.bindEventListeners();
    },
    
    // Add variables from menupage template
    mergeFromTemplate: function() {
        $.extend(this, menupageData);
    },
    
    // Create references to frequently used elements for convenience
    initObjects: function() {
        this.vgraphVClasses = $('#vgraph-childClasses');
        this.vgraphVClassLinks = $('#vgraph-childClasses li a');
        this.browseVClasses = $('#browse-childClasses');
        this.browseVClassLinks = $('#browse-childClasses li a');
        this.selectedBrowseVClass = $('#browse-childClasses li a.selected');
        this.alphaIndex = $('#alpha-browse-childClass');
        this.alphaIndexLinks = $('#alpha-browse-childClass li a');
        this.selectedAlphaIndex = $('#alpha-browse-childClass li a.selected');
        this.individualsInVClass = $('#individuals-in-childClass ul');
    },
    
    // Event listeners. Called on page load
    bindEventListeners: function() {
        // Listeners for vClass switching
        this.vgraphVClassLinks.click(function() {
            uri = $(this).attr("data-uri");
            browseByVClass.getIndividuals(uri);
        });
        
        this.browseVClassLinks.click(function() {
            uri = $(this).attr("data-uri");
            browseByVClass.getIndividuals(uri);
            return false;
        })
        
        // Listener for alpha switching
        this.alphaIndexLinks.click(function() {
            uri = $('#browse-childClasses li a.selected').attr("data-uri");
            alpha = $(this).attr("data-alpha");
            // alpha = $(this).text().substring(0, 1);
            browseByVClass.getIndividuals(uri, alpha);
            return false;
        })
    },
    
    // Load individuals for default class as specified by menupage template
    defaultVClass: function() {
        if ( this.defaultBrowseVClassURI != "false" ) {
            this.getIndividuals(this.defaultBrowseVClassUri);
        }
    },
    
    getIndividuals: function(vclassUri, alpha) {
        url = this.dataServiceUrl + encodeURIComponent(vclassUri);
        if ( alpha && alpha != "all") {
            url = url + '&alpha=' + alpha;
        }
        
        // First wipe currently displayed individuals
        this.individualsInVClass.empty();
        
        $.getJSON(url, function(results) {
            $.each(results.individuals, function(i, item) {
                label = results.individuals[i].label;
                moniker = results.individuals[i].moniker;
                vclassName = results.individuals[i].vclassName;
                uri = results.individuals[i].URI;
                profileUrl = results.individuals[i].profileUrl;
                if ( results.individuals[i].thumbUrl ) {
                    image = browseByVClass.baseUrl + results.individuals[i].thumbUrl;
                }
                // Build the content of each list item, piecing together each component
                listItem = '<li class="individual" role="listitem" role="navigation">';
                if ( typeof image !== "undefined" ) {
                    listItem += '<img src="'+ image +'" width="90" height="90" alt="'+ label +'" />';
                }
                listItem += '<h1><a href="'+ profileUrl +'" title="View the profile page for '+ label +'">'+ label +'</a></h1>';
                // Include the moniker only if it's not empty and not equal to the VClass name
                if ( moniker != vclassName && moniker != "" ) {
                    listItem += '<p>'+ moniker +'</p>';
                }
                listItem += '</li>';
                browseByVClass.individualsInVClass.append(listItem);
            })
            // set selected class and alpha
            browseByVClass.selectedVClass(results.vclass.URI);
            browseByVClass.selectedAlpha(alpha);
        });
    },
    
    selectedVClass: function(vclassUri) {
        // Remove active class on all vClasses
        $('#browse-childClasses li a.selected').removeClass('selected');
        // Can't figure out why using this.selectedBrowseVClass doesn't work here
        // this.selectedBrowseVClass.removeClass('selected');
        
        // Add active class for requested vClass
        $('#browse-childClasses li a[data-uri="'+ vclassUri +'"]').addClass('selected');
    },

    selectedAlpha: function(alpha) {
        // if no alpha argument sent, assume all
        if ( alpha == null ) {
            alpha = "all";
        }
        // Remove active class on all letters
        $('#alpha-browse-childClass li a.selected').removeClass('selected');
        
        // Add active class for requested alpha
        $('#alpha-browse-childClass li a[data-alpha="'+ alpha +'"]').addClass('selected');
    }
};

$(document).ready(function() {
    browseByVClass.onLoad();
    browseByVClass.defaultVClass();
});