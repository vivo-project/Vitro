package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Resource;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResource;

public class ResourcePool extends VersionableAbstractPool<ResourceKey, Resource, ResourcePool> {

	private static ResourcePool INSTANCE = new ResourcePool();

	public static ResourcePool getInstance() {
		return INSTANCE;
	}

	@Override
	public ResourcePool getPool() {
		return getInstance();
	}

	@Override
	public Resource getDefault() {
		return new DefaultResource();
	}

	@Override
	public Class<Resource> getType() {
		return Resource.class;
	}

}
