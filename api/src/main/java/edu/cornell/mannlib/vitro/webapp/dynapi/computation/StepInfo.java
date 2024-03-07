package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.ParameterInfo;

public interface StepInfo extends ParameterInfo {

    public Set<StepInfo> getNextNodes();

    public boolean isRoot();

    public boolean isOptional();
}
