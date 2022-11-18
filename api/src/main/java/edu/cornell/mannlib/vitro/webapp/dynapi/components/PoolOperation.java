package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Arrays;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.AbstractPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class PoolOperation extends Operation {

    private static final Log log = LogFactory.getLog(PoolOperation.class);
	public static enum Types {
		LOAD, UNLOAD, RELOAD, STATUS
	};

	protected Parameters inputParams = new Parameters();
	protected Parameters outputParams = new Parameters();
	protected AbstractPool pool;
	protected Types operationType;

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#poolOperationType", minOccurs = 0, maxOccurs = 1)
	public void setOperationType(String type) throws InitializationException {
		if (EnumUtils.isValidEnum(Types.class, type.toUpperCase())) {
			operationType = EnumUtils.getEnum(Types.class, type);
		} else {
			String message = "Provided operation type '" + type + "' is not supported. Supported operations: "
					+ Arrays.asList(Types.values());
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
		if (Types.LOAD.equals(operationType)) {
			return loadComponents(dataStore);
		} else if (Types.RELOAD.equals(operationType)) {
			return reloadComponents(dataStore);
		} else if (Types.UNLOAD.equals(operationType)) {
			return unloadComponents(dataStore);
		} else if (Types.STATUS.equals(operationType)) {
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

}
