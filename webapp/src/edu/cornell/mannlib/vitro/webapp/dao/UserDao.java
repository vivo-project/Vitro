/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.User;

import java.util.List;

public interface UserDao {

    public User getUserByUsername(String username);

    public User getUserByURI(String URI);

    public List <User> getAllUsers();

    public void updateUser(User user);

    public String insertUser(User user);

    public void deleteUser(User user);

    public List<String> getIndividualsUserMayEditAs(String userURI);
    
    public List<String> getUserAccountEmails();
    
    public String getUserEmailAddress(String userURI);
}
