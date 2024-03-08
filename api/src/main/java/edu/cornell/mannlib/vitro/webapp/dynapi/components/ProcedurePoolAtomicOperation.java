package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;

public class ProcedurePoolAtomicOperation extends PoolAtomicOperation {

	public ProcedurePoolAtomicOperation(){
		this.pool = ProcedurePool.getInstance();
	}
}
