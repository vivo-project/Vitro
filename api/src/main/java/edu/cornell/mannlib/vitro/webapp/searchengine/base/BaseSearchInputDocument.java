/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * A foundation class for implementing SearchInputDocument.
 */
public class BaseSearchInputDocument implements SearchInputDocument {
	private final Map<String, SearchInputField> fieldMap;
	private float documentBoost;

	/**
	 * Default constructor.
	 */
	public BaseSearchInputDocument() {
		this.fieldMap = new HashMap<>();
		this.documentBoost = 1.0F;
	}

	/**
	 * Create a deep copy, down to the value objects.
	 */
	public BaseSearchInputDocument(BaseSearchInputDocument doc) {
		this.documentBoost = doc.documentBoost;
		this.fieldMap = new HashMap<>();
		for (String fieldName : doc.getFieldMap().keySet()) {
			this.fieldMap.put(fieldName,
					new BaseSearchInputField(doc.getField(fieldName)));
		}
	}

	@Override
	public void addField(SearchInputField field) {
		fieldMap.put(field.getName(), field);
	}

	@Override
	public void addField(String name, Object... values) {
		addField(name, 1.0F, Arrays.asList(values));
	}

	@Override
	public void addField(String name, Collection<Object> values) {
		addField(name, 1.0F, values);
	}

	@Override
	public void addField(String name, float boost, Object... values) {
		addField(name, boost, Arrays.asList(values));
	}

	@Override
	public void addField(String name, float boost, Collection<Object> values) {
		SearchInputField field = fieldMap.get(name);
		if (field == null) {
			field = new BaseSearchInputField(name);
			fieldMap.put(name, field);
		}
		field.addValues(values);
		field.setBoost(boost * field.getBoost());
	}

	@Override
	public void setDocumentBoost(float searchBoost) {
		this.documentBoost = searchBoost;
	}

	@Override
	public float getDocumentBoost() {
		return this.documentBoost;
	}

	@Override
	public SearchInputField getField(String name) {
		return fieldMap.get(name);
	}

	@Override
	public Map<String, SearchInputField> getFieldMap() {
		return new HashMap<>(fieldMap);
	}

	/**
	 * Sub-classes should override this if the field requires special
	 * functionality.
	 */
	@Override
	public SearchInputField createField(String name) {
		return new BaseSearchInputField(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(documentBoost);
		result = prime * result + fieldMap.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseSearchInputDocument other = (BaseSearchInputDocument) obj;
		return (Float.floatToIntBits(documentBoost) == Float
				.floatToIntBits(other.documentBoost))
				&& fieldMap.equals(other.fieldMap);
	}

	@Override
	public String toString() {
		return "BaseSearchInputDocument[fieldMap=" + fieldMap
				+ ", documentBoost=" + documentBoost + "]";
	}

}
