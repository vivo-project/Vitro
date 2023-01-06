package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;

public class ResourceAPIPool extends VersionableAbstractPool<ResourceAPIKey, ResourceAPI, ResourceAPIPool> {

    private static ResourceAPIPool INSTANCE = new ResourceAPIPool();
    private static ResourceAPI defaultInstance = new DefaultResourceAPI();


    public static ResourceAPIPool getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceAPIPool getPool() {
        return getInstance();
    }

    @Override
    public ResourceAPI getDefault() {
        return defaultInstance;
    }

    @Override
    public Class<ResourceAPI> getType() {
        return ResourceAPI.class;
    }
}
