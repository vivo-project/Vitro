package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ResourceAPIPool;

public class ResourceAPIPoolAtomicOperation extends PoolAtomicOperation{

	public ResourceAPIPoolAtomicOperation(){
		this.pool = ResourceAPIPool.getInstance();
	}
}
