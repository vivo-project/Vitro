package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.SingletonIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SparqlDatasetGraph implements DatasetGraph {

    private String endpointURI;
    private Repository repository;
    
    public SparqlDatasetGraph(String endpointURI) {
        this.endpointURI = endpointURI;
        this.repository = new HTTPRepository(endpointURI);
    }

    private Graph getGraphFor(Quad q) {
        return getGraphFor(q.getGraph());
    }
    
    private Graph getGraphFor(Node g) {
        return (g == Node.ANY) 
                ? new SparqlGraph(endpointURI) 
                : new SparqlGraph(endpointURI, g.getURI());
    }
    
    @Override
    public void add(Quad arg0) {
        getGraphFor(arg0).add(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
    }

    @Override
    public void addGraph(Node arg0, Graph arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean contains(Quad arg0) {
        return getGraphFor(arg0).contains(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
    }

    @Override
    public boolean contains(Node arg0, Node arg1, Node arg2, Node arg3) {
        return getGraphFor(arg0).contains(arg1, arg2, arg3);
    }

    @Override
    public boolean containsGraph(Node arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void delete(Quad arg0) {
        getGraphFor(arg0).delete(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
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
    public Iterator<Quad> find(Node graph, Node subject, Node predicate, Node object) {
        if (!isVar(subject) && !isVar(predicate)  && !isVar(object) &&!isVar(graph)) {
            if (contains(subject, predicate, object, graph)) {
                return new SingletonIterator(new Triple(subject, predicate, object));
            } else {
                return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
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
        ResultSet rs = execSelect(findQuery.toString());
        //rs = execSelect(findQuery.toString());
        //rs = execSelect(findQuery.toString());
        
        List<Quad> quadlist = new ArrayList<Quad>();
        while (rs.hasNext()) {
            QuerySolution soln = rs.nextSolution();
            Quad q = new Quad(isVar(graph) ? soln.get("?g").asNode() : graph,
                                  isVar(subject) ? soln.get("?s").asNode() : subject, 
                                  isVar(predicate) ? soln.get("?p").asNode() : predicate, 
                                  isVar(object) ? soln.get("?o").asNode() : object);
            //log.info(t);
            quadlist.add(q);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Graph getDefaultGraph() {
        return new SparqlGraph(endpointURI);
    }

    @Override
    public Graph getGraph(Node arg0) {
        return new SparqlGraph(endpointURI, arg0.getURI());
    }

    @Override
    public Lock getLock() {
        // TODO Auto-generated method stub
        return null;
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
            RepositoryConnection conn = getConnection();
            try {
                RepositoryResult<Resource> conResult = conn.getContextIDs();
                while (conResult.hasNext()) {
                    Resource con = conResult.next();
                    graphNodeList.add(Node.createURI(con.stringValue()));   
                }
            } finally {
                conn.close();
            }
        } catch (RepositoryException re) {
            throw new RuntimeException(re);
        }
        return graphNodeList.iterator();
    }
    
    private RepositoryConnection getConnection() {
        try {
            return this.repository.getConnection();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
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
    
    private ResultSet execSelect(String queryStr) {
        
//      long startTime1 = System.currentTimeMillis();
//      try {
//          
//          RepositoryConnection conn = getConnection();
//          try {
//              GraphQuery q = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
//              q.evaluate();
//          } catch (MalformedQueryException e) {
//              throw new RuntimeException(e);
//          } finally {
//              conn.close();
//          }
//      } catch (Exception re) {
//          //log.info(re,re);
//      }
      
//      log.info((System.currentTimeMillis() - startTime1) + " to execute via sesame");
      
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
