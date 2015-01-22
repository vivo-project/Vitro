/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.Counts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.Counts.Type;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.RebuildCounts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.StatementCounts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.UriCounts;

/**
 * This listener keeps a list of the last several SearchIndexer events, and will
 * format them for display in a Freemarker template.
 */
public class IndexHistory implements SearchIndexer.Listener {
	private static final Log log = LogFactory.getLog(IndexHistory.class);

	private final static int MAX_EVENTS = 20;

	private final Deque<Event> events = new LinkedList<>();

	@Override
	public void receiveSearchIndexerEvent(Event event) {
		if (log.isInfoEnabled()) {
			log.info(event);
		}
		synchronized (events) {
			events.addFirst(event);
			while (events.size() > MAX_EVENTS) {
				events.removeLast();
			}
		}
	}

	public List<Map<String, Object>> toMaps() {
		synchronized (events) {
			List<Map<String, Object>> list = new ArrayList<>();
			for (Event event : events) {
				list.add(toMap(event));
			}
			return list;
		}
	}

	private Map<String, Object> toMap(Event event) {
		SearchIndexerStatus status = event.getStatus();
		Counts counts = status.getCounts();
		Type countsType = counts.getType();

		Map<String, Object> map = new HashMap<>();
		map.put("event", event.getType());
		map.put("statusType", status.getState());
		map.put("since", status.getSince());
		map.put("countsType", countsType);

		switch (countsType) {
		case URI_COUNTS:
			addCounts(counts.asUriCounts(), map);
			break;
		case STATEMENT_COUNTS:
			addCounts(counts.asStatementCounts(), map);
			break;
		case REBUILD_COUNTS:
			addCounts(counts.asRebuildCounts(), map);
			break;
		default: // NO_COUNTS
			break;
		}

		return map;
	}

	private void addCounts(UriCounts counts, Map<String, Object> map) {
		map.put("excluded", counts.getExcluded());
		map.put("updated", counts.getUpdated());
		map.put("deleted", counts.getDeleted());
		map.put("remaining", counts.getRemaining());
		map.put("total", counts.getTotal());
	}

	private void addCounts(StatementCounts counts, Map<String, Object> map) {
		map.put("processed", counts.getProcessed());
		map.put("remaining", counts.getRemaining());
		map.put("total", counts.getTotal());
	}

	private void addCounts(RebuildCounts counts, Map<String, Object> map) {
		map.put("documentsBefore", counts.getDocumentsBefore());
		map.put("documentsAfter", counts.getDocumentsAfter());
	}

}
