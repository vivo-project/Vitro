/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class NullProcedure extends Procedure {

    private static NullProcedure instance = new NullProcedure();

    @Override
    public OperationResult run(DataStore input) {
        return OperationResult.badRequest();
    }

    public static NullProcedure getInstance() {
        return instance;
    }

    private NullProcedure() {
    }
}
