/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/**
 * Should we allow the user to delete this DataPropertyStatement from this
 * model?
 */
public class DropDataPropertyStatement extends
		AbstractDataPropertyStatementAction {

	public DropDataPropertyStatement(String subjectUri, String predicateUri) {
		super(subjectUri, predicateUri);
	}

	public DropDataPropertyStatement(DataPropertyStatement dps) {
		super(dps);
	}
}
