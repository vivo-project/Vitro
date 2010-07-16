/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class HtmlLowerStopAnalyzer extends Analyzer {
    /* much of this code is from
     * http://www.onjava.com/pub/a/onjava/2003/01/15/lucene.html?page=2
     * bdc34
     */

    private static String[] _stopWords;

    /**
     * An array containing some common English words
     * that are usually not useful for searching.
     */
    public static final String[] STOP_WORDS =
    {
        "0", "1", "2", "3", "4", "5", "6", "7", "8",
        "9", "000", "$",
        "about", "after", "all", "also", "an", "and",
        "another", "any", "are", "as", "at", "be",
        "because", "been", "before", "being", "between",
        "both", "but", "by", "came", "can", "come",
        "could", "did", "do", "does", "each", "else",
        "for", "from", "get", "got", "has", "had",
        "he", "have", "her", "here", "him", "himself",
        "his", "how","if", "in", "into", "is", "it",
        "its", "just", "like", "make", "many", "me",
        "might", "more", "most", "much", "must", "my",
        "never", "now", "of", "on", "only", "or",
        "other", "our", "out", "over", "re", "said",
        "same", "see", "should", "since", "so", "some",
        "still", "such", "take", "than", "that", "the",
        "their", "them", "then", "there", "these",
        "they", "this", "those", "through", "to", "too",
        "under", "up", "use", "very", "want", "was",
        "way", "we", "well", "were", "what", "when",
        "where", "which", "while", "who", "will",
        "with", "would", "you", "your",
        "a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r",
        "s", "t", "u", "v", "w", "x", "y", "z"
    };

    /**
     * Builds an analyzer.
     */
    public HtmlLowerStopAnalyzer()
    {
        this(STOP_WORDS);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopWords a String array of stop words
     */
    public HtmlLowerStopAnalyzer(String[] stopWords)
    {
        _stopWords = stopWords;
    }

    /**
     * Processes the input by first converting it to
     * lower case, then by eliminating stop words, and
     * finally by performing Porter stemming on it.
     *
     * @param reader the Reader that
     *               provides access to the input text
     * @return an instance of TokenStream
     */

    public TokenStream tokenStream(String fieldName, Reader arg0) {
        //ignore fieldName, tokenize all fields the same way.

        /* With this we are pipeing the output of the inner most
         * Reader outwards.
         * Input-> HtmlStrip-> LowerCase-> stopFilter-> StemFilter-> Output
         */
//        TokenStream htmlStripAndLower =
//            new LowerCaseTokenizer(arg0 );
//
//         
//        boolean IGNORE_CASE = true;
//        TokenFilter stopFilter =
//            new StopFilter(htmlStripAndLower,STOP_WORDS , IGNORE_CASE) ;
//
//        return stopFilter;
//        
        
        TokenStream result = new StandardTokenizer(arg0); 
        result = new StandardFilter(result);  //break into tokens
        result = new LowerCaseFilter(result);  //lower case
        result = new StopFilter(result, _stopWords, IGNORE_CASE);  //remove stop words
        result = new ISOLatin1AccentFilter(result); //ISO-8859-1 accented chars are replace by unaccented 
        return result;
    
    }
    
    private static final boolean IGNORE_CASE = true;

}
