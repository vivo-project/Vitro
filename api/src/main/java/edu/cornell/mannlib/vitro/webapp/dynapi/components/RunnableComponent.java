package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcessInput;

public interface RunnableComponent extends Removable{
	
	public ProcessResult run(ProcessInput input);
	
}
