/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashSet;
import java.util.Set;

public class WebappDaoFactoryConfig {
	
	private String[] preferredLanguages;
	private String defaultNamespace;
	private Set<String> nonUserNamespaces;
	
	public WebappDaoFactoryConfig() {
        preferredLanguages = new String[3];
        preferredLanguages[0] = "en-US";
        preferredLanguages[1] = "en";
        preferredLanguages[2] = "EN";
		defaultNamespace = "http://vitro.mannlib.cornell.edu/ns/default#";
		nonUserNamespaces = new HashSet<String>();
		nonUserNamespaces.add(VitroVocabulary.vitroURI);
	}
	
	public String[] getPreferredLanguages() {
		return this.preferredLanguages;
	}
	
	public void setPreferredLanguages(String[] pl) {
		this.preferredLanguages = pl;
	}
	
	public String getDefaultNamespace() {
		return defaultNamespace;
	}
	
	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}
	
	public Set<String> getNonUserNamespaces() {
		return this.nonUserNamespaces;
	}
	
	public void setNonUserNamespaces(Set<String> nonUserNamespaces) {
		this.nonUserNamespaces = nonUserNamespaces;
	}
	
}
