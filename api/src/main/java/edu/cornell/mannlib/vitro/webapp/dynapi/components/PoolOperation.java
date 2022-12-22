package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.AbstractPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class PoolOperation extends Operation {

    private static final Log log = LogFactory.getLog(PoolOperation.class);
	public static enum OperationType {
		LOAD, UNLOAD, RELOAD, STATUS
	};

	protected Parameters inputParams = new Parameters();
	protected Parameters outputParams = new Parameters();
	protected AbstractPool pool;
	protected OperationType operationType;

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#poolOperationType", minOccurs = 1, maxOccurs = 1)
	public void setOperationType(String type) throws InitializationException {
		String upperCaseType = type.toUpperCase();
        if (EnumUtils.isValidEnum(OperationType.class, upperCaseType)) {
			operationType = EnumUtils.getEnum(OperationType.class, upperCaseType);
		} else {
			String message = "Provided operation type '" + type + "' is not supported. Supported operations: "
					+ Arrays.asList(OperationType.values());
			throw new InitializationException(message);
		}
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
	public void addOutputParameter(Parameter param) {
		outputParams.add(param);
	}

	@Override
	public OperationResult run(DataStore dataStore) {
		if (!isValid(dataStore)) {
			return OperationResult.internalServerError();
		}
		if (OperationType.LOAD.equals(operationType)) {
			return loadComponents(dataStore);
		} else if (OperationType.RELOAD.equals(operationType)) {
			return reloadComponents(dataStore);
		} else if (OperationType.UNLOAD.equals(operationType)) {
			return unloadComponents(dataStore);
		} else if (OperationType.STATUS.equals(operationType)) {
			return getComponentsStatus(dataStore);
		} 
		//Not implemented operation?
		return OperationResult.internalServerError();
	}

	protected abstract OperationResult getComponentsStatus(DataStore dataStore);

	protected abstract OperationResult unloadComponents(DataStore dataStore);

	protected abstract OperationResult reloadComponents(DataStore dataStore);

	protected abstract OperationResult loadComponents(DataStore dataStore);

	@Override
	public void dereference() {}

	@Override
	public Parameters getInputParams() {
		return inputParams;
	}

	@Override
	public Parameters getOutputParams() {
		return outputParams;
	}
	
    protected boolean isValid(DataStore dataStore) {
        for (String paramName : inputParams.getNames()) {
            if (!dataStore.contains(paramName)) {
                log.debug("Parameter '" + paramName + "' wasn't provided");
                return false;
            }
        }
        return true;
    };

    protected List<JsonContainer> getOutputJsonObjects(DataStore dataStore) {
        return JsonContainerView.getOutputJsonObjectList(outputParams, dataStore);
    }
}
