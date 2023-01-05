package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullAction;

public class RPCPool extends AbstractPool<String, Action, RPCPool> {

    private static RPCPool INSTANCE = new RPCPool();

    public static RPCPool getInstance() {
        return INSTANCE;
    }

    @Override
    public RPCPool getPool() {
        return getInstance();
    }

    @Override
    public Action getDefault() {
        return new NullAction();
    }

    @Override
    public Class<Action> getType() {
        return Action.class;
    }

}
