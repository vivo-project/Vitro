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
	private final List<Object> valueList;
	private float boost;

	public BaseSearchInputField(String name) {
		this.name = name;
		this.valueList = new ArrayList<>();
		this.boost = 1.0F;
	}

	/**
	 * Create a copy of the field.
	 */
	public BaseSearchInputField(SearchInputField field) {
		this.name = field.getName();
		this.valueList = new ArrayList<>(field.getValues());
		this.boost = field.getBoost();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(boost);
		result = prime * result + name.hashCode();
		result = prime * result + valueList.hashCode();
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
		BaseSearchInputField other = (BaseSearchInputField) obj;
		return (Float.floatToIntBits(boost) == Float
				.floatToIntBits(other.boost))
				&& name.equals(other.name)
				&& equalsIgnoreOrder(valueList, other.valueList);
	}

	/**
	 * Can't just compare the value lists, because they are considered to be
	 * equivalent even if the order is differemt.
	 * 
	 * Can't just convert them to Sets, because either list may contain the same
	 * object multiple times.
	 * 
	 * Remove the members of list1 from list2, one at a time. If any member is
	 * not found, the lists are not equivalent.
	 */
	private boolean equalsIgnoreOrder(List<Object> list1, List<Object> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}
		List<Object> remaining = new ArrayList<>(list2);
		for (Object value : list1) {
			if (!remaining.remove(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "BaseSearchInputField[name=" + name + ", valueList=" + valueList
				+ ", boost=" + boost + "]";
	}

}
