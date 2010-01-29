package edu.cornell.mannlib.vitro.webapp.search.lucene;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.utils.Html2Text;

/**
 * This is a highlighter and fragmenter for use with PagedSearchController. 
 */
public class SimpleLuceneHighlighter extends VitroHighlighter{    
    Highlighter fragHighlighter = null;
    Analyzer analyzer = null;
    
    public SimpleLuceneHighlighter(Query query, Analyzer a){
        QueryScorer scorer = new QueryScorer( query ,Entity2LuceneDoc.term.ALLTEXT);

        Formatter formatter =
            new SimpleHTMLFormatter(preTag,postTag);
        this.analyzer = a;
        this.fragHighlighter = new Highlighter(formatter, scorer);
    }
   
    @Override
    public String highlight( String in){
        //not really implemented.
        return in;
    }
    
    @Override
    public String getHighlightFragments(String in ) {
        Html2Text h2t = new Html2Text();
        try{
            h2t.parse(in);
        }catch(IOException ioe){
            log.debug("could not strip html from string",ioe);
        }
        String txt = h2t.getText();

        if( txt != null && txt.trim().length() > 0){
            String b = doHighlight( txt ,fragHighlighter);
            if( b != null && b.trim().length() > 0 )
                return "..." + " " + b + " " + "...";
            else
                return "";
        } else {
            return "";
        }
    }

    private String doHighlight(String in, Highlighter hi ) {
        String result = in;
        if(in != null ){
            TokenStream tokenStream =
                analyzer.tokenStream(Entity2LuceneDoc.term.ALLTEXT, new StringReader(in));
            try {
                //Get 3 best fragments and seperate with a "..."
                result = hi.getBestFragments(tokenStream, in , 3, "...");
            } catch (IOException e) {
                log.debug("could not highlight",e);
            }
        }
        return result;
    }
    
    private static Log log = LogFactory.getLog(SimpleLuceneHighlighter.class);
}
