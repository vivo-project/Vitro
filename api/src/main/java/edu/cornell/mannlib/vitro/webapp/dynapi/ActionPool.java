package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullAction;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.ModelValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.SHACLActionBeanValidator;
import org.apache.jena.rdf.model.Model;

public class ActionPool extends AbstractPool<String, Action, ActionPool> {

    private static ActionPool INSTANCE = new ActionPool();

    public static ActionPool getInstance() {
        return INSTANCE;
    }

    @Override
    public ActionPool getPool() {
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

    @Override
    public ModelValidator getValidator(Model data, Model scheme) {
        return new SHACLActionBeanValidator(data, scheme);
    }

}
