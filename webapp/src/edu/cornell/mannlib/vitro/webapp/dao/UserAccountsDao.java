/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Methods for dealing with UserAccount and PermissionSet objects in the User
 * Accounts model.
 */
public interface UserAccountsDao {

	/**
	 * Get the UserAccount for this URI.
	 * 
	 * @return null if the URI is null, or if there is no such UserAccount
	 */
	UserAccount getUserAccountByUri(String uri);

	/**
	 * Get the PermissionSet for this URI.
	 * 
	 * @return null if the URI is null, or if there is no such PermissionSet.
	 */
	PermissionSet getPermissionSetByUri(String uri);

	/**
	 * Get all of the PermissionSets in the model.
	 * 
	 * @return a collection which might be empty, but is never null.
	 */
	Collection<PermissionSet> getAllPermissionSets();

}
