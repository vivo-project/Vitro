/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Should we allow the user to edit this ObjectPropertyStatement in this model?
 */
public class EditObjectPropertyStatement extends AbstractObjectPropertyStatementAction {
	public EditObjectPropertyStatement(String subjectUri, String keywordPredUri,
			String objectUri) {
		super(subjectUri, keywordPredUri, objectUri);
	}

	public EditObjectPropertyStatement(ObjectPropertyStatement ops) {
		super(ops);
	}
}
