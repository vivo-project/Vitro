/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public interface RunnableComponent extends Removable {

    public OperationResult run(DataStore dataStore);

}
