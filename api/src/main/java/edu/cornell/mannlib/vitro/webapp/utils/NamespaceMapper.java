/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.List;

import org.apache.jena.rdf.model.ModelChangedListener;

public interface NamespaceMapper extends ModelChangedListener {

	/**
	 * Returns the current abbreviation to use for a given namespace,
	 * or null if undefined.
	 * @param namespace Namespace
	 */
	public String getPrefixForNamespace(String namespace);

	/**
	 * Returns a list of abbreviations that have been used to
	 * represent a given namespace.
	 * @param namespace Namespace
	 */
	public List<String> getPrefixesForNamespace(String namespace);

	/**
	 * Returns the full namespace URI represented by a given
	 * abbreviation, or null if not found.
	 * @param prefix Prefix
	 */
	public String getNamespaceForPrefix(String prefix);

}
