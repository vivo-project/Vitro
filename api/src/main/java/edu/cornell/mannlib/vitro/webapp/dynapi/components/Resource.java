package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Resource implements Poolable {

	private static final Log log = LogFactory.getLog(Resource.class);

	private String name;
	private String versionMin;
	private String versionMax;
	private RPC rpcOnGet;
	private RPC rpcOnPost;
	private RPC rpcOnDelete;
	private RPC rpcOnPut;
	private RPC rpcOnPatch;
	private List<CustomAction> customActions = new LinkedList<CustomAction>();

	private Set<Long> clients = ConcurrentHashMap.newKeySet();

	public String getVersionMin() {
		return versionMin;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#restAPIVersionMin", minOccurs = 0, maxOccurs = 1)
	public void setVersionMin(String versionMin) {
		this.versionMin = versionMin;
	}

	public String getVersionMax() {
		return versionMax;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#restAPIVersionMax", minOccurs = 0, maxOccurs = 1)
	public void setVersionMax(String versionMax) {
		this.versionMax = versionMax;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#resourceName", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	public RPC getRpcOnGet() {
		return rpcOnGet;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onGet", minOccurs = 0, maxOccurs = 1)
	public void setRpcOnGet(RPC rpcOnGet) {
		this.rpcOnGet = rpcOnGet;
	}

	public RPC getRpcOnPost() {
		return rpcOnPost;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onPost", minOccurs = 0, maxOccurs = 1)
	public void setRpcOnPost(RPC rpcOnPost) {
		this.rpcOnPost = rpcOnPost;
	}

	public RPC getRpcOnDelete() {
		return rpcOnDelete;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onDelete", minOccurs = 0, maxOccurs = 1)
	public void setRpcOnDelete(RPC rpcOnDelete) {
		this.rpcOnDelete = rpcOnDelete;
	}

	public RPC getRpcOnPut() {
		return rpcOnPut;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onPut", minOccurs = 0, maxOccurs = 1)
	public void setRpcOnPut(RPC rpcOnPut) {
		this.rpcOnPut = rpcOnPut;
	}

	public RPC getRpcOnPatch() {
		return rpcOnPatch;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onPatch", minOccurs = 0, maxOccurs = 1)
	public void setRpcOnPatch(RPC rpcOnPatch) {
		this.rpcOnPatch = rpcOnPatch;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasCustomAction")
	public void addCustomAction(CustomAction customAction) {
		customActions.add(customAction);
	}

	@Override
	public void dereference() {
		// TODO Auto-generated method stub
		
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

}
