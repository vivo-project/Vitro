/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;
import java.util.Map;

/**
 * A collection of fields and values that will be used to build a record in the
 * search index.
 */
public interface SearchInputDocument {
	/**
	 * Create a field that can be populated and added to the document.
	 */
	SearchInputField createField(String name);

	/**
	 * Put the field into the document. If a field with this name already exists
	 * in the document, it will be replaced.
	 */
	void addField(SearchInputField field);

	/**
	 * Create a field with this name and values, and put it into the document.
	 *
	 * If a field with this name already exists in the document, these values
	 * will be added to the existing values on the field.
	 */
	void addField(String name, Object... values);

	/**
	 * Create a field with this name and values, and put it into the document.
	 * 
	 * If a field with this name already exists in the document, these values
	 * will be added to the existing values on the field.
	 */
	void addField(String name, Collection<Object> values);

	/**
	 * Create a field with this name, boost level and values, and put it into
	 * the document. 
	 * 
	 * If a field with this name already exists in the document,
	 * these values will be added to the existing values on the field, and the
	 * existing boost will be multipled by this boost.
	 */
	void addField(String name, float boost, Object... values);

	/**
	 * Create a field with this name, boost level and values, and put it into
	 * the document. 
	 * 
	 * If a field with this name already exists in the document,
	 * these values will be added to the existing values on the field, and the
	 * existing boost will be multipled by this boost.
	 */
	void addField(String name, float boost, Collection<Object> values);

	/**
	 * Set a boost level for the document as a whole.
	 */
	void setDocumentBoost(float searchBoost);

	float getDocumentBoost();

	/**
	 * May return null.
	 */
	SearchInputField getField(String name);

	/**
	 * May return an empty map, but never null.
	 */
	Map<String, SearchInputField> getFieldMap();
}
