package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;

public class ActionPoolBulkOperation extends PoolBulkOperation {

	public ActionPoolBulkOperation(){
		this.pool = ActionPool.getInstance();
	}
}
