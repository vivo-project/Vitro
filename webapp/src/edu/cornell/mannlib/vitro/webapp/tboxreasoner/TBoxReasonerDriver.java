/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * What calls can the ConfiguredReasonerListener make to drive the TBox
 * reasoner?
 */
public interface TBoxReasonerDriver {
	void runSynchronizer();

	void addStatement(Statement stmt);

	void removeStatement(Statement stmt);

	void deleteDataProperty(Statement stmt);

	void deleteObjectProperty(Statement stmt);
	
	boolean isReasoning();

	Status getStatus();

	public static class Status {
		public static final Status SUCCESS = new Status(true, false, "");
		public static final Status ERROR = new Status(true, true, "");
		
		public static final Status inconsistent(String explanation) {
			return new Status(false, false, explanation);
		}

		private final boolean consistent;
		private final boolean inErrorState;
		private final String explanation;

		private Status(boolean consistent, boolean inErrorState,
				String explanation) {
			this.consistent = consistent;
			this.inErrorState = inErrorState;
			this.explanation = explanation;
		}

		public boolean isConsistent() {
			return consistent;
		}

		public boolean isInErrorState() {
			return inErrorState;
		}

		public String getExplanation() {
			return explanation;
		}

	}

}
