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
	 * Get all of the UserAccounts in the model.
	 */
	Collection<UserAccount> getAllUserAccounts();

	/**
	 * Get the UserAccount for this URI.
	 * 
	 * @return null if the URI is null, or if there is no such UserAccount
	 */
	UserAccount getUserAccountByUri(String uri);

	/**
	 * Get the UserAccount for this Email address.
	 * 
	 * @return null if the Email address is null, or if there is no such
	 *         UserAccount
	 */
	UserAccount getUserAccountByEmail(String emailAddress);

	/**
	 * Get the UserAccount for this External Authentication ID
	 * 
	 * @return null if the ID is null, or if there is no such UserAccount
	 */
	UserAccount getUserAccountByExternalAuthId(String externalAuthId);
	
	/**
	 * Get any UserAccounts who act as proxy editors for this profile page.
	 */
	Collection<UserAccount> getUserAccountsWhoProxyForPage(String profilePageUri);

	/**
	 * Create a new UserAccount in the model.
	 * 
	 * On entry, the URI of the UserAccount should be empty. On exit, the URI
	 * which was created for this UserAccount will be stored in the UserAccount,
	 * as well as being returned by the method.
	 * 
	 * Does not confirm that PermissionSet objects already exist for the
	 * PermissionSet URIs referenced by the UserAcocunt.
	 * 
	 * @throws NullPointerException
	 *             if the UserAccount is null.
	 * @throws IllegalArgumentException
	 *             if the URI of the UserAccount is not empty.
	 */
	String insertUserAccount(UserAccount userAccount);

	/**
	 * Update the values on a UserAccount that already exists in the model.
	 * 
	 * Does not confirm that PermissionSet objects already exist for the
	 * PermissionSet URIs referenced by the UserAcocunt.
	 * 
	 * @throws NullPointerException
	 *             if the UserAccount is null.
	 * @throws IllegalArgumentException
	 *             if a UserAccount with this URI does not already exist in the
	 *             model.
	 */
	void updateUserAccount(UserAccount userAccount);

	/**
	 * Remove the UserAccount with this URI from the model.
	 * 
	 * If the URI is null, or if no UserAccount with this URI is found in the
	 * model, no action is taken.
	 */
	void deleteUserAccount(String userAccountUri);

	/**
	 * Set so that these UserAccounts, and only these, are authorized as proxies on this
	 * profile page.
	 */
	void setProxyAccountsOnProfile(String profilePageUri, Collection<String> userAccountUris);
	
	/**
	 * Get the PermissionSet for this URI.
	 * 
	 * @return null if the URI is null, or if there is no such PermissionSet.
	 */
	PermissionSet getPermissionSetByUri(String uri);

	/**
	 * Get all of the PermissionSets in the model, sorted by URI.
	 * 
	 * @return a collection which might be empty, but is never null.
	 */
	Collection<PermissionSet> getAllPermissionSets();

}
