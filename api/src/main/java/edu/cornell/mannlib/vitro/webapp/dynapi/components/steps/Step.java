package edu.cornell.mannlib.vitro.webapp.dynapi.components.steps;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DependencyInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RunnableComponent;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;

public interface Step extends RunnableComponent, StepInfo, DependencyInfo {

}
