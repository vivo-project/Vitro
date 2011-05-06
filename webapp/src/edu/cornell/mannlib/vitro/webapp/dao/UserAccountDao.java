/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;


/**
 * TODO
 */
public interface UserAccountDao {

	public UserAccount getUserAccountByUri(String uri);

	/**
	 * <pre>
	 * public User getUserByUsername(String username);
	 * 
	 * public User getUserByURI(String URI);
	 * 
	 * public List&lt;User&gt; getAllUsers();
	 * 
	 * public void updateUser(User user);
	 * 
	 * public String insertUser(User user);
	 * 
	 * public void deleteUser(User user);
	 * 
	 * public List&lt;String&gt; getIndividualsUserMayEditAs(String userURI);
	 * 
	 * public List&lt;String&gt; getUserAccountEmails();
	 * 
	 * public String getUserEmailAddress(String userURI);
	 * </pre>
	 */
}
