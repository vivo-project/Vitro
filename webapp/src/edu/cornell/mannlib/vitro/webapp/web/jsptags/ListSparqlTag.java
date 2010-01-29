package edu.cornell.mannlib.vitro.webapp.web.jsptags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import net.djpowell.sparqltag.SelectTag;
import net.djpowell.sparqltag.SparqlTag;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.utils.IterableAdaptor;

/**
 * sparql:listselect - wraps a SPARQL select query, and copies the resultset into
 * a variable.
 *
 *      var         resultset  (list of rows in the "rows" property)
 *      model       model
 *
 * based on SelectTag.java from David Powell.
 */

public class ListSparqlTag extends SelectTag {

/**
 * 
 * The query get executed, the result set copied into a Collection and
 * the queryExecution gets closed.
 */    
    public void doTag() throws JspException {
        trc.debug("CollectionSparqlTag.doTag()");

        SparqlTag container = ((SparqlTag)SimpleTagSupport.findAncestorWithClass(this, SparqlTag.class));
        if (container == null) {
            throw new RuntimeException("Ancestor must be sparql container");
        }
        //we copy the resultset into a collection so dispose can be called immediately
        //container.addInnerTag((SelectTag)this);
        
        Query query = parseQuery();
        QueryExecution qex = QueryExecutionFactory.create(query, model, qparams);
        trc.debug("query executed");
        
        ResultSet results;
        model.enterCriticalSection(Lock.READ);
        try {
            results = qex.execSelect();
            List<Map<String,RDFNode>> resultList = new LinkedList<Map<String,RDFNode>>();

            for( QuerySolution qs : IterableAdaptor.adapt( (Iterator<QuerySolution>)results )  ){
                trc.debug("found solution");
                HashMap<String,RDFNode> map1 = new HashMap<String,RDFNode>();
                for( String name : IterableAdaptor.adapt((Iterator<String>)qs.varNames())){
                    RDFNode value = qs.get(name);
                    if( trc.isDebugEnabled() ){trc.debug(name + ": " + value.toString() );}
                    map1.put(name, value);
                }
                resultList.add(map1);
            }    
            trc.debug("setting " + var + " to a list of size " + resultList.size() );
            getJspContext().setAttribute(var, resultList);
        } finally {
            model.leaveCriticalSection();
            synchronized (this) {
                if (qex != null) {
                    qex.close();
                }
                model = null;
                var = null;
                qparams = null;
                qex = null;
            }  
        }            
    }

    private static final Logger trc = Logger.getLogger(ListSparqlTag.class);
}
