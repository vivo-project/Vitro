/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;

/**
 * A foundation class for implementing SearchFacetField.
 */
public class BaseSearchFacetField implements SearchFacetField {
	private final String name;
	private final List<Count> values;

	public BaseSearchFacetField(String name, List<? extends Count> values) {
		this.name = name;
		this.values = new ArrayList<>(values);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Count> getValues() {
		return values;
	}

	@Override
	public String toString() {
		return "BaseSearchFacetField[name=" + name + ", values=" + values + "]";
	}

	/**
	 * A foundation class for implementing SearchFacetField.Count.
	 */
	public static class BaseCount implements SearchFacetField.Count {
		private final String name;
		private final long count;

		public BaseCount(String name, long count) {
			this.name = name;
			this.count = count;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public long getCount() {
			return count;
		}

		@Override
		public String toString() {
			return "BaseCount[name=" + name + ", count=" + count + "]";
		}

	}
}
