package edu.cornell.mannlib.vitro.webapp.dynapi.components;

public interface Operation extends RunnableComponent {

	public void addRequiredParameter(Parameter param);

	public Parameters getRequiredParams();

}
