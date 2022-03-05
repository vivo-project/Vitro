package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResourceAPI;

public class ResourceAPIPool extends VersionableAbstractPool<ResourceAPIKey, ResourceAPI, ResourceAPIPool> {

	private static ResourceAPIPool INSTANCE = new ResourceAPIPool();

	public static ResourceAPIPool getInstance() {
		return INSTANCE;
	}

	@Override
	public ResourceAPIPool getPool() {
		return getInstance();
	}

	@Override
	public ResourceAPI getDefault() {
		return new DefaultResourceAPI();
	}

	@Override
	public Class<ResourceAPI> getType() {
		return ResourceAPI.class;
	}

}
