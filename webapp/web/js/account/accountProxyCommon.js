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
 * 1) a link with attribute templatePart="remove"
 * 2) a link with attribute templatePart="restore"
 * 3) a hidden field with attribute templatePart="uriField" and value="%uri%" see below
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
	
	var existed = existing;
	var removed = false;

	var content = template.replace(/%uri%/g, uri).replace(/%label%/g, label)
			.replace(/%classLabel%/g, classLabel).replace(/%imageUrl%/g,
					imageUrl);

	this.toString = function() {
		return "proxyInfoElement: " + content;
	}

	this.element = function() {
		var element = $("<div name='proxyInfoElement'>" + content + "</div>");
		var removeLink = $("[templatePart='remove']", element).first();
		var restoreLink = $("[templatePart='restore']", element).first();
		var proxyUriField = $("[templatePart='uriField']", element);

		var setClass = function(r) {
			if (r) {
				element.removeClass('new existing').addClass('removed')
			} else if (existed) {
				element.removeClass('new removed').addClass('existing')
			} else {
				element.removeClass('removed existing').addClass('new')
			}
		}

		var setRemoved = function(r) {
			removed = r;
			if (r) {
				removeLink.hide();
				restoreLink.show();
				proxyUriField.attr('disabled', 'disabled');
				setClass(r);
			} else {
				removeLink.show();
				restoreLink.hide();
				proxyUriField.attr('disabled', '');
				setClass(r);
			}
		}

		removeLink.click(function(event) {
			setRemoved(true);
			return false;
		});

		restoreLink.click(function(event) {
			setRemoved(false);
			return false;
		});

		setRemoved(removed);

		return element;
	}
}
