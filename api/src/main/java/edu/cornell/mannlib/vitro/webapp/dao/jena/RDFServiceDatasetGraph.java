/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRSW;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.SingletonIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class RDFServiceDatasetGraph implements DatasetGraph {

    private RDFService rdfService;
    private RDFServiceGraph defaultGraph;
    private Lock lock = new LockMRSW();
    private Context context = new Context() ;
    private Map<String, RDFServiceGraph> graphCache = new ConcurrentHashMap<String, RDFServiceGraph>();
    private ReadWrite transactionMode;
    
    public RDFServiceDatasetGraph(RDFService rdfService) {
        this.rdfService = rdfService;
        this.defaultGraph = new RDFServiceGraph(rdfService);
    }

    public Map<String, RDFServiceGraph> getGraphCache() {
        return graphCache;
    }

    @Override
    public synchronized void begin(ReadWrite mode) {
        this.transactionMode = mode;
        graphCache.clear();
    }

    @Override
    public synchronized void commit() {
        this.transactionMode = null;
        if (supportsTransactions(defaultGraph)) {
            defaultGraph.getTransactionHandler().commit();
        }

        for (RDFServiceGraph graph : graphCache.values()) {
            if (supportsTransactions(graph)) {
                graph.getTransactionHandler().commit();
            }

        }

        graphCache.clear();
    }

    @Override
    public synchronized void abort() {
        this.transactionMode = null;
        if (supportsTransactions(defaultGraph)) {
            defaultGraph.getTransactionHandler().abort();
        }

        for (RDFServiceGraph graph : graphCache.values()) {
            if (supportsTransactions(graph)) {
                graph.getTransactionHandler().abort();
            }

        }

        graphCache.clear();
    }

    @Override
    public boolean isInTransaction() {
        return false;
    }

    public void end() {
        this.transactionMode = null;
    }
    
    private Graph getGraphFor(Quad q) {
        return getGraphFor(q.getGraph());
    }
    
    private Graph getGraphFor(Node g) {
        return (g == Node.ANY) 
                ? defaultGraph
                : getGraph(g);
    }
    
    @Override
    public void add(Quad arg0) {
        getGraphFor(arg0).add(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
    }

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		add(new Quad(g, s, p, o));
	}

    @Override
    public void addGraph(Node uri, Graph arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean supportsTransactionAbort() {
        return true;
    }

    @Override
    public boolean contains(Quad arg0) {
        return getGraphFor(arg0).contains(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(Node arg0, Node arg1, Node arg2, Node arg3) {
        return getGraphFor(arg0).contains(arg1, arg2, arg3);
    }

    @Override
    public boolean containsGraph(Node arg0) {
        return getGraphFor(arg0) != null;
    }

    @Override
    public void delete(Quad arg0) {
        getGraphFor(arg0).delete(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
    }

	@Override
	public void delete(Node g, Node s, Node p, Node o) {
		delete(new Quad(g, s, p, o));
	}
    
    @Override
    public void deleteAny(Node arg0, Node arg1, Node arg2, Node arg3) {
        // TODO check this
        getGraphFor(arg0).delete(new Triple(arg1, arg2, arg3));
    }

    @Override
    public Iterator<Quad> find() {
        return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public Iterator<Quad> find(Quad arg0) {
        return find(arg0.getSubject(), arg0.getPredicate(), arg0.getObject(), arg0.getGraph());
    }

    @Override
    public Iterator<Quad> find(final Node graph, final Node subject, final Node predicate, final Node object) {
        if (!isVar(subject) && !isVar(predicate)  && !isVar(object) &&!isVar(graph)) {
            if (contains(subject, predicate, object, graph)) {
                return new SingletonIterator<Quad>(new Quad(subject, predicate, object, graph));
            } else {
                return WrappedIterator.create(Collections.<Quad>emptyIterator());
            }
        }
        StringBuffer findQuery = new StringBuffer("SELECT * WHERE { \n");
        String graphURI = !isVar(graph) ? graph.getURI() : null;
        findQuery.append("  GRAPH ");
        if (graphURI != null) {
            findQuery.append("  <" + graphURI + ">");
        } else {
            findQuery.append("?g");
        }
        findQuery.append(" { ");
        findQuery.append(SparqlGraph.sparqlNode(subject, "?s"))
        .append(" ")
        .append(SparqlGraph.sparqlNode(predicate, "?p"))
        .append(" ")
        .append(SparqlGraph.sparqlNode(object, "?o"));
        findQuery.append("  } ");
        findQuery.append("\n}");
        
        //log.info(findQuery.toString());
        
        final List<Quad> quadlist = new ArrayList<Quad>();
        try {
            rdfService.sparqlSelectQuery(findQuery.toString(), new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    Quad q = new Quad(isVar(graph) ? qs.get("?g").asNode() : graph,
                            isVar(subject) ? qs.get("?s").asNode() : subject,
                            isVar(predicate) ? qs.get("?p").asNode() : predicate,
                            isVar(object) ? qs.get("?o").asNode() : object);

                    quadlist.add(q);
                }
            });
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }

        //log.info(triplist.size() + " results");
        return WrappedIterator.create(quadlist.iterator());    }

    @Override
    public Iterator<Quad> findNG(Node arg0, Node arg1, Node arg2, Node arg3) {
        // TODO check this
        return find(arg0, arg1, arg2, arg3);
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public RDFServiceGraph getDefaultGraph() {
        return defaultGraph;
    }
    
    @Override
    public RDFServiceGraph getGraph(Node arg0) {
        String graphURI = arg0.getURI();
        if(graphCache.containsKey(graphURI)) {
            return graphCache.get(graphURI);
        } else {
            RDFServiceGraph graph = new RDFServiceGraph(rdfService, arg0.getURI());
            graphCache.put(graphURI, graph);
            if(transactionMode != null && supportsTransactions(graph)) {
                graph.getTransactionHandler().begin();
            }
            return graph;
        }
    }
    
    private boolean supportsTransactions(Graph graph) {
        return (graph.getTransactionHandler() != null 
                && graph.getTransactionHandler().transactionsSupported());
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        List<Node> graphNodeList = new ArrayList<Node>();
        try {
            for (String graphURI : rdfService.getGraphURIs()) {
                graphNodeList.add(NodeFactory.createURI(graphURI));   
            }
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
        return graphNodeList.iterator();
    }

    @Override
    public void removeGraph(Node arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDefaultGraph(Graph arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public long size() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    private boolean isVar(Node node) {
        return (node == null || node.isVariable() || node == Node.ANY);
    }

	@Override
	public String toString() {
		return "RDFServiceDatasetGraph[" + ToString.hashHex(this)
				+ ", " + rdfService + "]";
	}
	
}
