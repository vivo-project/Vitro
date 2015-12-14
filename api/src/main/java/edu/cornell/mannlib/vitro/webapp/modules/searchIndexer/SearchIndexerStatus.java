/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchIndexer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An immutable summary of the status of the SearchIndexer, at a fixed point in
 * time. Contains the current state, the time, and some counts.
 * 
 * If the indexer is processing URIs, processing statements, or preparing a
 * rebuild, the counts are URI_COUNTS, STATEMENT_COUNTS, or REBUILD_COUNTS,
 * respectively.
 * 
 * When the indexer starts up, becomes idle, or shuts down, the counts are
 * NO_COUNTS.
 */
public class SearchIndexerStatus {
	// ----------------------------------------------------------------------
	// factory methods
	// ----------------------------------------------------------------------

	public static SearchIndexerStatus idle() {
		return new SearchIndexerStatus(State.IDLE, new Date(), new NoCounts());
	}

	public static SearchIndexerStatus shutdown() {
		return new SearchIndexerStatus(State.SHUTDOWN, new Date(),
				new NoCounts());
	}

	// ----------------------------------------------------------------------
	// the instance
	// ----------------------------------------------------------------------

	private final State state;
	private final Date since;
	private final Counts counts;

	public SearchIndexerStatus(State state, Date since, Counts counts) {
		this.state = state;
		this.since = since;
		this.counts = counts;
	}

	public State getState() {
		return state;
	}

	public Date getSince() {
		return since;
	}

	public Counts getCounts() {
		return counts;
	}

	@Override
	public String toString() {
		return new SimpleDateFormat().format(since) + ", " + counts;
	}

	// ----------------------------------------------------------------------
	// helper classes
	// ----------------------------------------------------------------------

	public enum State {
		IDLE, PROCESSING_URIS, PROCESSING_STMTS, REBUILDING, SHUTDOWN
	}

	public abstract static class Counts {
		public enum Type {
			URI_COUNTS, STATEMENT_COUNTS, REBUILD_COUNTS, NO_COUNTS
		}

		private final Type type;

		public Counts(Type type) {
			this.type = type;
		}

		public Type getType() {
			return this.type;
		}

		public UriCounts asUriCounts() {
			return (UriCounts) this;
		}

		public StatementCounts asStatementCounts() {
			return (StatementCounts) this;
		}

		public RebuildCounts asRebuildCounts() {
			return (RebuildCounts) this;
		}

		public NoCounts asNoCounts() {
			return (NoCounts) this;
		}
	}

	public static class UriCounts extends Counts {
		private final int excluded;
		private final int deleted;
		private final int updated;
		private final int remaining;
		private final int total;

		public UriCounts(int excluded, int deleted, int updated, int remaining, int total) {
			super(Type.URI_COUNTS);
			this.excluded = excluded;
			this.deleted = deleted;
			this.updated = updated;
			this.remaining = remaining;
			this.total = total;
		}

		public int getExcluded() {
			return excluded;
		}
		
		public int getDeleted() {
			return deleted;
		}

		public int getUpdated() {
			return updated;
		}

		public int getRemaining() {
			return remaining;
		}

		public int getTotal() {
			return total;
		}

		@Override
		public String toString() {
			return "[excluded=" + excluded + ", deleted=" + deleted + ", updated=" + updated
					+ ", remaining=" + remaining + ", total=" + total + "]";
		}
	}

	public static class StatementCounts extends Counts {
		private final int processed;
		private final int remaining;
		private final int total;

		public StatementCounts(int processed, int remaining, int total) {
			super(Type.STATEMENT_COUNTS);
			this.processed = processed;
			this.remaining = remaining;
			this.total = total;
		}

		public int getProcessed() {
			return processed;
		}

		public int getRemaining() {
			return remaining;
		}

		public int getTotal() {
			return total;
		}

		@Override
		public String toString() {
			return "[processed=" + processed + ", remaining=" + remaining
					+ ", total=" + total + "]";
		}
	}

	public static class RebuildCounts extends Counts {
		private final int documentsBefore;
		private final int documentsAfter;

		public RebuildCounts(int documentsBefore, int documentsAfter) {
			super(Type.REBUILD_COUNTS);
			this.documentsBefore = documentsBefore;
			this.documentsAfter = documentsAfter;
		}

		public int getDocumentsBefore() {
			return documentsBefore;
		}

		public int getDocumentsAfter() {
			return documentsAfter;
		}

		@Override
		public String toString() {
			return "[documentsBefore=" + documentsBefore + ", documentsAfter="
					+ documentsAfter + "]";
		}
	}

	public static class NoCounts extends Counts {
		public NoCounts() {
			super(Type.NO_COUNTS);
		}

		@Override
		public String toString() {
			return "[]";
		}
	}
}
