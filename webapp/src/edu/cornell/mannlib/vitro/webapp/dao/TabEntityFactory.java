package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

import java.util.List;
/**
 * This class defines a minimum that we can expect a
 * TabEntityFactory to have for methods.
 *
 * @author bdc34
 *
 */
public interface TabEntityFactory {

    /**
     * Expected to return a list of entities to be displayed on the tab.
     * The standard meaning of the alpha parameters is that the tab
     * should only include entities that have names that start with that
     * letter.
     *
     * @param alpha
     * @return
     */
    public List <Individual> getRelatedEntites(String alpha);

    /**
     * Gets a count of entities that would be displayed on the tab.
     * @return
     */
    public int getRelatedEntityCount();

    /**
     * Returns a list of letters for which there are entities.
     * Each string should have a single letter.
     *
     * @return
     */
    public List <String> getLettersOfEnts();

}