package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BooleanView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PoolAtomicOperation extends PoolOperation {

    private static final Log log = LogFactory.getLog(PoolAtomicOperation.class);

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
    }

    protected OperationResult getComponentsStatus(DataStore dataStore) {
        List<String> uris = getComponentUris(dataStore);
        List<JsonContainer> containers = getOutputJsonObjects(dataStore);
        for (String uri : uris) {
            boolean loaded = pool.isInPool(uri);
            for (JsonContainer container : containers) {
                container.addKeyValue(uri, BooleanView.createData("status", loaded));
            }
        }
        return OperationResult.ok();
    };

    protected OperationResult loadComponents(DataStore dataStore) {
        List<String> uris = getComponentUris(dataStore);
        for (String uri : uris) {
            boolean loaded = false;
            try {
                pool.load(uri);
                loaded = true;
            } catch (Throwable t) {
                log.error(t, t);
            }
        }
        return OperationResult.ok();
    };

    protected OperationResult unloadComponents(DataStore dataStore) {
        List<String> uris = getComponentUris(dataStore);
        for (String uri : uris) {
            pool.unload(uri);
        }
        return OperationResult.ok();
    };

    protected OperationResult reloadComponents(DataStore dataStore) {
        List<String> uris = getComponentUris(dataStore);
        for (String uri : uris) {
            pool.unload(uri);
            boolean loaded = false;
            try {
                pool.load(uri);
                loaded = true;
            } catch (Throwable t) {
                log.error(t, t);
            }
        }
        return OperationResult.ok();
    };

    private List<String> getComponentUris(DataStore dataStore) {
        List<String> uris = new LinkedList<String>();
        uris.addAll(getResouceParametersUris(dataStore));
        uris.addAll(getJsonArraysUris(dataStore));
        uris.addAll(getPlainStringUris(dataStore));
        return uris;
    }

    private List<String> getPlainStringUris(DataStore dataStore) {
        return SimpleDataView.getPlainStringList(inputParams, dataStore);
    }

    private List<String> getJsonArraysUris(DataStore dataStore) {
        return JsonContainerView.getStringListFromJsonArrays(inputParams, dataStore);
    }

    private List<String> getResouceParametersUris(DataStore dataStore) {
        return RdfView.getUris(dataStore, inputParams);
    }

    public boolean isInputValid(DataStore dataStore) {
        if (!super.isInputValid(dataStore)) {
            return false;
        }
        List<String> uris = getComponentUris(dataStore);
        if (uris.size() == 0) {
            log.debug("No component URIs provided");
            return false;
        }
        return true;
    };
}
