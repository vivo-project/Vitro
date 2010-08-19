/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

/**
 * A class for non-static access to the stemmer via reflection in the ingest tools
 */
public class SimpleStemmer {
	   
   private int MAX_LENGTH = 32; // value used elsewhere in Vitro
   
   public String stemString(String str) {
	   return Stemmer.StemString(str,MAX_LENGTH); 
   }		   

}
