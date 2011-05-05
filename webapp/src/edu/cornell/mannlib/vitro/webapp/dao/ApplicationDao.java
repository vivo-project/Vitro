/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Set;

public interface ApplicationDao {

	public boolean isFlag1Active();
	
	public boolean isFlag2Active();
	
	public List<String> getExternallyLinkedNamespaces();
	
	public List<String> getExternallyLinkedNamespaces(boolean clearCache);

	public boolean isExternallyLinkedNamespace(String namespace);
	   
	public Set<String> getRdfaNamespaces();
	
	public Set<String> getRdfaNamespaces(boolean clearCache);
	
}
