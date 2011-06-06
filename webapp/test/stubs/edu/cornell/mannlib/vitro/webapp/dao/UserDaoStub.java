/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;

/**
 * TODO
 */
public class UserDaoStub implements UserDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, User> userByUriMap = new HashMap<String, User>();

	public void addUser(User user) {
		userByUriMap.put(user.getURI(), user);
	}
	
	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public User getUserByURI(String URI) {
		return userByUriMap.get(URI);
	}
	
	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public User getUserByUsername(String username) {
		throw new RuntimeException(
				"UserDaoStub.getUserByUsername() not implemented.");
	}

	@Override
	public List<User> getAllUsers() {
		throw new RuntimeException("UserDaoStub.getAllUsers() not implemented.");
	}

	@Override
	public void updateUser(User user) {
		throw new RuntimeException("UserDaoStub.updateUser() not implemented.");
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
	public List<String> getIndividualsUserMayEditAs(String userURI) {
		throw new RuntimeException(
				"UserDaoStub.getIndividualsUserMayEditAs() not implemented.");
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
