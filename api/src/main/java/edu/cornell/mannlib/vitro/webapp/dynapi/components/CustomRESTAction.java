package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class CustomRESTAction implements Removable {

	private String name;
	private RPC targetRPC;

	@Override
	public void dereference() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		return name;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

	public RPC getTargetRPC() {
		return targetRPC;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#forwardsTo", minOccurs = 1, maxOccurs = 1)
	public void setTargetRPC(RPC rpc) {
		this.targetRPC = rpc;
	}

}
