/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

/** Should we allow the user to delete this ObjectPropertyStatement? */
public class DropObjectPropStmt extends AbstractObjectPropertyAction {

	public DropObjectPropStmt(String sub, String pred, String obj) {
		super(sub, pred, obj);
	}
}
