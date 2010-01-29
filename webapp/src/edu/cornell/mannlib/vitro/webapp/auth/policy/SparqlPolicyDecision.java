package edu.cornell.mannlib.vitro.webapp.auth.policy;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;

/**
 * Extends the BasicPolicyDecision with additional debugging information about the
 * sparql queries that were run to create the decision.
 * 
 * @author bdc34
 *
 */
public class SparqlPolicyDecision extends BasicPolicyDecision {
    Query query = null;
    QueryExecution qexec = null;

    public SparqlPolicyDecision(Authorization authorized, String message) {
        super(authorized, message);
    }

    public QueryExecution getQexec() {
        return qexec;
    }

    public void setQexec(QueryExecution qexec) {
        this.qexec = qexec;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    @Override
    public String getDebuggingInfo() {
        String msg = "";
        if( super.getDebuggingInfo() != null && super.getDebuggingInfo().length() > 0)
            msg = super.getDebuggingInfo() + '\n';

        if( query != null )
            msg= msg + "query: \n" + query.toString() + '\n';
         else
            msg = msg + " query was null \n";

        if( qexec != null )
            msg = msg + "query exec: \n" + qexec.toString();
        else
            msg = msg + " query exec was null \n";

        return msg;
    }


}
