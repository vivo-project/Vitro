/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSet;

/**
 * Methods for manipulating PermissionSets.
 */
public interface PermissionSetDao {
	public PermissionSet getPermissionSetByUri(String uri);

	public Collection<PermissionSet> getAllPermissionSets();
}
