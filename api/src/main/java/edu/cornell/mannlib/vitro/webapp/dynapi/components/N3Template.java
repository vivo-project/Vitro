package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class N3Template implements Template{

	private String n3Text;
  	private Parameters requiredParams = new Parameters();

	@Override
	public void dereference() {
		
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasN3Text", minOccurs = 1, maxOccurs = 1)
	public void setN3Text(String n3Text) {
		this.n3Text = n3Text;
	}

	@Override
	public OperationResult run(OperationData input) {
		
		return null;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
	public void addRequiredParameter(Parameter param) {
	  requiredParams.add(param);
	}

	@Override
	public Parameters getRequiredParams() {
		return requiredParams;
	}

  	@Override
  	public Parameters getProvidedParams() {
    return new Parameters();
  }

}
