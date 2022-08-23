package edu.cornell.mannlib.vitro.webapp.dynapi.components;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class NullAction extends Action {

    private static final Log log = LogFactory.getLog(NullAction.class);
    
    @Override
    public OperationResult run(DataStore input) {
        return OperationResult.badRequest();
    }

}
