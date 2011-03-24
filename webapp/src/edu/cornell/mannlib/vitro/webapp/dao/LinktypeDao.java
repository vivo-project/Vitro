package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Linktype;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 3:19:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface LinktypeDao {
    List<Linktype> getAllLinktypes();
}
