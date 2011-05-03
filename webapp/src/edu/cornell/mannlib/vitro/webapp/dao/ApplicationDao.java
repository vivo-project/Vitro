/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;

public interface ApplicationDao {

    public ApplicationBean getApplicationBean();
    
    public void updateApplicationBean(ApplicationBean appBean);
	
	public List<String> getExternallyLinkedNamespaces();
	
	public boolean isExternallyLinkedNamespace(String namespace);
	
}
