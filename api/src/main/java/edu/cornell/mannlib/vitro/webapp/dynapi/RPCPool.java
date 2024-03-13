/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullRPC;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RPC;

public class RPCPool extends AbstractPool<String, RPC, RPCPool> {

    private static RPCPool INSTANCE = new RPCPool();

    public static RPCPool getInstance() {
        return INSTANCE;
    }

    @Override
    public RPCPool getPool() {
        return getInstance();
    }

    @Override
    public RPC getDefault() {
        return NullRPC.getInstance();
    }

    @Override
    public Class<RPC> getType() {
        return RPC.class;
    }

}
