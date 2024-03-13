/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;

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
        return NullResourceAPI.getInstance();
    }

    @Override
    public Class<ResourceAPI> getType() {
        return ResourceAPI.class;
    }
}
