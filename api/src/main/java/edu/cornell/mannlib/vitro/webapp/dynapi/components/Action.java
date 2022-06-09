package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.AutoConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Action extends Operation implements Poolable<String>, StepInfo {
    
    private static final Log log = LogFactory.getLog(Action.class);

    private Step firstStep = NullStep.getInstance();
    private RPC rpc;

    private Set<Long> clients = ConcurrentHashMap.newKeySet();

    private Parameters providedParams = new Parameters();
    private Parameters requiredParams = new Parameters();

    @Override
    public void dereference() {
        firstStep.dereference();
        firstStep = null;
        rpc.dereference();
        rpc = null;
    }

    @Override
    public OperationResult run(OperationData input) {
        return firstStep.run(input);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasFirstStep", minOccurs = 1, maxOccurs = 1)
    public void setStep(Step step) {
        this.firstStep = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasAssignedRPC", minOccurs = 1, maxOccurs = 1)
    public void setRPC(RPC rpc) {
        this.rpc = rpc;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addProvidedParameter(Parameter param) {
        providedParams.add(param);
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


    @Override
    public Parameters getRequiredParams() {
        return requiredParams;
    }

    @Override
    public Parameters getProvidedParams() {
        return providedParams;
    }

    @Override
    public boolean isOutputValid(OperationData inputOutput) {
        if (!(super.isOutputValid(inputOutput))) {
            return false;
        }
        Parameters providedParams = getRequiredParams();
        if (providedParams != null) {
            for (String name : providedParams.getNames()) {
                Parameter param = providedParams.get(name);
                String[] outputValues = inputOutput.get(name);
                if (!param.isValid(name, outputValues)) {
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
        return false;
    }

}
