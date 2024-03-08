package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;

public class ProcedurePoolBulkOperation extends PoolBulkOperation {

	public ProcedurePoolBulkOperation(){
		this.pool = ProcedurePool.getInstance();
	}
}
