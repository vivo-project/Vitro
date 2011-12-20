/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

/**
 * This doesn't actually do any filtering. It's just a placeholder in case we
 * decide to filter either UserAccounts or PermissionSets.
 */
public class UserAccountsDaoFiltering extends BaseFiltering implements
		UserAccountsDao {

	private final UserAccountsDao innerDao;

	@SuppressWarnings("unused")
	private final VitroFilters filters;

	public UserAccountsDaoFiltering(UserAccountsDao userDao,
			VitroFilters filters) {
		this.innerDao = userDao;
		this.filters = filters;
	}

	@Override
	public Collection<UserAccount> getAllUserAccounts() {
		return innerDao.getAllUserAccounts();
	}

	@Override
	public UserAccount getUserAccountByUri(String uri) {
		return innerDao.getUserAccountByUri(uri);
	}

	@Override
	public UserAccount getUserAccountByEmail(String emailAddress) {
		return innerDao.getUserAccountByEmail(emailAddress);
	}

	@Override
	public UserAccount getUserAccountByExternalAuthId(String externalAuthId) {
		return innerDao.getUserAccountByExternalAuthId(externalAuthId);
	}

	@Override
	public Collection<UserAccount> getUserAccountsWhoProxyForPage(
			String profilePageUri) {
		return innerDao.getUserAccountsWhoProxyForPage(profilePageUri);
	}

	@Override
	public String insertUserAccount(UserAccount userAccount) {
		return innerDao.insertUserAccount(userAccount);
	}

	@Override
	public void updateUserAccount(UserAccount userAccount) {
		innerDao.updateUserAccount(userAccount);
	}

	@Override
	public void deleteUserAccount(String userAccountUri) {
		innerDao.deleteUserAccount(userAccountUri);
	}

	@Override
	public void setProxyAccountsOnProfile(String profilePageUri,
			Collection<String> userAccountUris) {
		innerDao.setProxyAccountsOnProfile(profilePageUri, userAccountUris);
	}

	@Override
	public PermissionSet getPermissionSetByUri(String uri) {
		return innerDao.getPermissionSetByUri(uri);
	}

	@Override
	public Collection<PermissionSet> getAllPermissionSets() {
		return innerDao.getAllPermissionSets();
	}

}