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
        this.vgraphVClasses = $('#vgraph-classes');
        this.vgraphVClassLinks = $('#vgraph-classes li a');
        this.browseVClasses = $('#browse-classes');
        this.browseVClassLinks = $('#browse-classes li a');
        this.selectedBrowseVClass = $('#browse-classes li a.selected');
        this.alphaIndex = $('#alpha-browse-individuals');
        this.alphaIndexLinks = $('#alpha-browse-individuals li a');
        this.selectedAlphaIndex = $('#alpha-browse-individuals li a.selected');
        this.paginationNav = $('nav.pagination');
        this.paginationLinks = $('nav.pagination li a');
        this.individualsInVClass = $('#individuals-in-class ul');
        this.individualsContainer = $('#individuals-in-class');
    },
    
    // Event listeners. Called on page load
    bindEventListeners: function() {
        // Listeners for vClass switching
        this.vgraphVClassLinks.click(function() {
            uri = $(this).attr('data-uri');
            browseByVClass.getIndividuals(uri);
        });
        
        this.browseVClassLinks.click(function() {
            uri = $(this).attr('data-uri');
            browseByVClass.getIndividuals(uri);
            return false;
        });
        
        // Listener for alpha switching
        this.alphaIndexLinks.click(function() {
            uri = $('#browse-classes li a.selected').attr('data-uri');
            alpha = $(this).attr('data-alpha');
            // alpha = $(this).text().substring(0, 1);
            browseByVClass.getIndividuals(uri, alpha);
            return false;
        });
        
        // Call the pagination listener
        this.paginationListener();
    },
    
    paginationListener: function() {
        // Listener for page switching
        // separate from the rest because it needs to be callable
        $('nav.pagination li a').click(function() {
            uri = $('#browse-classes li a.selected').attr('data-uri');
            alpha = $('#alpha-browse-individuals li a.selected').attr('data-alpha');
            page = $(this).attr('class').substring(4,5);
            browseByVClass.getIndividuals(uri, alpha, page);
            return false;
        });
    },
    
    // Load individuals for default class as specified by menupage template
    defaultVClass: function() {
        if ( this.defaultBrowseVClassURI != "false" ) {
            this.getIndividuals(this.defaultBrowseVClassUri);
        }
    },
    
    getIndividuals: function(vclassUri, alpha, page) {
        url = this.dataServiceUrl + encodeURIComponent(vclassUri);
        if ( alpha && alpha != "all") {
            url += '&alpha=' + alpha;
        }
        if ( page ) {
            url += '&page=' + page;
        } else {
            page = 1;
        }
        
        // First wipe currently displayed individuals and existing pagination
        this.individualsInVClass.empty();
        $('nav.pagination').remove();
        
        $.getJSON(url, function(results) {
            // Check to see if we're dealing with pagination
            if ( results.pages.length ) {
                pages = results.pages;
                
                pagination = '<nav class="pagination menupage">';
                pagination += '<h3>page</h3>';
                pagination += '<ul>';
                $.each(pages, function(i, item) {
                    anchorOpen = '<a class="page'+ pages[i].text +' round" href="#" title="View page '+ pages[i].text +' of the results">';
                    anchorClose = '</a>';
                    
                    pagination += '<li class="page'+ pages[i].text;
                    pagination += ' round';
                    // Test for active page
                    if ( pages[i].text == page) {
                        pagination += ' selected';
                        anchorOpen = "";
                        anchorClose = "";
                    }
                    pagination += '" role="listitem">';
                    pagination += anchorOpen;
                    pagination += pages[i].text;
                    pagination += anchorClose;
                    pagination += '</li>';
                })
                pagination += '</ul>';
                
                // Add the pagination above and below the list of individuals and call the listener
                browseByVClass.individualsContainer.prepend(pagination);
                browseByVClass.individualsContainer.append(pagination);
                browseByVClass.paginationListener();
            }
            
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
                if ( typeof results.individuals[i].thumbUrl !== "undefined" ) {
                    listItem += '<img src="'+ image +'" width="90" height="90" alt="'+ label +'" /><h1 class="thumb">';
                } else {
                    listItem += '<h1>';
                }
                listItem += '<a href="'+ profileUrl +'" title="View the profile page for '+ label +'">'+ label +'</a></h1>';
                // Include the moniker only if it's not empty and not equal to the VClass name
                if ( moniker != vclassName && moniker != "" ) {
                    listItem += '<span class="title">'+ moniker +'</span>';
                }
                listItem += '</li>';
                browseByVClass.individualsInVClass.append(listItem);
            })
            
            // set selected class, alpha and page
            browseByVClass.selectedVClass(results.vclass.URI);
            browseByVClass.selectedAlpha(alpha);
        });
    },
    
    selectedVClass: function(vclassUri) {
        // Remove active class on all vClasses
        $('#browse-classes li a.selected').removeClass('selected');
        // Can't figure out why using this.selectedBrowseVClass doesn't work here
        // this.selectedBrowseVClass.removeClass('selected');
        
        // Add active class for requested vClass
        $('#browse-classes li a[data-uri="'+ vclassUri +'"]').addClass('selected');
    },

    selectedAlpha: function(alpha) {
        // if no alpha argument sent, assume all
        if ( alpha == null ) {
            alpha = "all";
        }
        // Remove active class on all letters
        $('#alpha-browse-individuals li a.selected').removeClass('selected');
        
        // Add active class for requested alpha
        $('#alpha-browse-individuals li a[data-alpha="'+ alpha +'"]').addClass('selected');
    }
};

$(document).ready(function() {
    browseByVClass.onLoad();
    browseByVClass.defaultVClass();
});