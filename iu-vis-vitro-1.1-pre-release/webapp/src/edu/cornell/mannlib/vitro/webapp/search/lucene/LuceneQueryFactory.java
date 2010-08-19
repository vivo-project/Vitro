/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.analysis.Analyzer;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;

public class LuceneQueryFactory implements VitroQueryFactory {

    public static final int MAX_QUERY_LENGTH = 500;
    private String indexDir;

    public LuceneQueryFactory(Analyzer analyzer, String indexDir ){
        this.analyzer = analyzer;
        this.indexDir = indexDir;
    }

    private Analyzer analyzer = null;

    public VitroQuery getQuery(VitroRequest request, PortalFlag portalState) throws SearchException {
        //there should be a better way to integrate this with LuceneQuery
        //here we check that the request has the parameters that we need to
        //make the query.  If it does not then we return null.    	
        String txt = request.getParameter(VitroQuery.QUERY_PARAMETER_NAME);
        if( txt == null || txt.length() == 0 )
            return null;
        if( txt.length() > MAX_QUERY_LENGTH )
            throw new SearchException("The search was too long. The maximum " +
            		"query length is " + MAX_QUERY_LENGTH );
        LuceneQuery query = new LuceneQuery(request, portalState, analyzer, indexDir);
        return query;
    }

    public Analyzer getAnalyzer(){
        return analyzer;
    }
}
