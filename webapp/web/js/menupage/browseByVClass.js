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
        this.individualsInVClass = $('#individuals-in-childClass');
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
        this.getIndividuals(this.defaultBrowseVClassUri);
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
                indivLabel = results.individuals[i].label;
                indivUri = results.individuals[i].URI;
                indivProfileUrl = results.individuals[i].profileUrl;
                if ( !results.individuals[i].thumbUrl ) {
                    indivImage = browseByVClass.baseUrl + '/images/placeholders/person.thumbnail.jpg';
                } else {
                    indivImage = browseByVClass.baseUrl + results.individuals[i].thumbUrl;
                }
                browseByVClass.individualsInVClass.append('<article class="vcard individual-foaf-person" role="navigation"> <img src="'+ indivImage +'" width="90" height="90" alt="'+ indivLabel +'" /><h1 class="fn"><a href="'+ indivProfileUrl +'" title="View the profile page for '+ indivLabel +'">'+ indivLabel + '</a></h1><p>core:preferredTitle <span class="org">org from preferredTitle??</span></p></article>');
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