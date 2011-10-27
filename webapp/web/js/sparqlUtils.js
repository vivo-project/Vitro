/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/*
 * Parse the results that we got from the SparqlQueryAjaxController.
 * 
 * The input is a complex structure from the controller. The output is an array
 * of maps where each map represents a result row, populated with key-value pairs.
 */
sparqlUtils = {
    parseSparqlResults: function(data) {
    	var parsed = [];
    	$.each(data.results.bindings, function() {
    		var row = {};
    		for (var i in this) {
    			row[i] = this[i].value;
    		}
    		parsed.push(row);
    	});
    	return parsed;
    }
};

