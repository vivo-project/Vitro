/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * When a change is heard, wait for an interval to see if more changes come in.
 * When changes stop coming in for a specified interval, send what has
 * accumulated.
 */
public class IndexingChangeListener implements ChangeListener {
	private static final Log log = LogFactory
			.getLog(IndexingChangeListener.class);

	private final SearchIndexer searchIndexer;
	private final Ticker ticker;

	/** All access to the list must be synchronized. */
	private final List<Statement> changes;

	public IndexingChangeListener(SearchIndexer searchIndexer) {
		this.searchIndexer = searchIndexer;
		this.ticker = new Ticker();
		this.changes = new ArrayList<>();
	}

	private synchronized void noteChange(Statement stmt) {
		changes.add(stmt);
		ticker.start();
	}

	private synchronized void respondToTicker() {
		searchIndexer.scheduleUpdatesForStatements(changes);
		changes.clear();
	}

	public void shutdown() {
		ticker.shutdown();
	}

	@Override
	public void addedStatement(String serializedTriple, String graphURI) {
		noteChange(parseTriple(serializedTriple));
	}

	@Override
	public void removedStatement(String serializedTriple, String graphURI) {
		noteChange(parseTriple(serializedTriple));
	}

	/**
	 * We only care about events that signal the end of an edit operation.
	 */
	@Override
	public void notifyEvent(String graphURI, Object event) {
		if ((event instanceof EditEvent)) {
			EditEvent editEvent = (EditEvent) event;
			if (!editEvent.getBegin()) { // editEvent is the end of an edit
				log.debug("Doing search index build at end of EditEvent");
				ticker.start();
			}
		} else {
			log.debug("ignoring event " + event.getClass().getName() + " "
					+ event);
		}
	}

	// TODO avoid overhead of Model.
	// TODO avoid duplication with JenaChangeListener
	private Statement parseTriple(String serializedTriple) {
		try {
			Model m = ModelFactory.createDefaultModel();
			m.read(new ByteArrayInputStream(serializedTriple.getBytes("UTF-8")),
					null, "N3");
			StmtIterator sit = m.listStatements();
			if (!sit.hasNext()) {
				throw new RuntimeException("no triple parsed from change event");
			} else {
				Statement s = sit.nextStatement();
				if (sit.hasNext()) {
					log.warn("More than one triple parsed from change event");
				}
				return s;
			}
		} catch (RuntimeException riot) {
			log.error("Failed to parse triple " + serializedTriple, riot);
			throw riot;
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(uee);
		}
	}

	// ----------------------------------------------------------------------
	// helper classes
	// ----------------------------------------------------------------------

	/**
	 * The ticker will ask for a response after two ticks, unless it is started
	 * again before the second one.
	 * 
	 * <pre>
	 * On a call to start():
	 *    Start the timer unless it is already running.
	 *    Reset the hasOneTick flag.
	 *    
	 * When the timer expires:
	 *    If the timer hasOneTick, we're done: call for a response.
	 *    Otherwise, record that it hasOneTick, and keep the timer running.
	 * </pre>
	 * 
	 * All methods are synchronized on the enclosing IndexingChangeListener.
	 */
	private class Ticker {
		private final ScheduledExecutorService queue;
		private volatile boolean running;
		private volatile boolean hasOneTick;

		public Ticker() {
			this.queue = Executors.newScheduledThreadPool(1,
					new VitroBackgroundThread.Factory(
							"IndexingChangeListener_Ticker"));
		}

		public void shutdown() {
			synchronized (IndexingChangeListener.this) {
				this.queue.shutdown();
			}
		}

		public void start() {
			synchronized (IndexingChangeListener.this) {
				if (!running) {
					startTicker();
				}
				hasOneTick = false;
			}
		}

		private void startTicker() {
			if (queue.isShutdown()) {
				log.warn("Attempt to start ticker after shutdown request.");
			} else {
				queue.schedule(new TickerResponse(), 1, TimeUnit.SECONDS);
				running = true;
			}
		}

		private class TickerResponse implements Runnable {
			@Override
			public void run() {
				synchronized (IndexingChangeListener.this) {
					running = false;
					if (hasOneTick) {
						respondToTicker();
						hasOneTick = false;
					} else {
						startTicker();
						hasOneTick = true;
					}
				}
			}
		}
	}
}
