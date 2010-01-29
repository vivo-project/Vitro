package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 3:17:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface KeywordIndividualRelationDao {
    KeywordIndividualRelation getKeywordIndividualRelationByURI(String URI);

    List<KeywordIndividualRelation> getKeywordIndividualRelationsByIndividualURI(String individualURI);

    String insertNewKeywordIndividualRelation(KeywordIndividualRelation k);

    void updateKeywordIndividualRelation(KeywordIndividualRelation k);

    void deleteKeywordIndividualRelation(KeywordIndividualRelation k);
}
