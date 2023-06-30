package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Set;

public interface Link extends ParameterInfo {
  
  public Set<Link> getNextLinks();
  
  public boolean isRoot();
  
}
