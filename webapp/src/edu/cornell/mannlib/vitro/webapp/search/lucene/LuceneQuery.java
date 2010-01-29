package edu.cornell.mannlib.vitro.webapp.search.lucene;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

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
 * @author bdc34
 *
 */
public class LuceneQuery extends VitroQuery {
    private final String defaultSearchField = "ALLTEXT";

    private final int SIMPLE = 1;
    private final int ADVANCED =2;
    private int queryType = SIMPLE;

    public boolean defaultTimeWindow = true;

    private Query query = null;
    private Analyzer analyzer = null;
    
    private static final Log log = LogFactory.getLog(LuceneQuery.class.getName());

    //private IndexReader indexReader;

    public LuceneQuery(VitroRequest request, PortalFlag portalState,
                       Analyzer analyzer, String indexDir ){
    	
        super(request,portalState); //the super class will stash the parameters for us.
        this.analyzer = analyzer;

//        if( indexReader == null ){
//          try {
//              indexReader = IndexReader.open( indexDir );
//          } catch (IOException e) {
//              System.out.println("LuceneQuery: could not create IndexReader"+e);
//              e.printStackTrace();
//          }
//        }
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
        VitroQueryParser qp = new VitroQueryParser(defaultSearchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        //set up the map of stemmed field names -> unstemmed field names
        HashMap<String,String> map = new HashMap<String, String>();
        map.put(Entity2LuceneDoc.term.ALLTEXT,Entity2LuceneDoc.term.ALLTEXTUNSTEMMED);
        qp.setStemmedToUnstemmed(map);

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

                //if we have a flag/portal query then we add
                //it by making a BooelanQuery.
                Query flagQuery = makeFlagQuery();
                if( flagQuery != null ){
                    BooleanQuery boolQuery = new BooleanQuery();
                    boolQuery.add( this.query, BooleanClause.Occur.MUST);
                    boolQuery.add( flagQuery, BooleanClause.Occur.MUST);
                    this.query = boolQuery;
                }
            } else if( ADVANCED == queryType ){
                this.query = null;
            }
        }catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return this.query;
    }

    /**
     * Adds a Query that will get doc where the
     * SUNSET is > NOW  and SUNRISE <= NOW. We'll do
     * this by creating two RangeQueries, one to
     * check that SUNRISE is between [BEGINNING_OF_TIME, NOW]
     * and that SUNSET is between [NOW, END_OF_TIME]
     * There don't seem to be any GraterThanQuery
     * or LessThanQuery classes in lucene.
     */
//     private BooleanQuery makeDefaultTimeWindowQuery(){
//         String nowStr = new DateTime().toString(LuceneIndexer.DATE_FORMAT);

//         Term BEGINNING_OF_TIME = null;
//         Term now = new Term(Entity2LuceneDoc.term.SUNRISE,nowStr );
//         RangeQuery sunriseBeforeNow = new RangeQuery(BEGINNING_OF_TIME,now, true);

//         Term END_OF_TIME = null;
//         now = new Term(Entity2LuceneDoc.term.SUNSET,nowStr);
//         RangeQuery sunsetAfterNow = new RangeQuery(now,END_OF_TIME, false);

//         BooleanQuery qRv = new BooleanQuery();
//         qRv.add( sunriseBeforeNow, BooleanClause.Occur.MUST);
//         qRv.add( sunsetAfterNow, BooleanClause.Occur.MUST);

//         return qRv;
//     }

    /**
     * Makes queries to return only things between the given times and adds
     * them as BooleanQuery objects.
     *
     * If earliest is null then the query include anything that existed before latest.
     * If latest is null then the query will include anthing that existes after earliest.
     * If both earliest and latest are null then NO restrictions will be added to the query.
     */
    private Query addTimeWindowedQuery( Query query, DateTime earliest, DateTime latest){
        Query returnQuery = null;
        if( earliest ==null && latest == null ) return query;

        if( earliest != null && latest != null ){
            //we work with the SUNSET here since that is the last time the
            //object will be seen.
             Term earliestTerm = new Term(Entity2LuceneDoc.term.SUNSET,
                                          earliest.toString(LuceneIndexer.DATE_FORMAT));
             Term latestTerm = new Term(Entity2LuceneDoc.term.SUNRISE,
                                        latest.toString(LuceneIndexer.DATE_FORMAT));

             RangeQuery timeWindowQuery = new RangeQuery(earliestTerm,latestTerm, true);
             BooleanQuery bQuery = new BooleanQuery();
             bQuery.add( query, BooleanClause.Occur.MUST);
             bQuery.add( timeWindowQuery, BooleanClause.Occur.MUST);
             returnQuery = bQuery;
        }
        return returnQuery;
    }

            // Term beginning_of_time = new Term(Entity2LuceneDoc.term.SUNSET,
//                                               BEGINNING_OF_TIME);
//             Term end_of_time = new Term(Entity2LuceneDoc.term.SUNRISE,
//                                         END_OF_TIME);
    /**
     * Makes a flag based query clause.  This is where searches can filter by portal.
     *
     * If you think that search is not working correctly with protals and
     * all that kruft then this is a method you want to look at.
     *
     * It only takes into account "the portal flag" and flag1Exclusive must
     * be set.  Where does that stuff get set?  Look in vitro.flags.PortalFlag
     *
     */
    @SuppressWarnings("static-access")
    private Query makeFlagQuery(){
        PortalFlag flag = super.getPortalState();
        if( flag == null || !flag.isFilteringActive() )
            return null;

//bdc34 - this is commented out because the exclusive flags are not property set when
// the portalFlag gets made.
//      if( !flag.getFlag1Exclusive() )
            //Q: what does it mean for flag1exclusive to be false?
            //A: it means we don't take it into account
//          return null;

//      System.out.println("in LuceneQuery and we are trying to figure out what is in the portalFlag:\n"+flag);

        // make one term for each bit in the numeric flag that is set
        Collection<TermQuery> terms = new LinkedList<TermQuery>();
        int portalNumericId = flag.getFlag1Numeric();
        Long[] bits = FlagMathUtils.numeric2numerics(portalNumericId);
        for (Long bit : bits) {
            terms.add(new TermQuery(new Term(Entity2LuceneDoc.term.PORTAL, Long
                    .toString(bit))));
        }

        // make a boolean OR query for all of those terms
        BooleanQuery boolQuery = new BooleanQuery();
        if (terms.size() > 0) {
            for (TermQuery term : terms) {
                    boolQuery.add(term, BooleanClause.Occur.SHOULD);
            }
            return boolQuery;
        } else {
            //we have no flags set, very odd, abort filtering
            return null;
        }
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
