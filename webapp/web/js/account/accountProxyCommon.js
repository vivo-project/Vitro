/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
 * A collection of building blocks for the proxy-management UI.
 */

/* 
 * ----------------------------------------------------------------------------
 * proxyInfoElement
 * ----------------------------------------------------------------------------
 * Display information about an entity according to the template. The entity 
 * can be either:
 *    a profile -- Individual to be edited.
 *    a proxy   -- User Account to do the editing, optionally with info from a 
 *                   profile associated with that individual.
 * ----------------------------------------------------------------------------
 * The template must inlude
 * 1) a link with attribute templatePart="remove" and restoreText="[something]"
 * 2) a hidden field with attribute templatePart="uriField" and value="%uri%" see below
 * 
 * The template may include tokens to be replaced, from the following:
 *    %uri% -- the URI of the individual being displayed 
 *    %label& -- the label of the individual.
 *    %classLabel% -- the label of the most specific class of the individual.
 *    %imageUrl% -- the URL that will fetch the image of the individual, 
 *                  or a placeholder image.
 * ----------------------------------------------------------------------------
 */

function proxyInfoElement(template, uri, label, classLabel, imageUrl, existing) {
	this.uri = uri;
	this.label = label;
	this.classLabel = classLabel;
	this.imageUrl = imageUrl;
	
	var existed = existing;
	var removed = false;

	this.toString = function() {
		return "proxyInfoElement: " + content;
	}

	this.element = function() {
		var content = template.replace(/%uri%/g, this.uri)
				              .replace(/%label%/g, this.label)
				              .replace(/%classLabel%/g, this.classLabel)
				              .replace(/%imageUrl%/g, this.imageUrl);

		var element = $("<div name='proxyInfoElement'>" + content + "</div>");
		var removeLink = $("[templatePart='remove']", element).first();
		var removeText = removeLink.text();
		var restoreText = removeLink.attr('restoreText');
		var proxyUriField = $("[templatePart='uriField']", element);

		var showRemoved = function() {
			if (removed) {
				removeLink.text(restoreText);
				proxyUriField.attr('disabled', 'disabled');
				element.addClass('removed');
			} else {
				removeLink.text(removeText);
				proxyUriField.attr('disabled', '');
				element.removeClass('removed');
			}
		}

		removeLink.click(function(event) {
			removed = !removed;
			showRemoved();
			return false;
		});

		element.removeClass('new existing removed');
		element.addClass(existed ? 'existing' : 'new')
		showRemoved()

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
 *   parms -- a map containing the URL of the AJAX controller, the query, and 
 *          the model selector.
 *   getProxyInfos -- a function that will return an array of proxyInfoElements
 *   	    that are already present in the list and so should be filtered out of 
 *          the autocomplete response.
 *   addProxyInfo -- a function that we can call when an item is selected.
 *          It will take the selection info, build a proxyInfoElement, and add 
 *          it to the panel.
 * ----------------------------------------------------------------------------
 * Before executing the AJAX request, the query from the parms map will be modified, 
 * replacing "%term%" with the current search term.
 * ----------------------------------------------------------------------------
 * The functionality includes:
 *   -- fetching data for the autocomplete list.
 *   -- cacheing the fetched data
 *   -- filtering as described above.
 *   -- calling addProxyInfo() and clearing the field when a value is selected.
 * ----------------------------------------------------------------------------
 */
function proxyAutocomplete(parms, getProxyInfos, addProxyInfo) {
	var cache = [];
	
	var filterResults = function(parsed) {
		var filtered = [];
		var existingUris = $.map(getProxyInfos(), function(p) {
			return p.uri;
			});
		$.each(parsed, function(i, p) {
			if (-1 == $.inArray(p.uri, existingUris)) {
				filtered.push(p);
			}
		});
		return filtered;
	}
	
    this.minLength = 3,
    
    this.source = function(request, response) {
        if (request.term in cache) {
        	var filtered = filterResults(cache[request.term]);
            response(filtered);
            return;
        }
        $.ajax({
            url: parms.url,
            dataType: 'json',
            data: {
            	model: parms.model,
            	query: parms.query.replace("%term%", request.term)
            },
            complete: function(xhr, status) {
                var results = $.parseJSON(xhr.responseText);
                var parsed = sparqlUtils.parseSparqlResults(results); 
                cache[request.term] = parsed; 
                var filtered = filterResults(parsed);
                response(filtered);
            }
        });
    }
    
    this.select = function(event, ui) {
    	addProxyInfo(ui.item);
        event.preventDefault();
        event.target.value = '';
	}
    
}

