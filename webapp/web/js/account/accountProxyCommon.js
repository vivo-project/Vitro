/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
 * A collection of building blocks for the proxy-management UI.
 */

/* 
 * ----------------------------------------------------------------------------
 * itemElement
 * ----------------------------------------------------------------------------
 * Display information about an entity according to the template. The entity 
 * can be either:
 *    a profile -- Individual to be edited.
 *    a proxy   -- User Account to do the editing, optionally with info from a 
 *                   profile associated with that individual.
 * 
 * You provide:
 *   template -- the HTML text that determines how the element should look. 
 *             The template must be a single HTML element, which may contain
 *             any number of sub-elements. It needs to have a single outer
 *             wrapper, however.
 *   uri, label, classLabel, imageUrl -- as described below
 *   remove -- a function that we can call when the user clicks on the remove 
 *             link or button. We will pass a reference to this struct.
 * ----------------------------------------------------------------------------
 * The template must inlude a link or button with attribute templatePart="remove"
 * 
 * The template may include tokens to be replaced, from the following:
 *    %uri% -- the URI of the individual being displayed 
 *    %label& -- the label of the individual.
 *    %classLabel% -- the label of the most specific class of the individual.
 *    %imageUrl% -- the URL that will fetch the image of the individual, 
 *                  or a placeholder image.
 * ----------------------------------------------------------------------------
 * This relies on magic names for the styles:
 *   existingProxyItem -- for an item that was present when the page was loaded
 *   newProxyItem      -- for an item that was added since the page was loaded
 *   removedProxyItem  -- added to an item when the "remove" link is cheked.
 * ----------------------------------------------------------------------------
 */
function itemElement(template, uri, label, classLabel, imageUrl, removeInfo) {
	var self = this;
	
	this.uri = uri;
	this.label = label;
	this.classLabel = classLabel;
	this.imageUrl = (imageUrl) ? imageUrl : imageUrl="../images/placeholders/person.thumbnail.jpg";
	this.removeInfo = removeInfo;
	
	this.toString = function() {
		return "itemElement: " + content;
	}

	this.element = function() {
		var content = template.replace(/%uri%/g, this.uri)
				              .replace(/%label%/g, this.label)
				              .replace(/%classLabel%/g, this.classLabel)
				              .replace(/%imageUrl%/g, this.imageUrl);
		
		var element = $(content);
		element.addClass("proxyInfoElement");

		var removeLink = $("[templatePart='remove']", element).first();
		removeLink.click(function(event) {
			self.removeInfo(self);
			return false;
		});

		return element;
	}
}


/* 
 * ----------------------------------------------------------------------------
 * proxyAutoComplete
 * ----------------------------------------------------------------------------
 * Attach the autocomplete funcionality that we like in proxy panels. 
 * 
 * You provide:
 *   parms -- a map containing the URL and the action code needed for the AJAX call.
 *   excludedUris -- these URIs are always filtered out of the results.
 *   getProxyInfos -- a function that will return an array of itemElements
 *   	    that are already present in the list and so should be filtered out of 
 *          the autocomplete response.
 *   addProxyInfo -- a function that we can call when an item is selected.
 *          It will take the selection info, build an itemElement, and add 
 *          it to the panel.
 *   reportSearchStatus -- a function that we can call when a search is done. It
 *          will accept the length of the search term and the number of results, 
 *          and will display it in some way.
 * ----------------------------------------------------------------------------
 * The AJAX request will include a "term" parameter, set to the current search term.
 * ----------------------------------------------------------------------------
 * The functionality includes:
 *   -- fetching data for the autocomplete list.
 *   -- cacheing the fetched data
 *   -- filtering as described above.
 *   -- calling addProxyInfo() and clearing the field when a value is selected.
 * ----------------------------------------------------------------------------
 */
function proxyAutocomplete(parms, excludedUris, getProxyInfos, addProxyInfo, reportSearchStatus) {
	var cache = [];
	
	var filterResults = function(parsed) {
		var filtered = [];
		var existingUris = $.map(getProxyInfos(), function(p) {
			return p.uri;
			});
		$.each(parsed, function(i, p) {
			if ((-1 == $.inArray(p.uri, existingUris))
					&& (-1 == $.inArray(p.uri, excludedUris))) {
				filtered.push(p);
			}
		});
		return filtered;
	}

	var sendResponse = function(request, response, results) {
        reportSearchStatus(request.term.length, results.length);
		response(results);
	}

    this.minLength = 0,
    
    this.source = function(request, response) {
    	if (request.term.length < 3) {
    		sendResponse(request, response, []);
    		return;
    	} 
        if (request.term in cache) {
        	sendResponse(request, response, filterResults(cache[request.term]));
            return;
        }
        $.ajax({
            url: parms.url,
            dataType: 'json',
            data: {
            	action: parms.action,
            	term: request.term
            },
            complete: function(xhr, status) {
                var results = $.parseJSON(xhr.responseText);
                cache[request.term] = results; 
                sendResponse(request, response, filterResults(results));
            }
        });
    }
    
    this.select = function(event, ui) {
    	addProxyInfo(ui.item);
        event.preventDefault();
        event.target.value = '';
	}
}
