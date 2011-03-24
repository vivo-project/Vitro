package edu.cornell.mannlib.vitro.webapp.search.lucene;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * This is a QueryParser that will fall back to unstemmed
 * field for WildcardQueries and PrefixQueries.
 *
 * @author bdc34 a cornell dot edu
 */
public class VitroQueryParser extends QueryParser {
    /**
     * Map from stemmed field names to the names of fields with the
     * same terms but unstemmed.
     */
    HashMap <String,String> stemmedToUnstemmed;

    public VitroQueryParser(String f, Analyzer a) {super(f, a); }
    public VitroQueryParser(CharStream stream) {super(stream);  }
    public VitroQueryParser(QueryParserTokenManager tm) {super(tm); }

    /**
     * Sets the map of field name to field name where
     * the key maps to the name of the field with the unstemmed
     * version of the same terms.
     * @param stemmedToUnstemmed
     */
    public void setStemmedToUnstemmed(HashMap<String, String> stemmedToUnstemmed) {
        this.stemmedToUnstemmed = stemmedToUnstemmed;
    }

    /**
     * attempts to get a field name for the unstemmed data of
     * the given stemmedField data.  Returns stemmedField
     * if there is not mapping in stemmedToUnstemmed.
     * @param stemmedField
     * @return
     */
    public String getUnstemmed(String stemmedField){
        if( stemmedField == null ||
            stemmedToUnstemmed == null ||
            !stemmedToUnstemmed.containsKey(stemmedField))
            return stemmedField;
        else
            return stemmedToUnstemmed.get(stemmedField);
    }

    @Override
    protected org.apache.lucene.search.Query getPrefixQuery(String field, String termStr)
    throws ParseException {
        
        if ( termStr.startsWith("*") )
            throw new ParseException("'*' not allowed as first character in PrefixQuery");

        //down case the terms that would have been down cased by the analyzer on index.
        if (  Entity2LuceneDoc.term.ALLTEXT.equals(field) ||
                Entity2LuceneDoc.term.ALLTEXTUNSTEMMED.equals(field) ||
                Entity2LuceneDoc.term.KEYWORDS.equals(field)  ) {                               
            termStr = termStr.toLowerCase();
        }

        Term t = new Term(field, termStr);
        return new PrefixQuery(t);                 
    }

    @Override
    protected org.apache.lucene.search.Query getWildcardQuery(String field, String termStr)
    throws ParseException {                
          if ( (termStr.startsWith("*") || termStr.startsWith("?")) )
            throw new ParseException("'*' or '?' not allowed as first character in WildcardQuery");
          
          //down case the terms that would have been down cased by the analyzer on index.
          if (  Entity2LuceneDoc.term.ALLTEXT.equals(field) ||
                  Entity2LuceneDoc.term.ALLTEXTUNSTEMMED.equals(field) ||
                  Entity2LuceneDoc.term.KEYWORDS.equals(field)  ) {                               
            termStr = termStr.toLowerCase();
          }
          
          Term t = new Term(field, termStr);
          return new WildcardQuery(t);        
    }
    
    
    @Override
    protected org.apache.lucene.search.Query getFieldQuery(String arg0,
            String arg1) throws ParseException {
        // TODO Auto-generated method stub
        return super.getFieldQuery(arg0, arg1);
    }
    
    
}
