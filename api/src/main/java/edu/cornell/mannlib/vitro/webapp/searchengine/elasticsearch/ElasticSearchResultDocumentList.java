/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;

/**
 * A simple implementation. In fact, this is so simple that perhaps it should be
 * named BaseSearchResultDocumentList.
 */
class ElasticSearchResultDocumentList implements SearchResultDocumentList {
    private final List<SearchResultDocument> documents;
    private final long numberFound;

    public ElasticSearchResultDocumentList(List<SearchResultDocument> documents,
            long numberFound) {
        this.documents = documents;
        this.numberFound = numberFound;
    }

    @Override
    public Iterator<SearchResultDocument> iterator() {
        return documents.iterator();
    }

    @Override
    public long getNumFound() {
        return numberFound;
    }

    @Override
    public int size() {
        return documents.size();
    }

    @Override
    public SearchResultDocument get(int i) {
        return documents.get(i);
    }

    @Override
    public String toString() {
        return String.format(
                "ElasticSearchResultDocumentList[numberFound=%s, documents=%s]",
                numberFound, documents);
    }

}