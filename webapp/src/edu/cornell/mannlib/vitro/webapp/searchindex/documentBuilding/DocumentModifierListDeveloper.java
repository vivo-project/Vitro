/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;

/**
 * An implementation that accumulates timing figures for each modifier and
 * writes them to the log.
 * 
 * Note that this must be thread-safe.
 */
public class DocumentModifierListDeveloper implements DocumentModifierList {
	private static final Log log = LogFactory
			.getLog(DocumentModifierListDeveloper.class);

	private final List<ModifierTiming> timings;
	private final AtomicInteger count = new AtomicInteger();

	public DocumentModifierListDeveloper(
			Collection<? extends DocumentModifier> modifiers) {
		List<ModifierTiming> list = new ArrayList<>();
		for (DocumentModifier modifier : modifiers) {
			list.add(new ModifierTiming(modifier));
		}
		this.timings = Collections.unmodifiableList(list);
	}

	@Override
	public void startIndexing() {
		// Nothing to do.
	}

	/**
	 * Each time a modifier is run, accumulate the timings for it.
	 */
	@Override
	public void modifyDocument(Individual ind, SearchInputDocument doc) {
		count.incrementAndGet();

		for (ModifierTiming timing : timings) {
			long startTime = System.currentTimeMillis();
			timing.getModifier().modifyDocument(ind, doc);
			timing.addElapsedTime(System.currentTimeMillis() - startTime);
		}
	}

	/**
	 * Write the timings to the log.
	 */
	@Override
	public void stopIndexing() {
		String message = String.format(
				"Timings for %d modifiers after %d calls:", timings.size(),
				count.get());
		for (ModifierTiming timing : timings) {
			int totalMillis = timing.getTotal();
			float totalSeconds = totalMillis / 1000.0F;
			int average = (count.get() == 0) ? 0 : totalMillis / count.get();
			message += String
					.format("\n   count: %7d, total: %9.3fsec, average: %4dms-- %1.200s",
							count.get(), totalSeconds, average,
							timing.getModifier());
		}
		log.info(message);
	}

	private static class ModifierTiming {
		private final DocumentModifier modifier;
		private final AtomicLong totalElapsedMillis = new AtomicLong();

		public ModifierTiming(DocumentModifier modifier) {
			this.modifier = modifier;
		}

		public DocumentModifier getModifier() {
			return modifier;
		}

		public int getTotal() {
			return (int) totalElapsedMillis.get();
		}

		public void addElapsedTime(long elapsed) {
			totalElapsedMillis.addAndGet(elapsed);
		}

	}

}
