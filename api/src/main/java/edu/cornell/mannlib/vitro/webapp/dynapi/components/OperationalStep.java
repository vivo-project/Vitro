package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class OperationalStep implements Step{

 	private static final Log log = LogFactory.getLog(OperationalStep.class);

 	private Operation operation;
 	private boolean optional;
	private Step nextStep;

 	
	public OperationalStep() {
		optional = false;
		operation = null;
	}

	@Override
	public void dereference() {
		if (operation != null) {
			operation.dereference();
			operation = null;
		}
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#next", minOccurs = 0, maxOccurs = 1)
	public void setNextStep(OperationalStep step) {
		this.nextStep = step;
	}	 
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasOperation", minOccurs = 0, maxOccurs = 1)
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isOptional", minOccurs = 0, maxOccurs = 1)
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	public OperationResult run(OperationData data) {
		OperationResult result = OperationResult.notImplemented();
		log.debug("Processing in STEP");
		log.debug("Execution step is optional? " + optional);
		if (operation != null) {
			log.debug("Operation not null");
			result = operation.run(data);
			if (!optional && result.hasError()) {
				return result;
			}
		}
		if (nextStep != null) {
			return nextStep.run(data);
		}
		return result;
	}
}
