/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;

public class UserDaoStub implements UserDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private Map<String, User> usersByUsername = new HashMap<String, User>();
	private Map<String, List<String>> individualsUserMayEditAs = new HashMap<String, List<String>>();

	public void addUser(User user) {
		usersByUsername.put(user.getUsername(), user);
	}

	public void setIndividualsUserMayEditAs(String userUri, List<String> uriList) {
		individualsUserMayEditAs.put(userUri, uriList);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public User getUserByUsername(String username) {
		return usersByUsername.get(username);
	}

	/**
	 * Does nothing for now. Do we need to record that the user has been
	 * updated?
	 */
	@Override
	public void updateUser(User user) {
	}

	@Override
	public List<String> getIndividualsUserMayEditAs(String userURI) {
		if (individualsUserMayEditAs.containsKey(userURI)) {
			return individualsUserMayEditAs.get(userURI);
		} else {
			return Collections.emptyList();
		}
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public User getUserByURI(String URI) {
		throw new RuntimeException(
				"UserDaoStub.getUserByURI() not implemented.");
	}

	@Override
	public List<User> getAllUsers() {
		throw new RuntimeException("UserDaoStub.getAllUsers() not implemented.");
	}

	@Override
	public String insertUser(User user) {
		throw new RuntimeException("UserDaoStub.insertUser() not implemented.");
	}

	@Override
	public void deleteUser(User user) {
		throw new RuntimeException("UserDaoStub.deleteUser() not implemented.");
	}

	@Override
	public List<String> getUserAccountEmails() {
		throw new RuntimeException(
				"UserDaoStub.getUserAccountEmails() not implemented.");
	}

	@Override
	public String getUserEmailAddress(String userURI) {
		throw new RuntimeException(
				"UserDaoStub.getUserEmailAddress() not implemented.");
	}

}
