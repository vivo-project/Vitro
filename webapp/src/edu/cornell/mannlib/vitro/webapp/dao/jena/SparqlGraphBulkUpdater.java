package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SparqlGraphBulkUpdater extends SimpleBulkUpdateHandler {

    private static final Log log = LogFactory.getLog(SparqlGraphBulkUpdater.class);
    private SparqlGraph graph;
    
    public SparqlGraphBulkUpdater(SparqlGraph graph) {
        super(graph);
        this.graph = graph;
    }
    
    @Override
    public void add(Triple[] arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (int i = 0 ; i < arg0.length ; i++) {
            g.add(arg0[i]);
        }
        add(g);
    }

    @Override
    public void add(List<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (Triple t : arg0) {
            g.add(t);
        }
        add(g);
    }

    @Override
    public void add(Iterator<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        while (arg0.hasNext()) {
            Triple t = arg0.next();
            g.add(t);
        }
        add(g);
    }

    @Override
    public void add(Graph arg0) {
        add(arg0, false);
    }

    @Override
    public void add(Graph g, boolean arg1) {
        Model gm = ModelFactory.createModelForGraph(g);
        Model blankNodeModel = ModelFactory.createDefaultModel();
        Model nonBlankNodeModel = ModelFactory.createDefaultModel();
        StmtIterator sit = gm.listStatements();
        while (sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (!stmt.getSubject().isAnon() && !stmt.getObject().isAnon()) {
                nonBlankNodeModel.add(stmt);
            } else {
                blankNodeModel.add(stmt);
            }
        }
        addModel(nonBlankNodeModel);
        // replace following call with different method
        addModel(blankNodeModel);
    }
    
    public void addModel(Model model) {
        Model m = ModelFactory.createDefaultModel();
        int testLimit = 1000;
        StmtIterator stmtIt = model.listStatements();
        int count = 0;
        try {
            while (stmtIt.hasNext()) {
                count++;
                m.add(stmtIt.nextStatement());
                if (count % testLimit == 0 || !stmtIt.hasNext()) {
                    StringWriter sw = new StringWriter();
                    m.write(sw, "N-TRIPLE");
                    StringBuffer updateStringBuff = new StringBuffer();
                    String graphURI = graph.getGraphURI();
                    updateStringBuff.append("INSERT DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" ));
                    updateStringBuff.append(sw);
                    updateStringBuff.append(((graphURI != null) ? " } " : "") + " }");
                    
                    String updateString = updateStringBuff.toString();
                    
                    //log.info(updateString);
                    
                    graph.executeUpdate(updateString);

                    m.removeAll();
                }
            }
        } finally {
            stmtIt.close();
        }
    }
    

    @Override 
    public void removeAll() {
        removeAll(graph);
        notifyRemoveAll(); 
    }

    protected void notifyRemoveAll() { 
        manager.notifyEvent(graph, GraphEvents.removeAll);
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        removeAll(graph, s, p, o);
        manager.notifyEvent(graph, GraphEvents.remove(s, p, o));
    }

    public static void removeAll(Graph g, Node s, Node p, Node o)
    {
        // OK, so the strategy here should be to remove all triples without blank nodes first
        // Then, feel the entire remaining part of the graph as a DELETE WHERE 
        // with the blank nodes as variables?
        
        ExtendedIterator<Triple> it = g.find( s, p, o );
        try { 
            while (it.hasNext()) {
                Triple t = it.next();
                g.delete(t);
                it.remove(); 
            } 
        }
        finally {
            it.close();
        }
    }

    public static void removeAll( Graph g )
    {
        ExtendedIterator<Triple> it = GraphUtil.findAll(g);
        try {
            while (it.hasNext()) {
                Triple t = it.next();
                g.delete(t);
                it.remove();
            } 
        } finally {
            it.close();
        }
        
        // get rid of remaining blank nodes using a SPARQL DELETE
        if (g instanceof SparqlGraph) {
            ((SparqlGraph) g).removeAll();
        }
                
    }

}
