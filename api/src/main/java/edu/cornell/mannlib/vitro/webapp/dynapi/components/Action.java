package edu.cornell.mannlib.vitro.webapp.dynapi.components;


import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcessInput;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Action implements RunnableComponent{

 	private static final Log log = LogFactory.getLog(Action.class);

	private Step step = null;
	private String rpcName = null;

	public Action(){

	}

	@Override
	public void dereference() {
		if (step != null) {
			step.dereference();
			step = null;
		}
		rpcName = null;
	}
	
	public ProcessResult run(ProcessInput input) {
		if (step == null) {
			return new ProcessResult(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		return step.run(input);
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#next", minOccurs = 1, maxOccurs = 1)
	public void setStep(ExecutionStep step) {
		this.step = step;
	}	 
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#rpcName", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.rpcName = name;
	}
	
	public String getName() {
		return rpcName;
	}
	
}
