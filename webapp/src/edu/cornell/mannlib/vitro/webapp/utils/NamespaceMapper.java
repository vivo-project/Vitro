/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.List;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;

public interface NamespaceMapper extends ModelChangedListener {

	/**
	 * Returns the current abbreviation to use for a given namespace,
	 * or null if undefined.
	 * @param namespace
	 * @return
	 */
	public String getPrefixForNamespace(String namespace);
	
	/**
	 * Returns a list of abbreviations that have been used to
	 * represent a given namespace.
	 * @param namespace
	 * @return
	 */
	public List<String> getPrefixesForNamespace(String namespace);
	
	/**
	 * Returns the full namespace URI represented by a given
	 * abbreviation, or null if not found.
	 * @param namespace
	 * @return
	 */
	public String getNamespaceForPrefix(String prefix);
	
}
