package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class N3Template implements Template{

	private String n3Text;
  	private Parameters requiredParams = new Parameters();

  	//region @Property Setters

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
	public void addRequiredParameter(Parameter param) {
		requiredParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#n3Text", minOccurs = 1, maxOccurs = 1)
	public void setN3Text(String n3Text) {
		this.n3Text = n3Text;
	}

	//endregion

	//region Getters

	@Override
	public Parameters getRequiredParams() {
		return requiredParams;
	}

	@Override
	public Parameters getProvidedParams() {
		return new Parameters();
	}

	//endregion

	@Override
	public OperationResult run(OperationData input) {
		return null;
	}

	@Override
	public void dereference() {

	}
}
