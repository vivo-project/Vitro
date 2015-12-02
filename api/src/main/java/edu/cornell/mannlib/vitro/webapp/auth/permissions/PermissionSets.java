/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.VITRO_AUTH;

/**
 * Constants and static methods to help manipulate PermissionSet instances.
 */
public class PermissionSets {
	public static final String URI_SELF_EDITOR = VITRO_AUTH + "SELF_EDITOR";
	public static final String URI_EDITOR = VITRO_AUTH + "EDITOR";
	public static final String URI_CURATOR = VITRO_AUTH + "CURATOR";
	public static final String URI_DBA = VITRO_AUTH + "ADMIN";

	/** No need to create an instance. */
	private PermissionSets() {
		// Nothing to initialize.
	}
}
