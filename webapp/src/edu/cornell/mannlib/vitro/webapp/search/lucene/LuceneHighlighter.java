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

public class LuceneHighlighter extends VitroHighlighter{
    /* See VitroHighlighter for prefix tag and postfix tag */

    Highlighter nonFragHighlighter = null;
    Highlighter fragHighlighter = null;

    Analyzer analyzer = null;

    /**
     * Makes a VitroHighlighter that uses lucene highlighters.
     * PreTag and PostTag are from VitroHighlighter.
     *
     * @param query - the query to highlight for.
     * @param a - the Analyzer that was used in the query.
     */
    public LuceneHighlighter(Query query, Analyzer a){
        QueryScorer scorer = new QueryScorer( query );
        /* See VitroHighlighter for prefix tag and postfix tag */
        Formatter formatter =
            new SimpleHTMLFormatter(preTag,postTag);
        this.analyzer = a;
        this.fragHighlighter = new Highlighter(formatter, scorer);

        //here we make a highlighter that doesn't fragment
        this.nonFragHighlighter = new Highlighter( formatter, scorer);
        this.nonFragHighlighter.setTextFragmenter(new NullFragmenter());
    }

    
    private Pattern htmlOrNot = Pattern.compile("(<[^>]*>)|([^<]*)");
    private int HTML_PATTERN_INDEX = 1;
    private int TEXT_PATTERN_INDEX = 2;
    /**
     * Highlights in a string. No Fragmenting. Attempts to avoid some HTML.
     * @param in
     * @return
     */
    public String highlight( String in){
        Matcher matcher =  htmlOrNot.matcher(in);
        StringBuilder output = new StringBuilder();
        
        boolean found = matcher.find();
        if( ! found )
            return in;
        
        while( found ){
            String foundHtmlElement = matcher.group( HTML_PATTERN_INDEX );
            if( foundHtmlElement != null ){
                output.append( foundHtmlElement );
            }else{
                String foundTextNode = matcher.group( TEXT_PATTERN_INDEX );
                String hi = foundTextNode;
                try {
                    hi = nonFragHighlighter.getBestFragment(analyzer,"contents",foundTextNode);                    
                } catch (IOException e) {
                    return in;
                }
                if( hi != null )
                    output.append( hi );
                else
                    output.append( foundTextNode );
            }
            found = matcher.find();
        }
        return output.toString();        
    }
    
    
            
    
    
    protected boolean WITH_ELLIPSIS = true;
    protected String ellipsis = "...";
    public String getHighlightFragments(String in ) {

        if(WITH_ELLIPSIS ){
            if( in != null && in.trim().length() > 0){
                String b = doHighlight( in ,fragHighlighter);
                if( b != null && b.trim().length() > 0 )
                    return ellipsis + " " + b + " " + ellipsis;
                else
                    return "";
            } else {
                return "";
            }
        } else {
            return doHighlight(  in , fragHighlighter);
        }
    }

    private String doHighlight(String in, Highlighter hi ) {
        String result = in;

        if(in != null ){


            TokenStream tokenStream =
                analyzer.tokenStream("contents", new StringReader(in));
            //       Get 3 best fragments and seperate with a "..."
            try {
                result = hi.getBestFragments(tokenStream, in , 3, "...");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return result;
    }
    
    private final int maxDocCharsToAnalyze = 4000;
    Log log = LogFactory.getLog(LuceneHighlighter.class);
}
