package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.RPC;

public class NullRPC extends RPC {

    private static final NullRPC INSTANCE = new NullRPC();

    public static NullRPC getInstance() {
        return INSTANCE;
    }
    
    private NullRPC() {}
}
