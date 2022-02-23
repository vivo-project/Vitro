package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;

public class ConditionalStep implements Step {

	private Condition condition;

  @Override
	public void dereference() {
		// TODO Auto-generated method stub

	}

	@Override
	public OperationResult run(OperationData input) {
		return null;
	}

  @Override
  public Set<Link> getNextLinks() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Parameters getRequiredParams() {
    return condition.getRequiredParams();
  }

  @Override
  public Parameters getProvidedParams() {
    return new Parameters();
  }

  @Override
  public boolean isRoot() {
    return false;
  }
  
  public void setCondition(Condition condition) {
    this.condition = condition;
  }

}
