package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullAction;

public class ActionPool extends AbstractPool<String, Action, ActionPool> {

    private static ActionPool INSTANCE = new ActionPool();
    private static Action defaultInstance = new NullAction();

    public static ActionPool getInstance() {
        return INSTANCE;
    }

    @Override
    public ActionPool getPool() {
        return getInstance();
    }

    @Override
    public Action getDefault() {
        return defaultInstance;
    }

    @Override
    public Class<Action> getType() {
        return Action.class;
    }

}
