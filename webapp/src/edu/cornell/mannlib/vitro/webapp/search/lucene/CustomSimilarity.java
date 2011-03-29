/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import org.apache.lucene.search.DefaultSimilarity;

public class CustomSimilarity extends DefaultSimilarity {
	
	public CustomSimilarity(){}
	
	/**
	 * According to Lucene Javadoc, idf computes a score factor
	 * based on a term's document frequency (the number of documents
	 * that contain the term).
	 * 
	 * idf = log(numDocs/(docFreq + 1))
	 * 
	 * From this formula we see that, the lower the value of docFreq
	 * higher the value of idf. In other words, rare terms have higher 
	 * idf scores.
	 * 
	 * Returning a value of 1.0f here for idf, since we wan't the 
	 * rarity of a term not to effect the score of a document.
	 * 
	 * bk392 3/29/2011
	 */
	@Override
	public float idf(int docFreq, int numDocs){
		return 1.0f;
	}
	
	
	/**
	 * Coord computes a score factor based on the fraction of all query terms
	 * that a document contains. The default implementation is
	 * 
	 * coord = (overlap/ maxOverlap)
	 * 
	 * overlap is the number of queryterms matched in the document and maxOverlap
	 * is the total number of terms present in the query. That means, more number of 
	 * query terms matched in a document, higher the score. Here, we are returning a 
	 * value of 1.0f to override this effect.
	 */
	@Override
	public float coord(int overlap, int maxOverlap){
		return 1.0f;
	}
	
	/**
	 * From Lucene Javadoc, lengthNorm computes the normalization value
	 * for a given field. These values together with the field boosts, are 
	 * stored in an index and multiplied into scores for hits on each field by 
	 * the search code.
	 * 
	 * lengthNorm = 1 / sqrt(numTerms)
	 * 
	 * In other words, the document score is inversely proportional to the number of terms
	 * contained in the field of interest. Higher the number, lower the doc score. We don't 
	 * want this since most of our fields contain single value. (except ALLTEXT and type)
	 */
	@Override
	public float lengthNorm(String fieldName, int numTerms){
		return 1.0f;
	}
}
