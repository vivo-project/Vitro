/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.TabVClassRelation;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 3:20:27 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TabVClassRelationDao {

    int insertTabVClassRelation(TabVClassRelation t2t );

    void deleteTabVClassRelation(TabVClassRelation t2t);

}
