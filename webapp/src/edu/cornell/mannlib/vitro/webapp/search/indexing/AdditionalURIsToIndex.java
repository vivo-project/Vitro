/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.List;

/**
 * Interface to use with IndexBuilder to find more URIs to index given a URI.
 *
 */
public interface AdditionalURIsToIndex {
    List<String> findAdditionalURIsToIndex(String uri);
}
