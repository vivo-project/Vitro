/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var vitro;
if (!vitro) {
	vitro = {};
}


vitro.utils = {

	/* borrowMethods concept from "JavaScript: The Definitive Guide," 5th edition, by David Flanagan
	 * Copyright 2006 O'Reilly Media, Inc.
	 * ISBN 978-0-596-10199-2
	 */
	// Borrow methods from one class for use by another.
	// The arguments should be the constructor functions for the classes.
	// Methods of built-in types such as Object, Array, Date, and RegExp are
	// not enumerable and cannot be borrowed from with this method.
	borrowPrototypeMethods: function(borrowFrom, addTo) {
		borrowMethods(borrowFrom.prototype, addTo.prototype);
	},
	
	borrowMethods: function(borrowFrom, addTo) {
		for (m in borrowFrom) { // loop through all properties of the prototype
			if (typeof borrowFrom[m] != "function") { continue; } // ignore non-functions
			addTo[m] = borrowFrom[m];
		}
	}

};