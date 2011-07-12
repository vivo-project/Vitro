package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.List;

/**
 * Interface to use with IndexBuilder to find more URIs to index given a URI.
 *
 */
public interface AdditionalURIsToIndex {
    List<String> findAdditionalURIsToIndex(String uri);
}
