/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * A Analyzer that strips html, lower cases, removes stops and
 * then does porter stemming.
 *
 * @author bdc34
 *
 */
public class HtmlLowerStopStemAnalyzer extends HtmlLowerStopAnalyzer {
    public HtmlLowerStopStemAnalyzer(){
        super();
    }

    public HtmlLowerStopStemAnalyzer(Set<String> stopWords){
        super(stopWords);
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
        return new PorterStemFilter(super.tokenStream(fieldName,arg0));
    }

}
