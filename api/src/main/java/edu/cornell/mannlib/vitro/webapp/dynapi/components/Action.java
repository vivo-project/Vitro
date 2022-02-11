package edu.cornell.mannlib.vitro.webapp.dynapi.components;


import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Action implements RunnableComponent{

 	private static final Log log = LogFactory.getLog(Action.class);

	private Step firstStep = null;
	private RPC rpc;

	@Override
	public void dereference() {
		if (firstStep != null) {
			firstStep.dereference();
			firstStep = null;
		}
		rpc.dereference();
		rpc = null;
	}
	
	public OperationResult run(OperationData input) {
		if (firstStep == null) {
			return new OperationResult(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		return firstStep.run(input);
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#firstStep", minOccurs = 1, maxOccurs = 1)
	public void setStep(OperationalStep step) {
		this.firstStep = step;
	}	 
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#assignedRPC", minOccurs = 1, maxOccurs = 1)
	public void setRPC(RPC rpc) {
		this.rpc = rpc;
	}
	
	public String getName() {
		return rpc.getName();
	}

	public boolean isValid() {
		return true;
	}
}
