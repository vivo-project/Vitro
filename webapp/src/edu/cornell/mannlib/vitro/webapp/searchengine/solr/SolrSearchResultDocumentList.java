/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import java.util.Iterator;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResultDocument;

/**
 * A Solr-based implementation of SearchResultDocumentList.
 * 
 * It's necessary to use this instead of the base version, so the iterator can
 * convert each document as it is requested.
 */
public class SolrSearchResultDocumentList implements SearchResultDocumentList {
	private SolrDocumentList solrDocs;

	public SolrSearchResultDocumentList(SolrDocumentList solrDocs) {
		if (solrDocs == null) {
			SolrDocumentList list = new SolrDocumentList();
			list.setStart(0L);
			list.setNumFound(0L);
			list.setMaxScore(0.0F);
			this.solrDocs = list;
		} else {
			this.solrDocs = solrDocs;
		}
	}

	@Override
	public Iterator<SearchResultDocument> iterator() {
		return new SearchResultDocumentIterator(solrDocs.iterator());
	}

	@Override
	public long getNumFound() {
		return solrDocs.getNumFound();
	}

	@Override
	public int size() {
		return solrDocs.size();
	}

	@Override
	public SearchResultDocument get(int i) {
		return convertToSearchResultDocument(solrDocs.get(i));
	}

	private static class SearchResultDocumentIterator implements
			Iterator<SearchResultDocument> {
		private final Iterator<SolrDocument> solrIterator;

		public SearchResultDocumentIterator(Iterator<SolrDocument> solrIterator) {
			this.solrIterator = solrIterator;
		}

		@Override
		public boolean hasNext() {
			return solrIterator.hasNext();
		}

		@Override
		public SearchResultDocument next() {
			return convertToSearchResultDocument(solrIterator.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static SearchResultDocument convertToSearchResultDocument(
			SolrDocument solrDoc) {
		return new BaseSearchResultDocument(
				(String) solrDoc.getFieldValue("DocId"),
				solrDoc.getFieldValuesMap());
	}

}
