package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.AccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.AutoConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Procedure extends AbstractPoolComponent implements Operation, Poolable<String>, StepInfo {
    
    private static final Log log = LogFactory.getLog(Procedure.class);

    private Step firstStep = NullStep.getInstance();
    private Parameters outputParams = new Parameters();
    private Parameters inputParams = new Parameters();
    private Parameters internalParams = new Parameters();
    private boolean publicAcess = false;
    private List<AccessWhitelist> accessWhitelists = new LinkedList<AccessWhitelist>();

    @Override
    public void dereference() {
    }

    @Override
    public OperationResult run(DataStore dataStore) {
        return firstStep.run(dataStore);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#accessWhiteList")
    public void addAccessFilter(AccessWhitelist whiteList) {
        accessWhitelists.add(whiteList);
        whiteList.setProcedureName(getUri());
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasFirstStep", maxOccurs = 1)
    public void setStep(Step step) {
        this.firstStep = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addProvidedParameter(Parameter param) {
        outputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#publicAccess")
    public void setPublicAccess(boolean access) {
        publicAcess = access;
    }
    
    @Override
    public String getKey() {
        return getUri();
    }

    @Override
    public boolean isValid() {
        boolean result = false;
        try {
            computeParams();
            result = true;
        } catch (Exception e) {
           log.error(e,e);
        }
        return result;
    }

    public Parameters getInternalParams() {
        return internalParams;
    }
    
    @Override
    public Parameters getInputParams() {
        return inputParams;
    }
    
    @Override
    public Parameters getOutputParams() {
        return outputParams;
    }

    @Override
    public boolean isOutputValid(DataStore dataStore) {
        Parameters providedParams = getOutputParams();
        if (providedParams != null) {
            for (String name : providedParams.getNames()) {
                if (!dataStore.contains(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
            }
        }
        if (inputParams != null) {
            for (String name : inputParams.getNames()) {
                Parameter param = inputParams.get(name);
                Data data = dataStore.getData(name);
                if (!param.isValid(name, data)) {
                    return false;
                }
            }
        }

        return true;
    }
    
    private void computeParams() {
        AutoConfiguration.computeParams(this);
    }

    @Override
    public Set<StepInfo> getNextNodes() {
        return Collections.singleton(firstStep);
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public boolean isOptional() {
		// TODO Auto-generated method stub
    	return false;
    }

	public String getInputSerializedSchema() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getOutputTemplate() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getInputPath() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getOutputPath() {
		// TODO Auto-generated method stub
		return "";
	}

	public boolean hasPermissions(UserAccount user) {
		if (isPublicAccessible()) {
			return true;
		}
		if (user == null) {
			return false;
		}
		if (user.isRootUser()) {
			return true;
		}
		for (AccessWhitelist filter : accessWhitelists) {
			if (filter.isAuthorized(user)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPublicAccessible() {
		return publicAcess;
	}
	
	@Override
	public Map<String, ProcedureDescriptor> getDependencies() {
	    return firstStep.getDependencies();
	}
	
	public boolean isInputValid(DataStore dataStore) {
        Parameters inputParams = getInputParams();
        for (String name : inputParams.getNames()) {
            if (!dataStore.contains(name)) {
                log.error("Parameter " + name + " not found");
                return false;
            }
            Parameter param = inputParams.get(name);
            Data data = dataStore.getData(name);
            if (!param.isValid(name, data)) {
                return false;
            }
        }
        return true;
    }

}
