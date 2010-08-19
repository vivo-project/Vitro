/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.docbuilder;

import edu.cornell.mannlib.vitro.webapp.search.IndexingException;

/**
 * The Obj2Doc is an object that can translate something into
 * an object that the indexer can use.
 *
 * @author bdc34
 *
 */
public interface Obj2DocIface {
    /**
     * Tests to see if this Obj2Doc an translate this object.
     * @param obj
     * @return
     */
    public boolean canTranslate(Object obj);

    /**
     * Returns an object that the indexer can use.
     * @param obj
     * @return
     * @throws IndexingException 
     */
    public Object translate(Object obj) throws IndexingException;

    /**
     * Returns a vitro object from a search result/hit.
     *
     */
    public Object unTranslate(Object result);

    /**
     * Test to see if this can untranslate a search result/hit.
     */
    public boolean canUnTranslate(Object result);

    /**
     * Gets the id used by the index for this obj.
     * @param obj
     * @return
     */
    public Object getIndexId(Object obj);
}
