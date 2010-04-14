/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.search.SearchException;

import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;

/**
 * This is used by the SearchController.  Any search back end should
 * implement this.
 *
 * Currently we use LuceneSearcher. It gets setup by LuceneSetup which
 * is specified to run as a listener in the application's web.xml.
 *
 * @author bdc34
 *
 */
public interface Searcher {

    public VitroQueryFactory getQueryFactory();

    /**
     * return a list of object that are related to the query.
     * The objects should be of type Entity or Tab, if not they
     * will be ignored.
     * @param query
     * @return
     * @throws SearchException
     */
    public List search( VitroQuery query ) throws SearchException;

    /**
     * The searcher may need to be used when making a highlighter.
     * In Lucene the highlighter needs access to the index.
     * @param q
     * @return
     */
    public abstract VitroHighlighter getHighlighter(VitroQuery q);

    /**
     * Used to close the searcher if the index that it was using gets
     * deleted.
     */
    public void close();
}
