/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;

/**
 * A foundation class for implementing SearchResultDocument.
 */
public class BaseSearchResultDocument implements SearchResultDocument {
	private final String uniqueId;
	private final Map<String, Collection<Object>> fieldValuesMap;

	public BaseSearchResultDocument(String uniqueId, Map<String, Collection<Object>> fieldValuesMap) {
		this.uniqueId = uniqueId;
		
		Map<String, Collection<Object>> map = new HashMap<>();
		for (String name : fieldValuesMap.keySet()) {
			map.put(name, Collections.unmodifiableList(new ArrayList<>(
					fieldValuesMap.get(name))));
		}
		this.fieldValuesMap = Collections.unmodifiableMap(map);
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public Collection<String> getFieldNames() {
		return fieldValuesMap.keySet();
	}

	@Override
	public Object getFirstValue(String name) {
		Collection<Object> values = fieldValuesMap.get(name);
		if (values == null || values.isEmpty()) {
			return null;
		}
		return values.iterator().next();
	}

	@Override
	public String getStringValue(String name) {
		Object o = getFirstValue(name);
		if (o == null) {
			return null;
		} else {
			return String.valueOf(o);
		}
	}

	@Override
	public Collection<Object> getFieldValues(String name) {
		Collection<Object> values = fieldValuesMap.get(name);
		if (values == null) {
			return Collections.emptyList();
		} else {
			return values;
		}
	}

	@Override
	public Map<String, Collection<Object>> getFieldValuesMap() {
		return fieldValuesMap;
	}

	@Override
	public String toString() {
		return "BaseSearchResultDocument [uniqueId=" + uniqueId
				+ ", fieldValuesMap=" + fieldValuesMap + "]";
	}

}
