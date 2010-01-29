package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 4:32:30 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FlagDao {
    String getFlagNames(String table, String field);
    public String convertNumericFlagToInsertString(int numeric,String column,String table);
    public String convertNumericFlagToInsertString(int numeric,String flagColumns);
}
