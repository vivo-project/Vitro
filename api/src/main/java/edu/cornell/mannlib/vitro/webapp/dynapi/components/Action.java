package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.AccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.AutoConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Action extends Operation implements Poolable<String>, StepInfo {
    
    private static final Log log = LogFactory.getLog(Action.class);

    private Step firstStep = NullStep.getInstance();
    private String uri;
    private RPC rpc;
    private Set<Long> clients = ConcurrentHashMap.newKeySet();
    private Parameters outputParams = new Parameters();
    private Parameters inputParams = new Parameters();
    private Parameters internalParams = new Parameters();
    private List<AccessWhitelist> accessWhitelists = new LinkedList<AccessWhitelist>();

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
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
        whiteList.setActionName(rpc.getName());
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasFirstStep", maxOccurs = 1)
    public void setStep(Step step) {
        this.firstStep = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasAssignedRPC", minOccurs = 1, maxOccurs = 1)
    public void setRPC(RPC rpc) {
        this.rpc = rpc;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addProvidedParameter(Parameter param) {
        outputParams.add(param);
    }

    @Override
    public String getKey() {
        return rpc.getName();
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

    @Override
    public void addClient() {
        clients.add(Thread.currentThread().getId());
    }

    @Override
    public void removeClient() {
        clients.remove(Thread.currentThread().getId());
    }

    @Override
    public void removeDeadClients() {
        Set<Long> currentClients = new HashSet<Long>();
        currentClients.addAll(clients);
        Map<Long, Boolean> currentThreadIds = Thread.getAllStackTraces()
                .keySet()
                .stream()
                .collect(Collectors.toMap(Thread::getId, Thread::isAlive));
        for (Long client : currentClients) {
            if (!currentThreadIds.containsKey(client) || currentThreadIds.get(client) == false) {
                log.error("Removed left client thread with id " + client);
                clients.remove(client);
            }
        }
    }

    @Override
    public boolean hasClients() {
        return !clients.isEmpty();
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
        if (!(super.isOutputValid(dataStore))) {
            return false;
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
		return false;
	}
	
	@Override
	public Map<String, ProcedureDescriptor> getDependencies() {
	    return firstStep.getDependencies();
	}

}
