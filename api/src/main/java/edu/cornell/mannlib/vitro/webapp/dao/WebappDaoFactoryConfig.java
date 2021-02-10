/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;

public class WebappDaoFactoryConfig {

	private List<String> preferredLanguages;
	private List<Locale> preferredLocales;
	private String defaultNamespace;
	private Set<String> nonUserNamespaces;
	private boolean isUnderlyingStoreReasoned = false;
	public Map<FullPropertyKey, String> customListViewConfigFileMap;

	public WebappDaoFactoryConfig() {
		preferredLanguages = Arrays.asList("en-US", "en", "EN");
		preferredLocales = LanguageFilteringUtils.languagesToLocales(preferredLanguages);
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

	public List<Locale> getPreferredLocales() {
		return this.preferredLocales;
	}

	public void setPreferredLocales(List<Locale> pl) {
		this.preferredLocales = pl;
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
	    return customListViewConfigFileMap;
	}

    public void setCustomListViewConfigFileMap(
            Map<FullPropertyKey, String> map) {
        this.customListViewConfigFileMap = map;
    }

}
