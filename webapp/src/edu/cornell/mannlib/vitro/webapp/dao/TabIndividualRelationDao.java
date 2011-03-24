package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.TabIndividualRelation;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 4:35:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TabIndividualRelationDao {
    TabIndividualRelation getTabIndividualRelationByURI(String uri);

    List<TabIndividualRelation> getTabIndividualRelationsByIndividualURI(String individualURI);

    List<TabIndividualRelation> getTabIndividualRelationsByTabURI(String individualURI);

    int insertNewTabIndividualRelation(TabIndividualRelation t2e );

    void updateTabIndividualRelation(TabIndividualRelation t2e);

    void deleteTabIndividualRelation(TabIndividualRelation t2e);

    void insertTabIndividualRelation(Tab tab, Individual ent);

    boolean tabIndividualRelationExists(Tab tab, Individual ent);
}
