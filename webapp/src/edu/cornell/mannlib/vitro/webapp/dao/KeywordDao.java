package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Keyword;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 3:18:29 PM
 * To change this template use File | Settings | File Templates.
 */
public interface KeywordDao {
    Keyword getKeywordById(int id);

    List<Keyword> getAllKeywords();

    List<Keyword> getKeywordsByStem(String stem);

    int insertNewKeyword(Keyword k);

    void updateKeyword(Keyword k);

    void deleteKeyword(Keyword keyword);

    List /*String*/ getAllOrigins();
}
