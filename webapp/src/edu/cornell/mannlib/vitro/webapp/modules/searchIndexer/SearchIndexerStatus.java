/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchIndexer;

import java.util.Date;

/**
 * An immutable summary of the status of the SearchIndexer, at some point in
 * time.Contains the current state, and some counts.
 * 
 * If the indexer is processing URIs, processing statements, or preparing a
 * rebuild, the counts are URI_COUNTS, STATEMENT_COUNTS, or REBUILD_COUNTS.
 * 
 * When the indexer starts up, and when it is is shut down, the counts are
 * NO_COUNTS.
 * 
 * If the indexer is idle, the counts are carried over from the previous
 * operation.
 */
public class SearchIndexerStatus {
	public enum State {
		IDLE, PROCESSING_URIS, PROCESSING_STMTS, PREPARING_REBUILD, SHUTDOWN
	}

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
		private final int deleted;
		private final int updated;
		private final int remaining;
		private final int total;

		public UriCounts(int deleted, int updated, int remaining, int total) {
			super(Type.URI_COUNTS);
			this.deleted = deleted;
			this.updated = updated;
			this.remaining = remaining;
			this.total = total;
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

	}

	public static class RebuildCounts extends Counts {
		private final int numberOfIndividuals;

		public RebuildCounts(int numberOfIndividuals) {
			super(Type.REBUILD_COUNTS);
			this.numberOfIndividuals = numberOfIndividuals;
		}

		public int getNumberOfIndividuals() {
			return numberOfIndividuals;
		}

	}

	public static class NoCounts extends Counts {
		public NoCounts() {
			super(Type.NO_COUNTS);
		}
	}
}
