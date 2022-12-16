package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.BooleanView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;

public abstract class PoolBulkOperation extends PoolOperation {

    private static final Log log = LogFactory.getLog(PoolBulkOperation.class);

	protected OperationResult getComponentsStatus(DataStore dataStore) {
	    List<String> uris = pool.getLoadedUris();
	    List<JsonContainer> containers = getOutputJsonObjects(dataStore);
	    for (String uri : uris) {
            boolean loaded = pool.isInPool(uri);
            for (JsonContainer container : containers) {
                container.addKeyValue(uri, BooleanView.createData("status", loaded));
            }
        }
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
