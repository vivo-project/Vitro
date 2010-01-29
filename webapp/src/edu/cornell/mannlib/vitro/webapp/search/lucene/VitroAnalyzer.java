package edu.cornell.mannlib.vitro.webapp.search.lucene;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.TokenStream;

public class VitroAnalyzer extends Analyzer {
    Analyzer keywordAnalyzer;
    Analyzer stemmingAnalyzer;
    Analyzer nonStemmingAnalyzer;
    
    public VitroAnalyzer(){
        keywordAnalyzer = new KeywordAnalyzer();
        stemmingAnalyzer = new HtmlLowerStopStemAnalyzer();
        nonStemmingAnalyzer = new HtmlLowerStopAnalyzer();
    }
    
    @Override
    public TokenStream tokenStream(String field, Reader reader) {
        if( Entity2LuceneDoc.term.ALLTEXT.equals(field) ||
            Entity2LuceneDoc.term.NAME.equals(field) )
            return stemmingAnalyzer.tokenStream(field, reader);
        else if( Entity2LuceneDoc.term.ALLTEXTUNSTEMMED.equals(field) )
            return nonStemmingAnalyzer.tokenStream(field, reader);
        else{
            return keywordAnalyzer.tokenStream(field, reader);
        }
    }

}