/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search;

public class VitroSearchTermNames {
    /** Id of entity, vclass or tab */
    public static String URI         = "URI";
    /** lucene document id */
    public static String DOCID      = "DocId";
    /** java class of the object that the Doc represents. */
    public static String JCLASS     = "JCLASS";
    /** rdf:type */
    public static String RDFTYPE    = "type";
    /** rdf:type */
    public static String CLASSGROUP_URI    = "classgroup";
    /** Modtime from db */
    public static String MODTIME    = "modTime";

    /** time of index in msec since epoc */
    public static String INDEXEDTIME= "indexedTime";
    /** timekey of entity in yyyymmddhhmm  */
    public static String TIMEKEY="TIMEKEY";
    /** time of sunset/end of entity in yyyymmddhhmm  */
    public static String SUNSET="SUNSET";
    /** time of sunrise/start of entity in yyyymmddhhmm  */
    public static String SUNRISE="SUNRISE";
    /** entity's moniker */
    public static String MONIKER="moniker";
    /** text for 'full text' search, this is stemmed */
    public static String ALLTEXT    = "ALLTEXT";
    /** text for 'full text' search, this is unstemmed for
     * use with wildcards and prefix queries */
    public static String ALLTEXTUNSTEMMED = "ALLTEXTUNSTEMMED";
    /** class name for storing targeted information **/
    public static final String targetInfo = "targetInfo";
    /** keywords */
    public static final String KEYWORDS = "KEYWORDS";
    /** Does the individual have a thumbnail image? 1=yes 0=no */
    public static final String THUMBNAIL = "THUMBNAIL";        
    /** Should individual be included in full text search results? 1=yes 0=no */
    public static final String PROHIBITED_FROM_TEXT_RESULTS = "PROHIBITED_FROM_TEXT_RESULTS";
    /** class names in human readable form of an individual*/
    public static final String CLASSLOCALNAMELOWERCASE = "classLocalNameLowerCase";
    /** class names in human readable form of an individual*/
    public static final String CLASSLOCALNAME = "classLocalName";      

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
    
    /** rdfs:label lowercased, untokenized, edge-n-gram-filtered for autocomplete on people names **/
    public static String AC_NAME_UNTOKENIZED = "acNameUntokenized";

    /** field for beta values of all documents **/
    public static final String BETA = "BETA";
    public static final String PHI = "PHI";
    public static final String ADJACENT_NODES = "ADJACENT_NODES";
    
    /** adding phonetic field **/
    public static final String ALLTEXT_PHONETIC = "ALLTEXT_PHONETIC";
    public static final String NAME_PHONETIC = "NAME_PHONETIC";
}
