package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Action implements Poolable<String>, Operation, Link {

	private Step firstStep = null;
	private RPC rpc;

	private Set<Long> clients = ConcurrentHashMap.newKeySet();

	private Parameters providedParams = new Parameters();
	private Parameters requiredParams;

	@Override
	public void dereference() {
		if (firstStep != null) {
			firstStep.dereference();
			firstStep = null;
		}
		rpc.dereference();
		rpc = null;
	}

	@Override
	public OperationResult run(OperationData input) {
		if (firstStep == null) {
			return new OperationResult(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		return firstStep.run(input);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasFirstStep", minOccurs = 1, maxOccurs = 1)
	public void setStep(OperationalStep step) {
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
		return true;
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
		Map<Long, Boolean> currentThreadIds = Thread
				.getAllStackTraces()
				.keySet()
				.stream()
				.collect(Collectors.toMap(Thread::getId, Thread::isAlive));
		for (Long client : clients) {
			if (!currentThreadIds.containsKey(client) || currentThreadIds.get(client) == false) {
				clients.remove(client);
			}
		}
	}

	@Override
	public boolean hasClients() {
		return !clients.isEmpty();
	}

	@Override
	public Set<Link> getNextLinks() {
		return Collections.singleton(firstStep);
	}

	@Override
	public Parameters getRequiredParams() {
		return requiredParams;
	}

	public void computeScopes() {
		requiredParams = Scopes.computeInitialRequirements(this);
	}

	@Override
	public Parameters getProvidedParams() {
		return providedParams;
	}

	@Override
	public boolean isRoot() {
		return true;
	}

}
