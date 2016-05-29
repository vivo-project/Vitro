/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;

public class WebappDaoFactoryConfig {
	
	private List<String> preferredLanguages;
	private String defaultNamespace;
	private Set<String> nonUserNamespaces;
	private boolean isUnderlyingStoreReasoned = false;
	public Map<FullPropertyKey, String> customListViewConfigFileMap;
	
	public WebappDaoFactoryConfig() {
	    preferredLanguages = Arrays.asList("en-US", "en", "EN");
		defaultNamespace = "http://vitro.mannlib.cornell.edu/ns/default#";
		nonUserNamespaces = new HashSet<String>();
		nonUserNamespaces.add(VitroVocabulary.vitroURI);
	}
	
	public List<String> getPreferredLanguages() {
		return this.preferredLanguages;
	}
	
	public void setPreferredLanguages(List<String> pl) {
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
	
	public void setUnderlyingStoreReasoned(boolean isReasoned) {
	    this.isUnderlyingStoreReasoned = isReasoned;
	}
	
	public boolean isUnderlyingStoreReasoned() {
	    return this.isUnderlyingStoreReasoned;
	}
	
	public Map<FullPropertyKey, String> getCustomListViewConfigFileMap() {
	    return this.getCustomListViewConfigFileMap();    
	}
	
    public void setCustomListViewConfigFileMap(
            Map<FullPropertyKey, String> map) {
        this.customListViewConfigFileMap = map;    
    }
	
}
