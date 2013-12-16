/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search;

public class VitroSearchTermNames {
    
    /** Id of entity, vclass or tab */
    public static String URI         = "URI";
    /** search document id */
    public static String DOCID      = "DocId";
    
    /** rdf:type */
    public static String RDFTYPE    = "type";
    /** classgroups from the individual's rdf:types  */
    public static String CLASSGROUP_URI    = "classgroup";    
    /** Most specific types for individual*/
     public static String MOST_SPECIFIC_TYPE_URIS = "mostSpecificTypeURIs";
    
    /** time of index in msec since epoc */
    public static String INDEXEDTIME= "indexedTime";
    
    /** text for 'full text' search, this is stemmed */
    public static String ALLTEXT    = "ALLTEXT";
    /** text for 'full text' search, this is unstemmed for
     * use with wildcards and prefix queries */
    public static String ALLTEXTUNSTEMMED = "ALLTEXTUNSTEMMED";            
    
    /** Does the individual have a thumbnail image? 1=yes 0=no */
    public static final String THUMBNAIL = "THUMBNAIL";        
    /** download url location for thumbnail */
    public static final String THUMBNAIL_URL = "THUMBNAIL_URL";
    
    // Fields derived from rdfs:label
    /** Raw rdfs:label: no lowercasing, no tokenizing, no stop words, no stemming **/
    public static String NAME_RAW = "nameRaw"; // 
    
    /** rdfs:label lowercased, no tokenizing, no stop words, no stemming **/
    public static String NAME_LOWERCASE = "nameLowercase"; // 

    /** Same as NAME_LOWERCASE, but single-valued so it's sortable. **/
    // RY Need to control how indexing selects which of multiple values to copy. 
    public static String NAME_LOWERCASE_SINGLE_VALUED = "nameLowercaseSingleValued";
    
    /** rdfs:label lowercased, tokenized, stop words, no stemming **/
    public static String NAME_UNSTEMMED = "nameUnstemmed"; 
    
    /** rdfs:label lowercased, tokenized, stop words, stemmed **/
    public static String NAME_STEMMED = "nameStemmed"; 

    /** preferred title */
    public static final String PREFERRED_TITLE = "PREFERRED_TITLE";
    
    public static final String NAME_PHONETIC = "NAME_PHONETIC";
    
    /** rdfs:label lowercased, untokenized, edge-n-gram-filtered for autocomplete on people names **/
    public static String AC_NAME_UNTOKENIZED = "acNameUntokenized";

    /** rdfs:label lowercased, tokenized, stop words, stemmed, edge-n-gram-filtered for autocomplete 
     * on non-person labels such as book titles and grant names **/
    public static String AC_NAME_STEMMED = "acNameStemmed";
    
    /* There is currently no use case for an autocomplete search field that is tokenized but not stemmed. 
    public static String AC_NAME_UNSTEMMED = "acNameUnstemmed";  */
    
    /** Beta values used in weighting **/
    public static final String BETA = "BETA";

	/** Source institution URL */
	public static final String SITE_URL = "siteURL";

	/** Source institution name */
	public static final String SITE_NAME = "siteName";
	
}
