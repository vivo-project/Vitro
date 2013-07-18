/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;

/**
 * TODO
 */
public class UserAccountsDaoStub implements UserAccountsDao {
	private static final Log log = LogFactory.getLog(UserAccountsDaoStub.class);

	private final Map<String, UserAccount> userAccountsByUri = new HashMap<String, UserAccount>();
	private final Map<String, PermissionSet> permissionSetsByUri = new HashMap<String, PermissionSet>();

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	public void addUser(UserAccount user) {
		userAccountsByUri.put(user.getUri(), user);
	}
	
	public void addPermissionSet(PermissionSet ps) {
		permissionSetsByUri.put(ps.getUri(), ps);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public UserAccount getUserAccountByUri(String uri) {
		return userAccountsByUri.get(uri);
	}

	@Override
	public Collection<PermissionSet> getAllPermissionSets() {
		return new ArrayList<PermissionSet>(permissionSetsByUri.values());
	}

	@Override
	public PermissionSet getPermissionSetByUri(String uri) {
		return permissionSetsByUri.get(uri);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public UserAccount getUserAccountByEmail(String emailAddress) {
		throw new RuntimeException(
				"UserAccountsDaoStub.getUserAccountByEmail() not implemented.");
	}

	@Override
	public String insertUserAccount(UserAccount userAccount) {
		throw new RuntimeException(
				"UserAccountsDaoStub.insertUserAccount() not implemented.");
	}

	@Override
	public void updateUserAccount(UserAccount userAccount) {
		throw new RuntimeException(
				"UserAccountsDaoStub.updateUserAccount() not implemented.");
	}

	@Override
	public void deleteUserAccount(String userAccountUri) {
		throw new RuntimeException(
				"UserAccountsDaoStub.deleteUserAccount() not implemented.");
	}

	@Override
	public UserAccount getUserAccountByExternalAuthId(String externalAuthId) {
		throw new RuntimeException(
				"UserAccountsDao.getUserAccountByExternalAuthId() not implemented.");
	}

	@Override
	public Collection<UserAccount> getAllUserAccounts() {
		throw new RuntimeException(
				"UserAccountsDao.getAllUserAccounts() not implemented.");
	}

	@Override
	public Collection<UserAccount> getUserAccountsWhoProxyForPage(
			String profilePageUri) {
		throw new RuntimeException(
				"UserAccountsDaoStub.getUserAccountsWhoProxyForPage() not implemented.");
	}

	@Override
	public void setProxyAccountsOnProfile(String profilePageUri,
			Collection<String> userAccountUris) {
		throw new RuntimeException(
				"UserAccountsDaoStub.setProxyAccountsOnProfile() not implemented.");
	}

}
