/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individuallist;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;

/**
 * These are the paged results of a query for Individuals.
 * 
 * The criteria for the search are the index of the desired page, the number of
 * results displayed on each page, and an optional initial letter to search
 * against.
 * 
 * By the time this is built, the results have already been partially processed.
 * A list of PageRecord object is included, with values that the GUI can use to
 * create Alphabetical links. Maybe this processing should have been done later.
 * Maybe it should have been left to the GUI.
 */
public class IndividualListResults {
	private static final Log log = LogFactory
			.getLog(IndividualListResults.class);

	public static final IndividualListResults EMPTY = new IndividualListResults();

	private final long totalCount;
	private final List<Individual> entities;
	private final String alpha;
	private final boolean showPages;
	private final List<PageRecord> pages;

	public IndividualListResults(long totalCount, List<Individual> entities,
			String alpha, boolean showPages, List<PageRecord> pages) {
		this.totalCount = totalCount;
		this.entities = entities;
		this.alpha = alpha;
		this.showPages = showPages;
		this.pages = pages;
	}

	private IndividualListResults() {
		this(0L, Collections.<Individual> emptyList(), "", false, Collections
				.<PageRecord> emptyList());
	}

	public long getTotalCount() {
		return totalCount;
	}

	public String getAlpha() {
		return alpha;
	}

	public List<Individual> getEntities() {
		return entities;
	}

	public List<PageRecord> getPages() {
		return pages;
	}

	public boolean isShowPages() {
		return showPages;
	}

	/**
	 * Some controllers put this data directly into the Freemarker body map.
	 * Others wrap it in JSON.
	 */
	public Map<String, Object> asFreemarkerMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("showPages", showPages);
		m.put("pages", pages);
		m.put("alpha", alpha);
		m.put("totalCount", totalCount);
		m.put("entities", entities);
		return m;
	}
}
