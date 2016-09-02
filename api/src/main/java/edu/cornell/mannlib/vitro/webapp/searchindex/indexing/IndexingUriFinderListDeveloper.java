/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Statement;

/**
 * An implementation that accumulates timing figures for each finder and writes
 * them to the log.
 * 
 * Note that this must be thread-safe.
 */
public class IndexingUriFinderListDeveloper implements IndexingUriFinderList {
	private static final Log log = LogFactory
			.getLog(IndexingUriFinderListDeveloper.class);

	private final List<FinderTiming> timings;
	private final AtomicInteger count = new AtomicInteger();

	public IndexingUriFinderListDeveloper(
			Collection<? extends IndexingUriFinder> finders) {
		List<FinderTiming> list = new ArrayList<>();
		for (IndexingUriFinder finder : finders) {
			list.add(new FinderTiming(finder));
		}
		this.timings = Collections.unmodifiableList(list);
	}

	@Override
	public void startIndexing() {
		for (FinderTiming timing : timings) {
			timing.getFinder().startIndexing();
		}
	}

	/**
	 * Each time a finder is run, accumulate the timings for it.
	 */
	@Override
	public Set<String> findAdditionalUris(Statement stmt) {
		count.incrementAndGet();

		Set<String> uris = new HashSet<>();
		for (FinderTiming timing : timings) {
			long startTime = System.currentTimeMillis();
			uris.addAll(timing.getFinder().findAdditionalURIsToIndex(stmt));
			timing.addElapsedTime(System.currentTimeMillis() - startTime);
		}
		return uris;
	}

	/**
	 * Write the timings to the log.
	 */
	@Override
	public void stopIndexing() {
		for (FinderTiming timing : timings) {
			timing.getFinder().endIndexing();
		}

		String message = String.format(
				"Timings for %d URI finders after %d calls:", timings.size(),
				count.get());
		for (FinderTiming timing : timings) {
			int totalMillis = timing.getTotal();
			float totalSeconds = totalMillis / 1000.0F;
			int average = (count.get() == 0) ? 0 : totalMillis / count.get();
			message += String
					.format("\n   count: %7d, total: %9.3fsec, average: %4dms-- %1.200s",
							count.get(), totalSeconds, average,
							timing.getFinder());
		}
		log.info(message);

	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class FinderTiming {
		private final IndexingUriFinder finder;
		private final AtomicLong totalElapsedMillis = new AtomicLong();

		public FinderTiming(IndexingUriFinder finder) {
			this.finder = finder;
		}

		public IndexingUriFinder getFinder() {
			return finder;
		}

		public int getTotal() {
			return (int) totalElapsedMillis.get();
		}

		public void addElapsedTime(long elapsed) {
			totalElapsedMillis.addAndGet(elapsed);
		}

	}

}
