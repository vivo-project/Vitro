/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;

/**
 * TODO
 */
public class SearchEngineStub implements SearchEngine {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------
	
	Map<String, SearchResponseStub> queryResponses = new HashMap<>();
	
	public void setQueryResponse(String queryText, SearchResponseStub response) {
		queryResponses.put(queryText, response);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public SearchQuery createQuery() {
		return new SearchQueryStub();
	}

	@Override
	public SearchQuery createQuery(String queryText) {
		return new SearchQueryStub(queryText);
	}

	@Override
	public SearchResponse query(SearchQuery query) throws SearchEngineException {
		SearchResponseStub response = queryResponses.get(query.getQuery());
		if (response == null) {
			return SearchResponseStub.EMPTY_RESPONSE;
		} else {
			return response;
		}
	}


	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchEngineStub.startup() not implemented.");
	}

	@Override
	public void shutdown(Application application) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchEngineStub.shutdown() not implemented.");
	}

	@Override
	public void ping() throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException("SearchEngineStub.ping() not implemented.");
	}

	@Override
	public SearchInputDocument createInputDocument() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchEngineStub.createInputDocument() not implemented.");
	}

	@Override
	public void add(SearchInputDocument... docs) throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException("SearchEngineStub.add() not implemented.");
	}

	@Override
	public void add(Collection<SearchInputDocument> docs)
			throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException("SearchEngineStub.add() not implemented.");
	}

	@Override
	public void commit() throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException("SearchEngineStub.commit() not implemented.");
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException("SearchEngineStub.commit() not implemented.");
	}

	@Override
	public void deleteById(String... ids) throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchEngineStub.deleteById() not implemented.");
	}

	@Override
	public void deleteById(Collection<String> ids) throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchEngineStub.deleteById() not implemented.");
	}

	@Override
	public void deleteByQuery(String query) throws SearchEngineException {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SearchEngineStub.deleteByQuery() not implemented.");
	}

}
