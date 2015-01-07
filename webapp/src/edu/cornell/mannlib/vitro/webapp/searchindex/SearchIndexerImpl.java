/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.REBUILD_REQUESTED;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.*;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.STOP_PROCESSING_STATEMENTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexingEventListener;
import edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.searchindex.exclusions.SearchIndexExcluder;
import edu.cornell.mannlib.vitro.webapp.searchindex.indexing.IndexingUriFinder;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevelStamp;

/**
 * TODO A silly implementation that just wraps the old IndexBuilder.
 */
public class SearchIndexerImpl implements SearchIndexer {
	private static final Log log = LogFactory.getLog(SearchIndexerImpl.class);

	private final ListenerList listeners = new ListenerList();

	private ServletContext ctx;
	private Set<SearchIndexExcluder> excluders;
	private Set<DocumentModifier> modifiers;
	private Set<IndexingUriFinder> uriFinders;

	// TODO
	private IndexBuilder indexBuilder;

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		try {
			this.ctx = application.getServletContext();
			loadConfiguration();
			ss.info("Configured SearchIndexer: excluders=" + excluders
					+ ", modifiers=" + modifiers + ", uriFinders=" + uriFinders);

			{ // >>>>>>> TODO
				this.indexBuilder = (IndexBuilder) ctx
						.getAttribute(IndexBuilder.class.getName());

				this.indexBuilder.addIndexBuilderListener(new BridgeListener());
			}

			createAndFire(STARTUP);
		} catch (Exception e) {
			ss.fatal("Failed to configure the SearchIndexer", e);
		}
	}

	private void createAndFire(Event.Type type) {
		listeners.fireEvent(new Event(type, getStatus()));
	}

	private void loadConfiguration() throws ConfigurationBeanLoaderException {
		ConfigurationBeanLoader beanLoader = new ConfigurationBeanLoader(
				ModelAccess.on(ctx).getOntModel(DISPLAY), ctx);
		excluders = beanLoader.loadAll(SearchIndexExcluder.class);
		modifiers = beanLoader.loadAll(DocumentModifier.class);
		uriFinders = beanLoader.loadAll(IndexingUriFinder.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer#
	 * scheduleUpdatesForUris(java.util.Collection)
	 */
	@Override
	public void scheduleUpdatesForUris(Collection<String> uris) {
		// TODO
		for (String uri : uris) {
			indexBuilder.addToChanged(uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer#
	 * rebuildIndex()
	 */
	@Override
	public void rebuildIndex() {
		// TODO
		indexBuilder.doIndexRebuild();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer#
	 * pause()
	 */
	@Override
	public void pause() {
		// TODO
		indexBuilder.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer#
	 * unpause()
	 */
	@Override
	public void unpause() {
		// TODO
		indexBuilder.unpause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer#
	 * getStatus()
	 */
	@Override
	public SearchIndexerStatus getStatus() {
		// TODO
		WorkLevelStamp workLevel = indexBuilder.getWorkLevel();
		WorkLevel level = workLevel.getLevel();
		Date since = workLevel.getSince();
		if (level == WorkLevel.IDLE) {
			return new SearchIndexerStatus(State.IDLE, since,
					new SearchIndexerStatus.NoCounts());
		} else {
			return new SearchIndexerStatus(State.PROCESSING_URIS, since,
					new SearchIndexerStatus.UriCounts(1, 2, 3, 6));
		}
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cornell.mannlib.vitro.webapp.modules.Application.Component#shutdown
	 * (edu.cornell.mannlib.vitro.webapp.modules.Application)
	 */
	@Override
	public void shutdown(Application application) {
		// TODO
	}

	/**
	 * A simple thread-safe list of event listeners.
	 */
	private static class ListenerList {
		private final List<Listener> list;

		public ListenerList() {
			list = Collections.synchronizedList(new ArrayList<Listener>());
		}

		public void add(Listener l) {
			list.add(l);
		}

		public void remove(Listener l) {
			list.remove(l);
		}

		public void fireEvent(Event e) {
			synchronized (list) {
				for (Listener l : list) {
					l.receiveSearchIndexerEvent(e);
				}
			}
		}
	}

	private class BridgeListener implements IndexingEventListener {
		@Override
		public void notifyOfIndexingEvent(EventTypes ie) {
			switch (ie) {
			case START_UPDATE:
				createAndFire(START_PROCESSING_STATEMENTS);
				break;
			case FINISHED_UPDATE:
				createAndFire(STOP_PROCESSING_STATEMENTS);
				break;
			case START_FULL_REBUILD:
				createAndFire(REBUILD_REQUESTED);
				createAndFire(START_PROCESSING_STATEMENTS);
				break;
			default: // FINISH_FULL_REBUILD
				createAndFire(STOP_PROCESSING_STATEMENTS);
				break;
			}
		}
	}
}
