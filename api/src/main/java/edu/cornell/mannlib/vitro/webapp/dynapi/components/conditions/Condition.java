package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public interface Condition {
    
    public boolean isSatisfied(OperationData input);

    public Parameters getRequiredParams();

}
