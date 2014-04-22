/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;

/**
 * TODO
 */
public class SearchResponseStub implements SearchResponse {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	public static final SearchResponseStub EMPTY_RESPONSE = null;
	

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public SearchResultDocumentList getResults() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchResponseStub.getResults() not implemented.");
	}

	@Override
	public Map<String, Map<String, List<String>>> getHighlighting() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchResponseStub.getHighlighting() not implemented.");
	}

	@Override
	public SearchFacetField getFacetField(String name) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchResponseStub.getFacetField() not implemented.");
	}

	@Override
	public List<SearchFacetField> getFacetFields() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchResponseStub.getFacetFields() not implemented.");
	}

}
