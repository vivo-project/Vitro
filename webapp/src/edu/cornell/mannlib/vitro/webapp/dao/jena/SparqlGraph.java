package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.graph.impl.SimpleEventManager;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.SingletonIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SparqlGraph implements GraphWithPerform {
    
    private String endpointURI;
    private static final Log log = LogFactory.getLog(SparqlGraph.class);
    
    private BulkUpdateHandler bulkUpdateHandler;
    private PrefixMapping prefixMapping = new PrefixMappingImpl();
    private GraphEventManager eventManager;
    private Reifier reifier = new EmptyReifier(this);
    private GraphStatisticsHandler graphStatisticsHandler;
    private TransactionHandler transactionHandler;
    private QueryHandler queryHandler;
    
    private Repository repository;
    
    public SparqlGraph(String endpointURI) {
        this.endpointURI = endpointURI;
        this.repository = new HTTPRepository(endpointURI);
    }
    
    private RepositoryConnection getConnection() {
        try {
            return this.repository.getConnection();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(Triple arg0) throws AddDeniedException {
        performAdd(arg0);
    }

    @Override
    public void performAdd(Triple t) {
        
        //log.info("adding " + t);
        
        String updateString = "INSERT DATA { GRAPH <junk:junk> { " 
                + sparqlNode(t.getSubject(), "") + " " 
                + sparqlNode(t.getPredicate(), "") + " " 
                + sparqlNode(t.getObject(), "") + 
                " } }";
        
        if (false) {
            try { 
            
                throw new RuntimeException("Breakpoint");
            } catch (RuntimeException e) {
                log.error(e, e);
                //throw(e);
            }
        }
        
        //log.info(updateString);
        
        try {
            RepositoryConnection conn = getConnection();
            try {
                Update u = conn.prepareUpdate(QueryLanguage.SPARQL, updateString);
                u.execute();
            } catch (MalformedQueryException e) {
                throw new RuntimeException(e);
            } catch (UpdateExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException re) {
            throw new RuntimeException(re);
        }
        
    }
    
    @Override
    public void performDelete(Triple t) {
        log.info ("************** DELETE!!!!! ********************");
        
        String updateString = "DELETE DATA { GRAPH <junk:junk> { " 
                + sparqlNode(t.getSubject(), "") + " " 
                + sparqlNode(t.getPredicate(), "") + " " 
                + sparqlNode(t.getObject(), "") + 
                " } }";
        
        log.info(updateString);
        
        try {
            RepositoryConnection conn = getConnection();
            try {
                Update u = conn.prepareUpdate(QueryLanguage.SPARQL, updateString);
                u.execute();
            } catch (MalformedQueryException e) {
                throw new RuntimeException(e);
            } catch (UpdateExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException re) {
            throw new RuntimeException(re);
        }
    }
    
    @Override
    public void close() {
        // can't close a remote endpoint
    }

    @Override
    public boolean contains(Triple arg0) {
       return contains(arg0.getSubject(), arg0.getPredicate(), arg0.getObject());
    }

    @Override
    public boolean contains(Node subject, Node predicate, Node object) {
        if (subject.isBlank() || predicate.isBlank() || object.isBlank()) {
            return false;
        }
        StringBuffer containsQuery = new StringBuffer("ASK { \n")
        .append(sparqlNode(subject, "?s"))
        .append(" ")
        .append(sparqlNode(predicate, "?p"))
        .append(" ")
        .append(sparqlNode(object, "?o"))
        .append("\n}");
        return execAsk(containsQuery.toString());    
    }

    @Override
    public void delete(Triple arg0) throws DeleteDeniedException {
        log.info("********************** DELETE!!!!!! ************************");
        performDelete(arg0);
    }

    @Override
    public boolean dependsOn(Graph arg0) {
        return false; // who knows?
    }

    @Override
    public ExtendedIterator<Triple> find(TripleMatch arg0) {
        //log.info("find(TripleMatch) " + arg0);
        Triple t = arg0.asTriple();
        return find(t.getSubject(), t.getPredicate(), t.getObject());
    }

    private String sparqlNode(Node node, String varName) {
        if (node == null || node.isVariable()) {
            return varName;
        } else if (node.isBlank()) {
            return "<" + "fake:blank" + ">"; // or throw exception?
        } else if (node.isURI()) {
            StringBuffer uriBuff = new StringBuffer();
            return uriBuff.append("<").append(node.getURI()).append(">").toString();
        } else if (node.isLiteral()) {
            StringBuffer literalBuff = new StringBuffer();
            literalBuff.append("\"").append(node.getLiteralLexicalForm()).append("\"");
            if (node.getLiteralDatatypeURI() != null) {
                literalBuff.append("^^<").append(node.getLiteralDatatypeURI()).append(">");
            } else if (node.getLiteralLanguage() != null && node.getLiteralLanguage() != "") {
                literalBuff.append("@").append(node.getLiteralLanguage());
;            }
            return literalBuff.toString();
        } else {
            return varName;
        }
    }
    
    @Override
    public ExtendedIterator<Triple> find(Node subject, Node predicate, Node object) {
        if (!isVar(subject) && !isVar(predicate)  && !isVar(object)) {
            if (contains(subject, predicate, object)) {
                return new SingletonIterator(new Triple(subject, predicate, object));
            } else {
                return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
            }
        }
        StringBuffer findQuery = new StringBuffer("SELECT * WHERE { \n")
        .append(sparqlNode(subject, "?s"))
        .append(" ")
        .append(sparqlNode(predicate, "?p"))
        .append(" ")
        .append(sparqlNode(object, "?o"))
        .append("\n}");
        
        //log.info(findQuery.toString());
        ResultSet rs = execSelect(findQuery.toString());
        //rs = execSelect(findQuery.toString());
        //rs = execSelect(findQuery.toString());
        
        List<Triple> triplist = new ArrayList<Triple>();
        while (rs.hasNext()) {
            QuerySolution soln = rs.nextSolution();
            Triple t = new Triple(isVar(subject) ? soln.get("?s").asNode() : subject, 
                                  isVar(predicate) ? soln.get("?p").asNode() : predicate, 
                                  isVar(object) ? soln.get("?o").asNode() : object);
            //log.info(t);
            triplist.add(t);
        }
        //log.info(triplist.size() + " results");
        return WrappedIterator.create(triplist.iterator());
    }

    private boolean isVar(Node node) {
        return (node == null || node.isVariable() || node == Node.ANY);
    }
    
    @Override
    public BulkUpdateHandler getBulkUpdateHandler() {
        if (this.bulkUpdateHandler == null) {
            this.bulkUpdateHandler = new SparqlGraphBulkUpdater(this);
        }
        return this.bulkUpdateHandler;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public GraphEventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new SimpleEventManager(this);
        }
        return eventManager;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public Reifier getReifier() {
        //if (reifier == null) {
        //    reifier = new SimpleReifier(this, ReificationStyle.Standard);
        //}
        return reifier;
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler() {
        return null;
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        return (size() == 0);
    }

    @Override
    public boolean isIsomorphicWith(Graph arg0) {
        throw new UnsupportedOperationException("isIsomorphicWith() not supported " +
        		"by SPARQL graphs");
    }

    @Override
    public QueryHandler queryHandler() {
        if (queryHandler == null) {
            queryHandler = new SimpleQueryHandler(this);
        }
        return queryHandler;
    }

    @Override
    public int size() {
        return find(null, null, null).toList().size();
    }
    
    private final static Capabilities capabilities = new Capabilities() {
        
        public boolean addAllowed() {
            return false;
        }
        
        public boolean addAllowed(boolean everyTriple) {
            return false;
        }
        
        public boolean canBeEmpty() {
            return true;
        }
        
        public boolean deleteAllowed() {
            return false;
        }
        
        public boolean deleteAllowed(boolean everyTriple) {
            return false;
        }
        
        public boolean findContractSafe() {
            return true;
        }
        
        public boolean handlesLiteralTyping() {
            return true;
        }
        
        public boolean iteratorRemoveAllowed() {
            return false;
        }
        
        public boolean sizeAccurate() {
            return true;
        }
    };
    
    private boolean execAsk(String queryStr) {
        Query askQuery = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, askQuery);
        try {
            return qe.execAsk();
        } finally {
            qe.close();
        }
    }
    
    private ResultSet execSelect(String queryStr) {
        
//        long startTime1 = System.currentTimeMillis();
//        try {
//            
//            RepositoryConnection conn = getConnection();
//            try {
//                GraphQuery q = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
//                q.evaluate();
//            } catch (MalformedQueryException e) {
//                throw new RuntimeException(e);
//            } finally {
//                conn.close();
//            }
//        } catch (Exception re) {
//            //log.info(re,re);
//        }
        
//        log.info((System.currentTimeMillis() - startTime1) + " to execute via sesame");
        
        long startTime = System.currentTimeMillis();
        Query askQuery = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, askQuery);
        try {
            return new ResultSetMem(qe.execSelect());
        } finally {
            //log.info((System.currentTimeMillis() - startTime) + " to execute via Jena");
            qe.close();
        }
    }
    

}
