package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashSet;
import java.util.Set;

public class VitroModelProperties {

	public static final int RDF_ABOX_ONLY = 50;
	public static final int RDFS = 100;
	public static final int OWL_FULL = 200;
	public static final int OWL_DL = 202;
	public static final int OWL_LITE = 204;
	
	private int languageProfile;
	private String[] preferredLanguages;
	private String defaultNamespace;
	private Set<String> nonUserNamespaces;
	
	public static boolean isOWL(int languageProfile) {
		return ((OWL_FULL <= languageProfile && OWL_LITE >= languageProfile));
	}
	
	public static boolean isRDFS(int languageProfile) {
		return (languageProfile==100);
	}
	
	public VitroModelProperties() {
		languageProfile = 200;
		preferredLanguages = new String[1];
		preferredLanguages[0] = null;
		defaultNamespace = "http://vitro.mannlib.cornell.edu/ns/default#";
		nonUserNamespaces = new HashSet<String>();
		nonUserNamespaces.add(VitroVocabulary.vitroURI);
	}
	
	public int getLanguageProfile() {
		return this.languageProfile;
	}
	
	public void setLanguageProfile(int languageProfile) {
		this.languageProfile = languageProfile;
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
