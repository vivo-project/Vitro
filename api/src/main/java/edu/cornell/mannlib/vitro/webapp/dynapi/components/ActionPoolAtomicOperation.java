package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;

public class ActionPoolAtomicOperation extends PoolAtomicOperation {

	public ActionPoolAtomicOperation(){
		this.pool = ActionPool.getInstance();
	}
}
