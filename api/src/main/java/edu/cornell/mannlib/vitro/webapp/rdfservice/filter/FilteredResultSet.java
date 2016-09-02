/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.binding.Binding;

public class FilteredResultSet implements ResultSet {

    protected Iterator<QuerySolution> solutIt;
    protected ResultSet originalResultSet;
    protected int rowNum = -1;
    
    public FilteredResultSet (List<QuerySolution> solutions, ResultSet originalResultSet) {
        this.solutIt = solutions.iterator();
        this.originalResultSet = originalResultSet;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Attempt to remove an element");
    }

    @Override
    public Model getResourceModel() {
        return originalResultSet.getResourceModel();
    }

    @Override
    public List<String> getResultVars() {
        return originalResultSet.getResultVars();
    }

    @Override
    public int getRowNumber() {
        return rowNum;
    }

    @Override
    public boolean hasNext() {
        return solutIt.hasNext();
    }

    @Override
    public QuerySolution next() {
        return nextSolution();
    }

    @Override
    public Binding nextBinding() {
        throw new UnsupportedOperationException("Can we ignore this?");
    }

    @Override
    public QuerySolution nextSolution() {
        rowNum++;
        return solutIt.next();
    }

}
