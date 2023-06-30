package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Resource implements Versionable<ResourceKey> {

	private String name;
	private String versionMin;
	private String versionMax;
	private RPC rpcOnGet;
	private RPC rpcOnPost;
	private RPC rpcOnDelete;
	private RPC rpcOnPut;
	private RPC rpcOnPatch;
	private List<CustomRESTAction> customRESTActions = new LinkedList<CustomRESTAction>();

	private Set<Long> clients = ConcurrentHashMap.newKeySet();

	@Override
	public String getVersionMin() {
		return versionMin;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#restAPIVersionMin", minOccurs = 0, maxOccurs = 1)
	public void setVersionMin(String versionMin) {
		this.versionMin = versionMin;
	}

	@Override
	public String getVersionMax() {
		return versionMax;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#restAPIVersionMax", minOccurs = 0, maxOccurs = 1)
	public void setVersionMax(String versionMax) {
		this.versionMax = versionMax;
	}

	public String getName() {
		return name;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#resourceName", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ResourceKey getKey() {
		return ResourceKey.of(name, versionMin);
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

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasCustomRESTAction")
	public void addCustomRESTAction(CustomRESTAction customRESTAction) {
		customRESTActions.add(customRESTAction);
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

	public String getCustomRestActionByName(String name) {
		for (CustomRESTAction customRestAction : customRESTActions) {
			if (customRestAction.getName().equals(name)) {
				return customRestAction.getTargetRPC().getName();
			}
		}
		throw new UnsupportedOperationException("Unsupported custom action");
	}

	public String getActionNameByMethod(String method) {
		System.out.println("\nget action name: " + method + "\n");
		switch (method.toUpperCase()) {
			case "POST":
				return getNameOfRpc(rpcOnPost);
			case "GET":
				return getNameOfRpc(rpcOnGet);
			case "DELETE":
				return getNameOfRpc(rpcOnDelete);
			case "PUT":
				return getNameOfRpc(rpcOnPut);
			case "PATCH":
				return getNameOfRpc(rpcOnPatch);
			default:
				throw new UnsupportedOperationException("Unsupported method");
		}
	}

	private String getNameOfRpc(RPC rpc) {
		System.out.println("\nget name of rpc: " + rpc + "\n");
		if (rpc != null) {
			return rpc.getName();
		}
		throw new UnsupportedOperationException("Unable to determine action");
	}

}
