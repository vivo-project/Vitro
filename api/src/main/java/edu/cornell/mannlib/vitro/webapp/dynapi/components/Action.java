package edu.cornell.mannlib.vitro.webapp.dynapi.components;


import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Action implements RunnableComponent{

 	private static final Log log = LogFactory.getLog(Action.class);

	private Step firstStep = null;
	private String rpcName = null;

	public Action(){

	}

	@Override
	public void dereference() {
		if (firstStep != null) {
			firstStep.dereference();
			firstStep = null;
		}
		rpcName = null;
	}
	
	public OperationResult run(OperationData input) {
		if (firstStep == null) {
			return new OperationResult(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		return firstStep.run(input);
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#next", minOccurs = 1, maxOccurs = 1)
	public void setStep(OperationalStep step) {
		this.firstStep = step;
	}	 
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#rpcName", minOccurs = 1, maxOccurs = 1)
	public void setRPCName(String name) {
		this.rpcName = name;
	}
	
	public String getName() {
		return rpcName;
	}
	
}
