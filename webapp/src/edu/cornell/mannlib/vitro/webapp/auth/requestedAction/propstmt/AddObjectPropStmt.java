/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

/** Should we allow the user to add this ObjectPropertyStatement? */
public class AddObjectPropStmt extends AbstractObjectPropertyAction {

	public AddObjectPropStmt(String uriOfSub, String uriOfPred, String uriOfObj) {
		super(uriOfSub, uriOfPred, uriOfObj);
	}
}
