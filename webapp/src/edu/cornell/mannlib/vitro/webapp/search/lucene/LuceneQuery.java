/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;

/**
 * Creates a query that can be used to search a Lucene index.
 * This class uses the Lucene class QueryParser to parse the
 * text from the html text field input form.
 *
 * For information about how syntaxt and semantics of the
 * QueryParser see:
 * http://lucene.apache.org/java/docs/queryparsersyntax.html
 * http://today.java.net/pub/a/today/2003/11/07/QueryParserRules.html
 *
 * This class is not thread safe, use one instance per request.
 * @author bdc34
 *
 */
public class LuceneQuery extends VitroQuery {
    private final String defaultSearchField = "ALLTEXT";

    private final int SIMPLE = 1;
    private final int ADVANCED =2;
    private int queryType = SIMPLE;

    private Query query = null;
    private Analyzer analyzer = null;
    
    private static final Log log = LogFactory.getLog(LuceneQuery.class.getName());

    public LuceneQuery(VitroRequest request, 
                       Analyzer analyzer, 
                       String defaultField ){    	
        super(request); //the super class will stash the parameters for us.
        this.analyzer = analyzer;

        if( isAdvancedQuery( request ) ){
            queryType = ADVANCED;
        }
    }

    @SuppressWarnings("static-access")
    private QueryParser getQueryParser(){    	
        //defaultSearchField indicates which field search against when there is no term
        //indicated in the query string.
        //The analyzer is needed so that we use the same analyzer on the search queries as
        //was used on the text that was indexed.
        QueryParser qp = new QueryParser(defaultSearchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        return qp;
    }

    public Object getQuery() throws SearchException {
        if( this.query != null )
            return this.query;

        String querystr = "";
        if( getParameters() != null && getParameters().get(VitroQuery.QUERY_PARAMETER_NAME)!=null ){
            Object obj= getParameters().get(VitroQuery.QUERY_PARAMETER_NAME);
            if( obj instanceof String[])
                querystr = ((String[])obj)[0];
            else
                log.debug("LuceneQquery.getQuery() querytext is of class " +
                                   obj.getClass().getName());
        }
        else
            throw new SearchException(this.getClass().getName() +
                    ": There was no Parameter '"+VitroQuery.QUERY_PARAMETER_NAME+"' in the request.");

        try{
            if( SIMPLE == queryType ){
                QueryParser parser= getQueryParser();
                this.query = parser.parse(querystr);
            } else if( ADVANCED == queryType ){
                this.query = null;
            }
        }catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return this.query;
    }

    /**
     * Check to see if the request came from some sort of advanced query page.
     *
     * @param request
     * @return
     */
    private boolean isAdvancedQuery(HttpServletRequest request){
        return false;
    }

    @Override
    public String getTerms() {
        if( getParameters() != null &&
            getParameters().get(VitroQuery.QUERY_PARAMETER_NAME) != null) {
            Object terms = getParameters().get(VitroQuery.QUERY_PARAMETER_NAME);
            if( terms instanceof String[] ) {
                return ((String[])terms)[0];
            } else {
                log.debug("LuceneQuery.getTerms(): terms in a " + terms.getClass().getName() );
                return "";
            }
        } else
            return "";
    }

    public Analyzer getAnalyzer(){return analyzer;}
}
