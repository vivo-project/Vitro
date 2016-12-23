/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.REBUILD_REQUESTED;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.START_REBUILD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * When a change is heard, wait for an interval to see if more changes come in.
 * When changes stop coming in for a specified interval, send what has
 * accumulated.
 * 
 * When the SearchIndexer pauses, stop sending changes until the SearchIndexer
 * unpauses.
 * 
 * If the SearchIndexer begins a rebuild, discard any changes that we had
 * accumulated. They will be accomplished by the rebuild.
 * 
 * -----------------------
 * 
 * When a changed statement is received, it should not be added to the list of
 * pending changes. The elements of the statement hold references to the model
 * in which they were created, as well as to other structures.
 * 
 * Thus, an action that produces many changes to the models could become
 * unscalable.
 * 
 * To avoid this, we use the ResourceFactory to create a "sanitized" statement
 * which is semantically equivalent to the original, and add that to the list
 * instead. The original statement is released.
 */
public class IndexingChangeListener extends StatementListener 
        implements ModelChangedListener, SearchIndexer.Listener {
	private static final Log log = LogFactory
			.getLog(IndexingChangeListener.class);

	private final SearchIndexer searchIndexer;
	private final Ticker ticker;
	private volatile boolean rebuildScheduled;
    private final Model defaultModel;

	/** All access to the list must be synchronized. */
	private final List<Statement> changes;

	public IndexingChangeListener(SearchIndexer searchIndexer) {
		this.searchIndexer = searchIndexer;
		this.ticker = new Ticker();
        this.defaultModel = ModelFactory.createDefaultModel();
		this.changes = new ArrayList<>();

		searchIndexer.addListener(this);
	}

	private synchronized void noteChange(Statement stmt) {
        changes.add(stmt); 
		ticker.start();
	}

	@Override
	public void receiveSearchIndexerEvent(Event event) {
		if (event.getType() == REBUILD_REQUESTED) {
			rebuildScheduled = true;
		} else if (event.getType() == START_REBUILD) {
			rebuildScheduled = false;
			discardChanges();
		}
	}

	private synchronized void respondToTicker() {
		if (!changes.isEmpty()) {
			searchIndexer.scheduleUpdatesForStatements(changes);
			changes.clear();
		}
	}

	private synchronized void discardChanges() {
		changes.clear();
	}

	public void shutdown() {
		ticker.shutdown();
	}

	@Override
	public void addedStatement(Statement stmt) {
		if (!rebuildScheduled) {
			noteChange(stmt);
		}
	}

	@Override
	public void removedStatement(Statement stmt) {
		if (!rebuildScheduled) {
			noteChange(stmt);
		}
	}

	/**
	 * We only care about events that signal the end of an edit operation.
	 */
	@Override
	public void notifyEvent(Model model, Object event) {
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

//	private Statement parseTriple(String serializedTriple) {
//        try {
//            // Use RiotReader to parse a Triple
//            // NB A Triple can be serialized correctly with: FmtUtils.stringForTriple(triple, PrefixMapping.Factory.create()) + " .";'
//        	Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(serializedTriple);
//			Iterator<Triple> it = RiotReader.createParserNTriples(tokenizer, null);
//
//            if (it.hasNext()) {
//                Triple triple = it.next();
//
//                if (it.hasNext()) {
//                    log.warn("More than one triple parsed from change event: '" + serializedTriple + "'");
//                }
//
//                // Use the retained defaultModel instance to convert the Triple to a Statement
//                // This does not add the Statement to the Model, so the Statement can be disposed when unused
//                // And whilst the Model is attached to the Statement, using a single instance means only one Model
//                // is created and attached to all of the Statements created by this instance
//                return defaultModel.asStatement(triple);
//            } else {
//                throw new RuntimeException("no triple parsed from change event: '" + serializedTriple + "'");
//            }
//        } catch (RuntimeException riot) {
//            log.error("Failed to parse triple " + serializedTriple, riot);
//            throw riot;
//        }
//	}

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
