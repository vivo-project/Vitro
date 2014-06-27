/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;

/**
 * A named field with a name and one or more values. This can be added to a
 * SearchInputDocument and inserted into the search index.
 */
public interface SearchInputField {

	/**
	 * Add values to this field.
	 */
	void addValues(Object... values);

	/**
	 * Add values to this field.
	 */
	void addValues(Collection<? extends Object> values);

	/**
	 * Set the boost level for this field.
	 */
	void setBoost(float boost);

	String getName();

	float getBoost();

	/**
	 * May return an empty collection, but never null.
	 */
	Collection<Object> getValues();

	/**
	 * May return null.
	 */
	Object getFirstValue();

}
