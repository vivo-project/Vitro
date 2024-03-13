/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import edu.cornell.mannlib.vitro.webapp.dynapi.RPCPool;

public class RPCPoolAtomicOperation extends PoolAtomicOperation {

    public RPCPoolAtomicOperation() {
        this.pool = RPCPool.getInstance();
    }
}
