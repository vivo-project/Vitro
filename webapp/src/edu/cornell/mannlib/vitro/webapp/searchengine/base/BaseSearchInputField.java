/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * A foundation class for implementing SearchInputField.
 */
public class BaseSearchInputField implements SearchInputField {
	private final String name;
	private final List<Object> valueList = new ArrayList<>();

	private float boost = 1.0F;

	public BaseSearchInputField(String name) {
		this.name = name;
	}

	@Override
	public void addValues(Object... values) {
		addValues(Arrays.asList(values));
	}

	@Override
	public void addValues(Collection<? extends Object> values) {
		valueList.addAll(values);
	}

	@Override
	public void setBoost(float boost) {
		this.boost = boost;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public float getBoost() {
		return boost;
	}

	@Override
	public Collection<Object> getValues() {
		return new ArrayList<Object>(valueList);
	}

	@Override
	public Object getFirstValue() {
		if (valueList.isEmpty()) {
			return null;
		} else {
			return valueList.get(0);
		}
	}

}
