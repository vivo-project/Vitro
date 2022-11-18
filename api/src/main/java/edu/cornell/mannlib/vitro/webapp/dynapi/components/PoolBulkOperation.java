package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public abstract class PoolBulkOperation extends PoolOperation {

    private static final Log log = LogFactory.getLog(PoolBulkOperation.class);

	protected OperationResult getComponentsStatus(DataStore dataStore) {
	    List<String> uris = pool.getLoadedUris();
		return OperationResult.internalServerError();
	};

	protected OperationResult unloadComponents(DataStore dataStore) {
		List<String> uris = pool.getLoadedUris();
		for (String uri : uris) {
		    pool.unload(uri);
		}
		return OperationResult.ok();
	};

	protected OperationResult reloadComponents(DataStore dataStore) {
	    pool.reload();
		return OperationResult.ok();
	};
	
	protected OperationResult loadComponents(DataStore dataStore) {
        pool.reload();
        return OperationResult.ok();
	};
	
}
