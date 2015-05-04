/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;

/**
 * An ordered list of DocumentModifier objects, in a handy package.
 * 
 * Implementations should make a protective copy of the list of
 * DocumentModifiers. Implementations must be thread-safe.
 * 
 * The life-cycle is:
 * 
 * <pre>
 * startIndexing(), 
 * 0 or more modifyDocument() by multiple threads,
 * stopIndexing().
 * </pre>
 */
public interface DocumentModifierList {

	/**
	 * Do any required setup on the individual modifiers.
	 */
	void startIndexing();

	/**
	 * Do any required teardown on the individual modifiers.
	 */
	void stopIndexing();

	/**
	 * Exercise the list of modifiers, making changes to this document based on
	 * this individual.
	 */
	void modifyDocument(Individual ind, SearchInputDocument doc);

}
