/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Map;

public interface DependencyInfo {

    public Map<String, ProcedureDescriptor> getDependencies();

}
