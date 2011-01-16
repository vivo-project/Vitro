/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

public interface ApplicationDao {

	public boolean isFlag1Active();
	
	public boolean isFlag2Active();
	
	public List<String> getExternallyLinkedNamespaces();
	
	public boolean isExternallyLinkedNamespace(String namespace);
	
}
