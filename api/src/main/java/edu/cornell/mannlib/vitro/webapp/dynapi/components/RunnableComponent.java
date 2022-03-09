package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;

public interface RunnableComponent extends Removable {

    public OperationResult run(OperationData input);

}
