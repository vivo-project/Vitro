/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import edu.cornell.mannlib.vitro.webapp.beans.*;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import net.sf.jga.fn.UnaryFunctor;

import java.util.List;

public class UserDaoFiltering extends BaseFiltering implements UserDao{

    private final UserDao innerDao;
    private final VitroFilters filters;

    public UserDaoFiltering(UserDao userDao, VitroFilters filters) {
        this.innerDao = userDao;
        this.filters = filters;
    }

    public List<User> getAllUsers() {
        return filter(innerDao.getAllUsers(),filters.getUserFilter());
    }

    public User getUserByURI(String URI) {
        User u = innerDao.getUserByURI(URI);
        if( u != null && filters.getUserFilter().fn(u))
            return u;
        else
            return null;
    }

    public User getUserByUsername(String username) {
        User u = innerDao.getUserByUsername(username);
        if( u != null && filters.getUserFilter().fn(u))
            return u;
        else
            return null;
    }

    public void updateUser(User user) {
        innerDao.updateUser(user);
    }

    public String insertUser(User user) {
        return innerDao.insertUser(user);
    }

    public void deleteUser(User user) {
        innerDao.deleteUser(user);
    }

    public List<String> getIndividualsUserMayEditAs(String userURI) {
        return innerDao.getIndividualsUserMayEditAs(userURI);
    }

}