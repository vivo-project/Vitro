package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryResultsMapCache implements AutoCloseable {
	private static final Log log = LogFactory.getLog(QueryResultsMapCache.class);

    private static ThreadLocal<HashMap<String, List<String>>> threadLocal = new ThreadLocal<HashMap<String, List<String>>>();

    public QueryResultsMapCache() {
		threadLocal.set(new HashMap<String, List<String>>());
		log.debug("Query results map cache initialized");
	}
    
    @Override
	public void close() throws IOException {
		threadLocal.remove();
		log.debug("QueryResultsMapCache is closed");
	}

	public static HashMap<String, List<String>> get() {
		HashMap<String, List<String>> queryResultsMap = threadLocal.get();
    	if (queryResultsMap == null) {
    		queryResultsMap = new HashMap<String, List<String>>();
    		log.debug("Use a non-cached query results map");
    	} else {
    		log.debug("Use cached query results map");
    	}
		return queryResultsMap;
	}

	public static void update(HashMap<String, List<String>> queryResultsMap) {
		if (threadLocal.get() != null ) {
			threadLocal.set(queryResultsMap);
    		log.debug("Query results map cache has been updated");
		} else {
			log.debug("Query results map cache has not been updated as it wasn't initialized");	
		}
	}
}
