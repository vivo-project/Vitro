/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * An implementation that accumulates timing figures for each excluder and
 * writes them to the log.
 * 
 * Note that this must be thread-safe.
 */
public class SearchIndexExcluderListDeveloper implements
		SearchIndexExcluderList {
	private static final Log log = LogFactory
			.getLog(SearchIndexExcluderListDeveloper.class);

	private final List<ExcluderTiming> timings;
	private final AtomicInteger count = new AtomicInteger();

	public SearchIndexExcluderListDeveloper(
			Collection<? extends SearchIndexExcluder> excluders) {

		List<ExcluderTiming> list = new ArrayList<>();
		for (SearchIndexExcluder excluder : excluders) {
			list.add(new ExcluderTiming(excluder));
		}
		this.timings = Collections.unmodifiableList(list);
	}

	@Override
	public void startIndexing() {
		// Nothing to do.
	}

	/**
	 * Each time a excluder is run, accumulate the timings for it. Note that
	 * those at the top of the list will run more times than those at the
	 * bottom.
	 */
	@Override
	public boolean isExcluded(Individual ind) {
		count.incrementAndGet();

		for (ExcluderTiming timing : timings) {
			long startTime = System.currentTimeMillis();

			String message = timing.getExcluder().checkForExclusion(ind);

			timing.incrementCount();
			timing.addElapsedTime(System.currentTimeMillis() - startTime);

			if (message != SearchIndexExcluder.DONT_EXCLUDE) {
				log.debug("Excluded " + ind + " because " + message);
				return true;
			}
		}
		return false;
	}

	/**
	 * Write the timings to the log.
	 */
	@Override
	public void stopIndexing() {
		String message = String.format(
				"Timings for %d excluders after %d calls:", timings.size(),
				count.get());
		for (ExcluderTiming timing : timings) {
			int thisCount = timing.getCount();
			int totalMillis = timing.getTotal();
			float totalSeconds = totalMillis / 1000.0F;
			int average = (thisCount == 0) ? 0 : totalMillis / thisCount;
			message += String
					.format("\n   count: %7d, total: %9.3fsec, average: %4dms-- %1.200s",
							thisCount, totalSeconds, average,
							timing.getExcluder());
		}
		log.info(message);
	}

	private static class ExcluderTiming {
		private final SearchIndexExcluder excluder;
		private final AtomicInteger invocationCount = new AtomicInteger();
		private final AtomicLong totalElapsedMillis = new AtomicLong();

		public ExcluderTiming(SearchIndexExcluder excluder) {
			this.excluder = excluder;
		}

		public SearchIndexExcluder getExcluder() {
			return excluder;
		}

		public void incrementCount() {
			invocationCount.incrementAndGet();
		}

		public int getTotal() {
			return (int) totalElapsedMillis.get();
		}

		public int getCount() {
			return invocationCount.get();
		}

		public void addElapsedTime(long elapsed) {
			totalElapsedMillis.addAndGet(elapsed);
		}

	}
}
