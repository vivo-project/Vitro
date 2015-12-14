/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchIndexer;

/**
 * Some handy methods for dealing with the search index.
 */
public class SearchIndexerUtils {
	
	/**
	 * The document ID in the search index is derived from the individual's URI.
	 */
    public static String getIdForUri(String uri){
        if( uri != null ){
            return  "vitroIndividual:" + uri;
        }else{
            return null;
        }
    }
}
