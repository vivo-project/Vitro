// SuperSleight, version 1.1.0
// version 1.1.0 by Jeffrey Barke <http://themechanism.com/> 20071218
// Essential (99% of the) code by Drew McLellan <http://24ways.org/2007/supersleight-transparent-png-in-ie6>
var supersleight = function() {

	// local vars
	var root = false;
	var applyPositioning = false;
	// path to a transparent GIF image
	var shim = './images/x.gif';

	var fnLoadPngs = function() {
		// if supersleight.limitTo called, limit to specified id
		if (root) { root = document.getElementById(root); } else { root = document; }
		// loop
		for (var i = root.all.length - 1, obj = null; (obj = root.all[i]); i--) {
			// background pngs
			if (obj.currentStyle.backgroundImage.match(/\.png/i) !== null) {
				bg_fnFixPng(obj);
			}
			// image elements
			if (obj.tagName == 'IMG' && obj.src.match(/\.png$/i) !== null) {
				el_fnFixPng(obj);
			}
			// apply position to 'active' elements
			if (applyPositioning && (obj.tagName == 'A' || obj.tagName == 'INPUT') && obj.style.position === '') {
				obj.style.position = 'relative';
			}
		}
	};

	var bg_fnFixPng = function(obj) {
		var mode = 'scale';
		var bg = obj.currentStyle.backgroundImage;
		var src = bg.substring(5,bg.length-2);
		if (obj.currentStyle.backgroundRepeat == 'no-repeat') {
			mode = 'crop';
		}
		obj.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + src + "', sizingMethod='" + mode + "')";
		obj.style.backgroundImage = 'url(' + shim + ')';
	};

	var el_fnFixPng = function(img) {
		var src = img.src;
		img.style.width = img.width + 'px';
		img.style.height = img.height + 'px';
		img.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + src + "', sizingMethod='scale')";
		img.src = shim;
	};

	var addLoadEvent = function(func) {
		var oldonload = window.onload;
		if (typeof window.onload != 'function') {
			window.onload = func;
		} else {
			window.onload = function() {
				if (oldonload) {
					oldonload();
				}
				func();
			};
		}
	};

	// supersleight object
	return {
		init: function(strPath, blnPos, strId) {
			if (document.getElementById) {
				if (typeof(strPath) != 'undefined' && null !== strPath) { shim = strPath; }
				if (typeof(blnPos) != 'undefined' && null !== blnPos) { applyPositioning = blnPos; }
				if (typeof(strId) != 'undefined' && null !== strId) { root = strId; }
				addLoadEvent(fnLoadPngs);
			} else {
				return false;
			}
		},
		limitTo: function(el) {
			root = el;
		},
		run: function(strPath, blnPos, strId) {
			if (document.getElementById) {
				if (typeof(strPath) != 'undefined' && null !== strPath) { shim = strPath; }
				if (typeof(blnPos) != 'undefined' && null !== blnPos) { applyPositioning = blnPos; }
				if (typeof(strId) != 'undefined' && null !== strId) { root = strId; }
				fnLoadPngs();
			} else {
				return false;
			}
		}
	};

}();

// limit to part of the page ... pass an ID to limitTo:
// supersleight.limitTo('top');
// optional path to a transparent GIF image, apply positioning, limitTo
supersleight.init();